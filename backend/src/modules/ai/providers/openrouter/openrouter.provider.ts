import { Injectable, Logger } from '@nestjs/common';
import { AiConfigService } from '../../config/ai-config.service';
import { OPENROUTER_PROVIDER_ID } from '../../constants/ai.constants';
import {
  AiException,
  ProviderTimeoutException,
  ProviderUnavailableException,
} from '../../exceptions/ai.exceptions';
import { classifyProviderHttpError } from '../../exceptions/classify-provider-http-error.util';
import {
  AiGenerateRequest,
  AiGenerateResult,
  AiProvider,
} from '../../interfaces/ai-provider.interface';
import { toOpenRouterGenerateResult } from '../../mapper/openrouter-response.mapper';
import { withTimeout } from '../with-timeout.util';

const OPENROUTER_ENDPOINT = 'https://openrouter.ai/api/v1/chat/completions';

/**
 * Sprint 18B's second `AiProvider` — proves the architecture built in
 * Sprint 18A is genuinely provider-agnostic: registered the same way as
 * `GeminiProvider` (a normal Nest provider collected by `ai.module.ts`'s
 * `AI_PROVIDERS` factory), no change to `ProviderRegistry`, `ProviderRouter`,
 * `AiService`, `ContextBuilder`, or `PromptBuilder`.
 *
 * OpenRouter exposes an OpenAI-compatible `chat/completions` REST endpoint
 * — called via the runtime's native `fetch` (Node 20+, already the Docker
 * base image) rather than adding a dedicated SDK dependency, unlike
 * `GeminiProvider`'s use of `@google/genai` (there's no comparable
 * "official Node SDK" for OpenRouter the way there is for Gemini). This is
 * the only file in the module that talks to OpenRouter's API. Read secrets
 * only through `AiConfigService` — never `process.env` here.
 */
@Injectable()
export class OpenRouterProvider implements AiProvider {
  readonly id = OPENROUTER_PROVIDER_ID;
  readonly displayName = 'OpenRouter';

  private readonly logger = new Logger(OpenRouterProvider.name);

  constructor(private readonly aiConfig: AiConfigService) {}

  /** No API key configured — the app still boots fine (see `env.validation.ts`'s doc comment); this just keeps `ProviderRegistry`/`ProviderRouter` from ever routing to an unusable provider. */
  isAvailable(): boolean {
    return Boolean(this.aiConfig.openRouterApiKey);
  }

  async generate(request: AiGenerateRequest): Promise<AiGenerateResult> {
    if (!this.isAvailable()) {
      throw new ProviderUnavailableException(this.id);
    }

    const model = this.aiConfig.openRouterModel;
    const timeoutMs = request.options?.timeoutMs ?? this.aiConfig.timeoutMs;
    const startedAt = Date.now();

    try {
      const response = await withTimeout(
        (signal) =>
          fetch(OPENROUTER_ENDPOINT, {
            method: 'POST',
            headers: {
              Authorization: `Bearer ${this.aiConfig.openRouterApiKey}`,
              'Content-Type': 'application/json',
            },
            signal,
            body: JSON.stringify({
              model,
              messages: [
                { role: 'system', content: request.systemPrompt },
                { role: 'user', content: request.userPrompt },
              ],
              temperature: request.options?.temperature ?? this.aiConfig.temperature,
              top_p: request.options?.topP ?? this.aiConfig.topP,
              max_tokens: request.options?.maxTokens ?? this.aiConfig.maxTokens,
            }),
          }),
        timeoutMs,
      );

      if (!response.ok) {
        await response.text().catch(() => undefined);
        throw classifyProviderHttpError(this.id, response.status);
      }

      const body: unknown = await response.json();
      return toOpenRouterGenerateResult(body, {
        providerId: this.id,
        model,
        latencyMs: Date.now() - startedAt,
      });
    } catch (error) {
      throw this.normalizeError(error);
    }
  }

  /**
   * Never rethrows the raw error — logs it server-side for debugging, then
   * throws one of this module's own exceptions carrying only a provider id
   * and a generic, non-sensitive reason (see this module's Security
   * notes). Passes an already-classified `AiException` (e.g. from
   * `classifyProviderHttpError`, thrown inside the same `try` block above)
   * through unchanged instead of re-wrapping it. An error that's neither a
   * classified `AiException` nor a timeout — a raw network failure (DNS,
   * connection reset, ...) — is treated as `ProviderUnavailableException`
   * (transient, retried, eligible for fallback) rather than
   * `InvalidProviderResponseException` (which would never be retried):
   * a network-level failure is far more likely to be a transient blip
   * than a malformed response.
   */
  private normalizeError(error: unknown): Error {
    if (error instanceof AiException) {
      return error;
    }

    this.logger.error(
      `OpenRouter request failed: ${error instanceof Error ? error.message : 'unknown error'}`,
    );

    if (error instanceof Error && error.name === 'AbortError') {
      return new ProviderTimeoutException(this.id);
    }
    return new ProviderUnavailableException(this.id);
  }
}
