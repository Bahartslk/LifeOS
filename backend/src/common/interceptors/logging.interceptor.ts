import { CallHandler, ExecutionContext, Injectable, Logger, NestInterceptor } from '@nestjs/common';
import { Request, Response } from 'express';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

/**
 * Logs method, path, status code, and duration for every request. This is
 * the minimum observability needed once more than one API instance is
 * running, per docs/12-project-architecture.md#scalability--production-readiness
 * ("failures can be attributed to a specific instance/request").
 */
@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  private readonly logger = new Logger('HTTP');

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const request = context.switchToHttp().getRequest<Request>();
    const response = context.switchToHttp().getResponse<Response>();
    const { method, originalUrl } = request;
    const startTime = Date.now();

    return next.handle().pipe(
      tap(() => {
        const durationMs = Date.now() - startTime;
        this.logger.log(`${method} ${originalUrl} ${response.statusCode} +${durationMs}ms`);
      }),
    );
  }
}
