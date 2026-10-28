import { ThinkingConfig } from '@google/genai';

/**
 * Gemini 2.5 Flash and Flash-Lite (including their dated previews, with or
 * without the `models/` prefix) are the models that accept a
 * `thinkingBudget` of 0. Other models either reject it (2.5 Pro cannot turn
 * thinking off) or use a different setting (Gemini 3 uses `thinkingLevel`),
 * and specialized variants (`-image`, `-tts`, ...) don't think at all.
 */
const THINKING_BUDGET_OFF_MODEL_PATTERN =
  /^(?:models\/)?gemini-2\.5-flash(?:-lite)?(?:-preview-\d{2}-\d{4})?$/;

/**
 * Turns thinking off for the models that support it. Every AI capability
 * here asks for a short JSON answer with a small `maxTokens` budget, and a
 * thinking model spends that budget on thoughts first, cutting the JSON
 * off. Returns `undefined` for any other model so the setting is not sent
 * at all.
 */
export function thinkingConfigFor(model: string): ThinkingConfig | undefined {
  return THINKING_BUDGET_OFF_MODEL_PATTERN.test(model) ? { thinkingBudget: 0 } : undefined;
}
