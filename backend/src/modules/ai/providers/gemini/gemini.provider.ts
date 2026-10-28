import { HttpStatus, Injectable, Logger } from '@nestjs/common';
import { ApiError, GenerateContentResponse, GoogleGenAI } from '@google/genai';
import { AiConfigService } from '../../config/ai-config.service';
import { GEMINI_PROVIDER_ID } from '../../constants/ai.constants';
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
import { toGeminiGenerateResult } from '../../mapper/gemini-response.mapper';
import { withTimeout } from '../with-timeout.util';
import { thinkingConfigFor } from './gemini-thinking-config.util';

/**
 * The only file in this module that imports `@google/genai` — per
 * docs/12-project-architecture.md#ai-service-boundaries, "the GeminiProvider
 * adapter is the only component that imports the Gemini SDK/client."
 * Everything else in `ai/` talks to the `AiProvider` interface, never to
 * this class or an SDK type directly. Read secrets only through
 * `AiConfigService` (itself a thin wrapper over `ConfigService`) — never
 * `process.env` here.
 */
@Injectable()
export class GeminiProvider implements AiProvider {
  readonly id = GEMINI_PROVIDER_ID;
  readonly displayName = 'Google Gemini';

  private readonly logger = new Logger(GeminiProvider.name);
  private client: GoogleGenAI | null = null;

  constructor(private readonly aiConfig: AiConfigService) {}

  /** No API key configured — the app still boots fine (see `env.validation.ts`'s doc comment); this just keeps `ProviderRegistry`/`ProviderRouter` from ever routing to an unusable provider. */
  isAvailable(): boolean {
    return Boolean(this.aiConfig.geminiApiKey);
  }

  async generate(request: AiGenerateRequest): Promise<AiGenerateResult> {
    if (!this.isAvailable()) {
      throw new ProviderUnavailableException(this.id);
    }

    const models = this.aiConfig.modelChain;
    const startedAt = Date.now();

    for (const [index, model] of models.entries()) {
      try {
        const response = await this.generateWithModel(model, request);
        return toGeminiGenerateResult(response, {
          providerId: this.id,
          model,
          latencyMs: Date.now() - startedAt,
        });
      } catch (error) {
        const next = models[index + 1];
        if (next && this.shouldTryNextModel(error)) {
          this.logger.warn(
            `Gemini model "${model}" answered ${(error as ApiError).status}; trying "${next}".`,
          );
          continue;
        }
        throw this.normalizeError(error);
      }
    }
    // Unreachable: the chain always holds at least `AI_MODEL`, and the last
    // model either returns or throws above.
    throw new ProviderUnavailableException(this.id);
  }

  private generateWithModel(
    model: string,
    request: AiGenerateRequest,
  ): Promise<GenerateContentResponse> {
    const thinkingConfig = thinkingConfigFor(model);
    return withTimeout(
      (signal) =>
        this.getClient().models.generateContent({
          model,
          contents: [{ role: 'user', parts: [{ text: request.userPrompt }] }],
          config: {
            systemInstruction: request.systemPrompt,
            temperature: request.options?.temperature ?? this.aiConfig.temperature,
            topP: request.options?.topP ?? this.aiConfig.topP,
            topK: request.options?.topK ?? this.aiConfig.topK,
            maxOutputTokens: request.options?.maxTokens ?? this.aiConfig.maxTokens,
            ...(thinkingConfig && { thinkingConfig }),
            abortSignal: signal,
          },
        }),
      request.options?.timeoutMs ?? this.aiConfig.timeoutMs,
    );
  }

  /**
   * Only a used-up quota (429) or an overloaded model (503) moves on to the
   * next model: each Gemini model has its own free-tier quota and load.
   * Any other failure (a timeout, a rejected request) would most likely
   * repeat on the next model too, so it is not passed down the chain.
   */
  private shouldTryNextModel(error: unknown): boolean {
    return (
      error instanceof ApiError &&
      (error.status === HttpStatus.TOO_MANY_REQUESTS ||
        error.status === HttpStatus.SERVICE_UNAVAILABLE)
    );
  }

  private getClient(): GoogleGenAI {
    if (!this.client) {
      this.client = new GoogleGenAI({ apiKey: this.aiConfig.geminiApiKey });
    }
    return this.client;
  }

  /**
   * Never rethrows the raw SDK error — logs it server-side for debugging
   * (this module's Logging rule: internal logs may capture it, but nothing
   * derived from it reaches a caller), then throws one of this module's own
   * exceptions carrying only a provider id and a generic, non-sensitive
   * reason (see this module's Security notes). Passes an already-classified
   * `AiException` (e.g. from `toGeminiGenerateResult`'s empty-text check)
   * through unchanged instead of re-wrapping it.
   */
  private normalizeError(error: unknown): Error {
    if (error instanceof AiException) {
      return error;
    }

    this.logger.error(
      `Gemini request failed: ${error instanceof Error ? error.message : 'unknown error'}`,
    );

    if (error instanceof Error && error.name === 'AbortError') {
      return new ProviderTimeoutException(this.id);
    }
    if (error instanceof ApiError) {
      return classifyProviderHttpError(this.id, error.status);
    }
    return new ProviderUnavailableException(this.id);
  }
}
