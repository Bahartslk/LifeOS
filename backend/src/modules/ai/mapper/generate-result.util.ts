import { AiGenerateResult } from '../interfaces/ai-provider.interface';
import { InvalidProviderResponseException } from '../exceptions/ai.exceptions';
import { ProviderId } from '../types/ai.types';

export interface GenerateResultMeta {
  providerId: ProviderId;
  model: string;
  latencyMs: number;
}

export interface RawUsage {
  inputTokens: number;
  outputTokens: number;
}

/**
 * Assembles the provider-agnostic `AiGenerateResult` — both
 * `gemini-response.mapper.ts` and `openrouter-response.mapper.ts` extract
 * their SDK/REST-specific `text`/usage fields and hand them here, rather
 * than each independently constructing the same result shape and
 * repeating the same "empty text is invalid" check.
 */
export function buildAiGenerateResult(
  text: string | undefined | null,
  usage: RawUsage,
  meta: GenerateResultMeta,
): AiGenerateResult {
  if (!text) {
    throw new InvalidProviderResponseException(meta.providerId, 'Empty response text.');
  }

  return {
    text,
    providerId: meta.providerId,
    model: meta.model,
    usage: {
      estimatedInputTokens: usage.inputTokens,
      estimatedOutputTokens: usage.outputTokens,
    },
    latencyMs: meta.latencyMs,
  };
}
