import { GenerateContentResponse } from '@google/genai';
import { AiGenerateResult } from '../interfaces/ai-provider.interface';
import { ProviderId } from '../types/ai.types';
import { buildAiGenerateResult } from './generate-result.util';

export interface GeminiResultMeta {
  providerId: ProviderId;
  model: string;
  latencyMs: number;
}

/**
 * Normalizes the Gemini SDK's `GenerateContentResponse` into the
 * provider-agnostic `AiGenerateResult` — the only place in this module
 * that reads a Gemini-specific response field directly.
 * `usageMetadata`'s token counts are the SDK's actual reported figures
 * (not a client-side estimate), reused as-is for `AiUsage`'s
 * `estimated*Tokens` fields (named generically since not every provider
 * necessarily returns exact counts — see `toOpenRouterGenerateResult` for
 * the same convention).
 */
export function toGeminiGenerateResult(
  response: GenerateContentResponse,
  meta: GeminiResultMeta,
): AiGenerateResult {
  return buildAiGenerateResult(
    response.text,
    {
      inputTokens: response.usageMetadata?.promptTokenCount ?? 0,
      outputTokens: response.usageMetadata?.candidatesTokenCount ?? 0,
    },
    meta,
  );
}
