import { z } from 'zod';
import { GEMINI_PROVIDER_ID, OPENROUTER_PROVIDER_ID } from '../constants/ai.constants';
import { AiCapabilityDefinition } from './capability.types';

/**
 * The four capability-based endpoints this platform implements instead of
 * a generic chatbot, per Sprint 18B's explicit "the objective is NOT to
 * build a generic chatbot" design rule. Each backs exactly one `POST
 * /ai/...` route in `ai.controller.ts`. `outputSchema` is the runtime
 * contract `response-validation.util.ts` enforces before a provider's
 * response ever reaches a caller — a non-empty `.min(1)` string, and
 * `.min(1).max(3)` on every suggestions array to match what
 * `basePrompt`/`jsonShapeInstruction` already ask the model for, so a
 * technically-valid-but-useless response (e.g. zero suggestions) is
 * rejected as `InvalidProviderResponseException`, not silently returned.
 */

const PLANNER_ANALYZE_SCHEMA = z.object({
  summary: z.string().min(1),
  insights: z.array(z.string().min(1)),
  riskFlags: z.array(z.string().min(1)),
});
export type PlannerAnalyzeResult = z.infer<typeof PLANNER_ANALYZE_SCHEMA>;

export const PLANNER_ANALYZE: AiCapabilityDefinition<PlannerAnalyzeResult> = {
  id: 'planner-analyze',
  templateId: 'planner',
  feature: 'planner',
  contextScope: { includeTravel: false },
  basePrompt:
    "Analyze the caller's current Planner data: identify patterns, overdue risk, and workload balance across today's and upcoming tasks.",
  jsonShapeInstruction: '{"summary": string, "insights": string[], "riskFlags": string[]}',
  outputSchema: PLANNER_ANALYZE_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  // Analytical, not creative — a lower temperature favors a consistent,
  // grounded read of the same task data over varied phrasing.
  temperature: 0.3,
  maxTokens: 600,
};

const TASK_SUGGESTION_SCHEMA = z.object({
  title: z.string().min(1),
  reason: z.string().min(1),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH']),
});

const PLANNER_SUGGEST_SCHEMA = z.object({
  suggestions: z.array(TASK_SUGGESTION_SCHEMA).min(1).max(3),
});
export type PlannerSuggestResult = z.infer<typeof PLANNER_SUGGEST_SCHEMA>;

export const PLANNER_SUGGEST: AiCapabilityDefinition<PlannerSuggestResult> = {
  id: 'planner-suggest',
  templateId: 'planner',
  feature: 'planner',
  contextScope: { includeTravel: false },
  basePrompt:
    'Suggest 1 to 3 new tasks that would help the caller, based on their current Planner data. Do not repeat a task that already exists.',
  jsonShapeInstruction:
    '{"suggestions": [{"title": string, "reason": string, "priority": "LOW" | "MEDIUM" | "HIGH"}]}',
  outputSchema: PLANNER_SUGGEST_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  // Suggesting new tasks is a creative-ish judgment call — a mid-range
  // temperature avoids both overly generic and overly random suggestions.
  temperature: 0.8,
  maxTokens: 500,
};

const TRAVEL_SUGGESTION_SCHEMA = z.object({
  title: z.string().min(1),
  description: z.string().min(1),
  type: z.string().min(1),
});

const TRAVEL_SUGGEST_SCHEMA = z.object({
  suggestions: z.array(TRAVEL_SUGGESTION_SCHEMA).min(1).max(3),
});
export type TravelSuggestResult = z.infer<typeof TRAVEL_SUGGEST_SCHEMA>;

export const TRAVEL_SUGGEST: AiCapabilityDefinition<TravelSuggestResult> = {
  id: 'travel-suggest',
  templateId: 'travel',
  feature: 'travel',
  contextScope: { includePlanner: false },
  basePrompt:
    "Suggest 1 to 3 relevant travel ideas or itinerary items, based on the caller's current Travel data (their upcoming/active trips and destinations).",
  jsonShapeInstruction:
    '{"suggestions": [{"title": string, "description": string, "type": string}]}',
  outputSchema: TRAVEL_SUGGEST_SCHEMA,
  preferredProvider: OPENROUTER_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.8,
  maxTokens: 500,
};

const DAILY_BRIEF_SCHEMA = z.object({
  greeting: z.string().min(1),
  summary: z.string().min(1),
  focusItems: z.array(z.string().min(1)),
});
export type DailyBriefResult = z.infer<typeof DAILY_BRIEF_SCHEMA>;

export const DAILY_BRIEF: AiCapabilityDefinition<DailyBriefResult> = {
  id: 'daily-brief',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    "Write a short, personalized daily brief combining the caller's Planner and Travel data for today.",
  jsonShapeInstruction: '{"greeting": string, "summary": string, "focusItems": string[]}',
  outputSchema: DAILY_BRIEF_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  // A warm, personable tone benefits from more variation than the
  // analytical planner-analyze capability above.
  temperature: 0.7,
  maxTokens: 400,
};
