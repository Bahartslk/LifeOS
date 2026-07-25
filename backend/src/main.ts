import { Logger, RequestMethod, ValidationPipe, VersioningType } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { NestFactory } from '@nestjs/core';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import compression from 'compression';
import helmet from 'helmet';
import { AppModule } from './app.module';
import { AppConfig } from './config/configuration';

async function bootstrap(): Promise<void> {
  const app = await NestFactory.create(AppModule);
  const configService = app.get(ConfigService<AppConfig, true>);

  app.use(helmet());
  app.use(compression());

  // Cross-origin access for local/dev tooling (e.g. Swagger UI, a future
  // desktop/web Compose Multiplatform target). Mobile native clients are
  // unaffected by CORS.
  app.enableCors();

  // Every request DTO is validated before it reaches business logic, per
  // docs/12-project-architecture.md#api-validation--input-sanitization.
  // whitelist + forbidNonWhitelisted reject unexpected fields outright
  // instead of silently dropping them.
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
    }),
  );

  // URI-based versioning, per docs/15-api-design.md#versioning (/api/v1/...).
  // /health is excluded from both the prefix and versioning — infra tooling
  // (Docker healthcheck, load balancers, k8s probes) needs a single stable
  // path that doesn't change if the API version bumps.
  app.setGlobalPrefix('api', {
    exclude: [{ path: 'health', method: RequestMethod.GET }],
  });
  app.enableVersioning({
    type: VersioningType.URI,
    defaultVersion: '1',
  });

  const swaggerConfig = new DocumentBuilder()
    .setTitle('LifeOS API')
    .setDescription('REST API for LifeOS — see docs/15-api-design.md for the full contract.')
    .setVersion('1.0')
    .addBearerAuth()
    .build();
  const swaggerDocument = SwaggerModule.createDocument(app, swaggerConfig);
  SwaggerModule.setup('api/docs', app, swaggerDocument);

  const port = configService.get('port', { infer: true });
  await app.listen(port);

  Logger.log(`LifeOS API listening on port ${port}`, 'Bootstrap');
  Logger.log(`Swagger docs available at /api/docs`, 'Bootstrap');
}

bootstrap();
