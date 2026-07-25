import { SetMetadata } from '@nestjs/common';

export const SKIP_RESPONSE_TRANSFORM_KEY = 'skipResponseTransform';

/**
 * Opts a single route out of {@link import('../interceptors/transform-response.interceptor').TransformResponseInterceptor}'s
 * global `{ data: ... }` envelope. Reserved for endpoints consumed by
 * non-API-client infra tooling that expects a flat, predictable shape —
 * currently only `GET /health` (Docker healthcheck, load balancers, k8s
 * probes), not a general escape hatch for feature endpoints.
 */
export const SkipResponseTransform = (): MethodDecorator & ClassDecorator =>
  SetMetadata(SKIP_RESPONSE_TRANSFORM_KEY, true);
