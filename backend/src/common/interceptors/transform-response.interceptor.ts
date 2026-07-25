import { CallHandler, ExecutionContext, Injectable, NestInterceptor } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { SKIP_RESPONSE_TRANSFORM_KEY } from '../decorators/skip-response-transform.decorator';

interface Envelope<T> {
  data: T;
}

function isAlreadyEnveloped(value: unknown): value is Envelope<unknown> {
  return typeof value === 'object' && value !== null && 'data' in value;
}

/**
 * Ensures every successful response follows the envelope defined in
 * docs/15-api-design.md#request-and-response-formats: `{ data: ... }` for a
 * single resource, or `{ data: [...], meta: {...} }` for a paginated list.
 *
 * Services that already return a paginated `{ data, meta }` shape (per
 * docs/15-api-design.md#pagination) pass through unchanged; everything else
 * is wrapped in `{ data: value }` automatically, so individual controllers
 * don't each have to remember to do it. Routes marked
 * `@SkipResponseTransform()` (currently only `GET /health`) pass through
 * unwrapped entirely, for infra tooling that expects a flat shape.
 */
@Injectable()
export class TransformResponseInterceptor<T> implements NestInterceptor<T, Envelope<T> | T> {
  constructor(private readonly reflector: Reflector) {}

  intercept(context: ExecutionContext, next: CallHandler<T>): Observable<Envelope<T> | T> {
    const skip = this.reflector.getAllAndOverride<boolean>(SKIP_RESPONSE_TRANSFORM_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);

    return next.handle().pipe(
      map((value): Envelope<T> | T => {
        if (skip) return value;
        return isAlreadyEnveloped(value) ? (value as Envelope<T>) : { data: value };
      }),
    );
  }
}
