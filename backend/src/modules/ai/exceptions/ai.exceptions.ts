import { HttpException, HttpStatus } from '@nestjs/common';
import { ProviderId } from '../types/ai.types';

/**
 * Base for every AI-layer exception. Every subclass's message carries only
 * a provider id and a short, generic reason — never a raw provider error,
 * API key, prompt, or user context (see this module's Security notes;
 * `GeminiProvider.normalizeError` logs the real error server-side, then
 * throws one of these instead of rethrowing it).
 *
 * Extending `HttpException` — the same base every other module's
 * exceptions already use (`NotFoundException`, `BadRequestException`,
 * `ConflictException`, ...) — means a future controller that calls
 * `AiService.generate()` gets a correctly-structured error response from
 * the existing global `AllExceptionsFilter` for free; no new error-handling
 * wiring is needed when a later sprint adds one.
 */
export abstract class AiException extends HttpException {}

/** The resolved/requested provider isn't registered, isn't enabled, or `isAvailable()` returned false (e.g. no API key configured). Also thrown for a provider-side 5xx / outage — a transient failure `AiRetryExecutor` retries. */
export class ProviderUnavailableException extends AiException {
  constructor(providerId: ProviderId) {
    super(`AI provider "${providerId}" is not available.`, HttpStatus.SERVICE_UNAVAILABLE);
  }
}

/** The provider call exceeded its configured timeout — a transient failure `AiRetryExecutor` retries. */
export class ProviderTimeoutException extends AiException {
  constructor(providerId: ProviderId) {
    super(`AI provider "${providerId}" timed out.`, HttpStatus.GATEWAY_TIMEOUT);
  }
}

/** The provider rejected the request for exceeding its own rate limit. Deliberately NOT retried by `AiRetryExecutor` — per docs/12-project-architecture.md#error-handling, "not silently retried in a way that could compound the limit." */
export class ProviderRateLimitException extends AiException {
  constructor(providerId: ProviderId) {
    super(`AI provider "${providerId}" rate limit exceeded.`, HttpStatus.TOO_MANY_REQUESTS);
  }
}

/** The provider returned a response this module couldn't use (malformed, empty, or a non-429 4xx rejection of the request). Not retried — a template/context bug, not a transient failure, per docs/12's "logged as an internal defect ... not retried." */
export class InvalidProviderResponseException extends AiException {
  constructor(providerId: ProviderId, reason: string) {
    super(
      `AI provider "${providerId}" returned an invalid response: ${reason}`,
      HttpStatus.BAD_GATEWAY,
    );
  }
}

/** A misconfiguration this module can't route around — e.g. an unknown prompt template id, or a duplicate provider id registered twice. */
export class AiConfigurationException extends AiException {
  constructor(reason: string) {
    super(`AI configuration error: ${reason}`, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}

/**
 * Every candidate provider in `AiFallbackExecutor`'s resolved chain failed
 * (each after exhausting its own `AiRetryExecutor` retries) — Sprint 18B's
 * "fallback exhausted" case. Thrown instead of re-surfacing the last
 * provider's own exception, so a caller/log can tell "every provider is
 * down" apart from "the one provider is down".
 */
export class ProviderFallbackExhaustedException extends AiException {
  constructor(attemptedProviderIds: ProviderId[]) {
    super(
      `All AI providers failed: ${attemptedProviderIds.join(', ')}.`,
      HttpStatus.SERVICE_UNAVAILABLE,
    );
  }
}
