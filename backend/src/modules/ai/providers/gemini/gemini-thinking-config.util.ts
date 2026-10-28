import { ThinkingConfig, ThinkingLevel } from '@google/genai';

/**
 * Gemini 2.5 Flash and Flash-Lite (including their dated previews, with or
 * without the `models/` prefix) turn thinking off with a `thinkingBudget`
 * of 0.
 */
const THINKING_BUDGET_OFF_MODEL_PATTERN =
  /^(?:models\/)?gemini-2\.5-flash(?:-lite)?(?:-preview-\d{2}-\d{4})?$/;

/**
 * Gemini 3.x Flash (`gemini-3-flash`, `gemini-3.5-flash`, ...) thinks by
 * default and uses `thinkingLevel` instead of a budget; `MINIMAL` is the
 * lowest level. Flash-Lite 3.x doesn't think, and preview, `-image` and
 * `-tts` variants are left alone.
 */
const THINKING_LEVEL_MINIMAL_MODEL_PATTERN = /^(?:models\/)?gemini-3(?:\.\d+)?-flash$/;

/**
 * Keeps thinking as low as each model allows. Every AI capability here asks
 * for a short JSON answer with a small `maxTokens` budget, and a thinking
 * model spends that budget on thoughts first, cutting the JSON off. Returns
 * `undefined` for any other model so no thinking setting is sent at all
 * (2.5 Pro, for example, rejects a budget of 0).
 */
export function thinkingConfigFor(model: string): ThinkingConfig | undefined {
  if (THINKING_BUDGET_OFF_MODEL_PATTERN.test(model)) {
    return { thinkingBudget: 0 };
  }
  if (THINKING_LEVEL_MINIMAL_MODEL_PATTERN.test(model)) {
    return { thinkingLevel: ThinkingLevel.MINIMAL };
  }
  return undefined;
}
