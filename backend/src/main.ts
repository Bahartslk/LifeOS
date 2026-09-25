import { Logger, RequestMethod, ValidationPipe, VersioningType } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { NestFactory } from '@nestjs/core';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import compression from 'compression';
import type { NextFunction, Request, Response } from 'express';
import helmet from 'helmet';
import { AppModule } from './app.module';
import { AppConfig } from './config/configuration';

async function bootstrap(): Promise<void> {
  const app = await NestFactory.create(AppModule);
  const configService = app.get(ConfigService<AppConfig, true>);

  app.use(helmet());
  app.use(compression());

  // Nest has no built-in access log — without this, Railway's runtime logs
  // only ever show explicit Logger.log calls (this file's two below), so a
  // request that never reaches the process and one that reaches it but
  // fails somewhere Nest doesn't log itself (validation, an unhandled
  // exception, ...) are both silent and indistinguishable from the outside.
  const httpLogger = new Logger('HTTP');
  app.use((req: Request, res: Response, next: NextFunction) => {
    const start = Date.now();
    res.on('finish', () => {
      httpLogger.log(`${req.method} ${req.originalUrl} ${res.statusCode} ${Date.now() - start}ms`);
    });
    next();
  });

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
  // Explicit 0.0.0.0, not just `listen(port)`: Railway (and most container
  // platforms) route external traffic to the container through a reverse
  // proxy that connects to it, not to a loopback-only listener — binding
  // unambiguously to all interfaces is required for that proxy to ever
  // reach this process, per Railway's own deployment guidance.
  await app.listen(port, '0.0.0.0');

  Logger.log(`LifeOS API listening on port ${port}`, 'Bootstrap');
  Logger.log(`Swagger docs available at /api/docs`, 'Bootstrap');
}

bootstrap();
