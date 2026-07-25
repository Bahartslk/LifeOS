import { AiGenerateResult } from '../interfaces/ai-provider.interface';
import { ProviderId } from '../types/ai.types';
import { buildAiGenerateResult } from './generate-result.util';

export interface OpenRouterResultMeta {
  providerId: ProviderId;
  model: string;
  latencyMs: number;
}

interface OpenRouterChatCompletion {
  choices?: Array<{ message?: { content?: string } }>;
  usage?: { prompt_tokens?: number; completion_tokens?: number };
}

/**
 * Normalizes OpenRouter's OpenAI-compatible `chat/completions` response
 * body into the provider-agnostic `AiGenerateResult` — the only place in
 * this module that reads an OpenRouter-specific response field directly.
 * `usage.prompt_tokens`/`completion_tokens` are OpenRouter's actual
 * reported figures, reused as-is (see `toGeminiGenerateResult` for the
 * same convention).
 */
export function toOpenRouterGenerateResult(
  body: unknown,
  meta: OpenRouterResultMeta,
): AiGenerateResult {
  const completion = body as OpenRouterChatCompletion;
  return buildAiGenerateResult(
    completion.choices?.[0]?.message?.content,
    {
      inputTokens: completion.usage?.prompt_tokens ?? 0,
      outputTokens: completion.usage?.completion_tokens ?? 0,
    },
    meta,
  );
}
