import { ApiProperty } from '@nestjs/swagger';

/**
 * Shape of GET /health, per Sprint 13.0's "status, timestamp, version"
 * requirement. Deliberately excluded from the global response envelope
 * ({@link import('../../common/interceptors/transform-response.interceptor').TransformResponseInterceptor})
 * scope concern — health checks are consumed by infra tooling (Docker,
 * load balancers), not API clients, so this stays a flat, predictable shape.
 */
export class HealthResponseDto {
  @ApiProperty({ type: String, example: 'ok' })
  status = 'ok' as const;

  @ApiProperty({ example: '2026-07-20T12:00:00.000Z' })
  timestamp!: string;

  @ApiProperty({ example: '0.1.0' })
  version!: string;
}
