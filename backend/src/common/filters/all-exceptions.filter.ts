import {
  ArgumentsHost,
  Catch,
  ExceptionFilter,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { Request, Response } from 'express';

/**
 * Translates every thrown error into the single error envelope defined in
 * docs/15-api-design.md#error-handling, so clients never have to handle more
 * than one error shape regardless of which layer (guard, pipe, service)
 * threw.
 */
@Catch()
export class AllExceptionsFilter implements ExceptionFilter {
  private readonly logger = new Logger(AllExceptionsFilter.name);

  catch(exception: unknown, host: ArgumentsHost): void {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();

    const isHttpException = exception instanceof HttpException;
    const statusCode = isHttpException ? exception.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;

    const httpExceptionResponse = isHttpException ? exception.getResponse() : null;
    const message = this.extractMessage(httpExceptionResponse, exception);
    const code = this.extractCode(httpExceptionResponse);

    if (!isHttpException) {
      this.logger.error(exception instanceof Error ? exception.stack : exception);
    }

    response.status(statusCode).json({
      statusCode,
      error: HttpStatus[statusCode] ?? 'Internal Server Error',
      // Optional machine-readable reason, only when an exception sets one
      // (e.g. `CHAT_DAILY_LIMIT_REACHED`), so a client can tell two errors
      // with the same status apart.
      ...(code && { code }),
      message,
      path: request.url,
      timestamp: new Date().toISOString(),
    });
  }

  private extractCode(httpExceptionResponse: string | object | null): string | undefined {
    if (httpExceptionResponse && typeof httpExceptionResponse === 'object') {
      const maybeCode = (httpExceptionResponse as { code?: unknown }).code;
      if (typeof maybeCode === 'string' && maybeCode.length > 0) {
        return maybeCode;
      }
    }
    return undefined;
  }

  private extractMessage(
    httpExceptionResponse: string | object | null,
    exception: unknown,
  ): string | string[] {
    if (httpExceptionResponse && typeof httpExceptionResponse === 'object') {
      const maybeMessage = (httpExceptionResponse as { message?: string | string[] }).message;
      if (maybeMessage) {
        return maybeMessage;
      }
    }
    if (typeof httpExceptionResponse === 'string') {
      return httpExceptionResponse;
    }
    return exception instanceof Error ? exception.message : 'Internal server error';
  }
}
