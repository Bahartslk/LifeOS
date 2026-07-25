import { HttpStatus } from '@nestjs/common';
import { ProviderId } from '../types/ai.types';
import {
  AiException,
  InvalidProviderResponseException,
  ProviderRateLimitException,
  ProviderUnavailableException,
} from './ai.exceptions';

/**
 * The one place the "HTTP status code -> AI exception type" mapping is
 * defined — both `GeminiProvider` (via the `@google/genai` SDK's
 * `ApiError.status`) and `OpenRouterProvider` (via `fetch`'s
 * `Response.status`) hit the identical 429/5xx/other-4xx classification,
 * so the logic lives here once instead of being duplicated per provider.
 * Each provider still owns *extracting* the status code from its own
 * SDK/response shape — only the classification itself is shared.
 */
export function classifyProviderHttpError(providerId: ProviderId, status: number): AiException {
  if (status === HttpStatus.TOO_MANY_REQUESTS) {
    return new ProviderRateLimitException(providerId);
  }
  if (status >= HttpStatus.INTERNAL_SERVER_ERROR) {
    return new ProviderUnavailableException(providerId);
  }
  return new InvalidProviderResponseException(
    providerId,
    `Provider rejected the request (HTTP ${status}).`,
  );
}
