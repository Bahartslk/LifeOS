import { RequestMethod, ValidationPipe, VersioningType } from '@nestjs/common';
import { CorsOptions } from '@nestjs/common/interfaces/external/cors-options.interface';
import { ConfigService } from '@nestjs/config';
import { NestExpressApplication } from '@nestjs/platform-express';
import compression from 'compression';
import helmet from 'helmet';
import { AppConfig } from './config/configuration';

/**
 * Cross-origin policy for browser clients: only the explicitly allowed
 * origins (the admin panel; Vite's dev servers outside production), and
 * no cross-origin access at all when the list is empty. Tokens travel in
 * the `Authorization` header, never in cookies, so credentials stay off.
 * Native mobile clients send no `Origin` and are unaffected; Swagger UI is
 * served from the API's own origin.
 */
export function buildCorsOptions(allowedOrigins: string[]): CorsOptions {
  return {
    origin: allowedOrigins.length > 0 ? allowedOrigins : false,
    methods: ['GET', 'POST', 'PATCH', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Authorization', 'Content-Type'],
    credentials: false,
  };
}

/**
 * Every HTTP-level setting of the application, shared by `main.ts` and the
 * HTTP-level tests so tests exercise the exact production pipeline
 * (validation, prefix, versioning, CORS, proxy trust).
 */
export function configureApp(
  app: NestExpressApplication,
  configService: ConfigService<AppConfig, true>,
): void {
  // Behind Railway's reverse proxy the socket address is the proxy's, not
  // the client's. Trusting the configured number of hops makes `req.ip`
  // the real client IP, which per-IP rate limiting depends on.
  app.set('trust proxy', configService.get('http.trustProxyHops', { infer: true }));

  app.use(helmet());
  app.use(compression());

  app.enableCors(buildCorsOptions(configService.get('http.corsAllowedOrigins', { infer: true })));

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
}
