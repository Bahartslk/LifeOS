import { z } from 'zod';
import { DATE_PATTERN } from '../../../common/utils/date-time.util';
import { AiCapabilityDefinition } from '../../ai/capabilities/capability.types';
import { GEMINI_PROVIDER_ID, OPENROUTER_PROVIDER_ID } from '../../ai/constants/ai.constants';

/**
 * Sprint 21 (Daily Brief Intelligence) — six cross-module AI capabilities,
 * built entirely on the existing execution engine (`AiCapabilityService.run`,
 * `ProviderRouter`, `ProviderRegistry`, `AiFallbackExecutor`,
 * `ContextBuilder`, `PromptBuilder`) — the same declarative-definition
 * pattern Sprint 19/20 established, just owned by a brand-new
 * `DailyBriefModule` instead of an existing business module.
 *
 * Every one of these uses `templateId: 'assistant'` and the default
 * `contextScope` (`{}` — both Planner and Travel loaded), the first
 * capabilities in the platform explicitly allowed to combine both, per
 * this sprint's "Daily Brief becomes the first feature that understands
 * the user's overall situation rather than a single module" goal. None of
 * these operate on one specific existing `Task`/`Trip` — unlike Sprint
 * 19/20's per-entity capabilities, every one here reasons only over the
 * caller's own aggregate context (already assembled by the unmodified
 * `ContextBuilder`), so no capability needs a `primaryInput` or an entity
 * lookup — `notes` is the only ever caller-supplied content.
 *
 * None of these write to `Task`/`TaskList`/`Trip`/`ItineraryItem` — every
 * `result` is a suggestion; `DailyBriefController`'s callers (the mobile
 * client) decide whether to act on any of it.
 */

const DAILY_SUMMARY_SCHEMA = z.object({
  todayPriorities: z.array(z.string().min(1)).min(1).max(5),
  keyEvents: z.array(z.string().min(1)).default([]),
  suggestedFocus: z.string().min(1),
});
export type DailySummaryResult = z.infer<typeof DAILY_SUMMARY_SCHEMA>;

export const DAILY_SUMMARY: AiCapabilityDefinition<DailySummaryResult> = {
  id: 'daily-brief-summary',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    "Combine the caller's Planner tasks, upcoming trips, and any important deadlines from the context above into one coherent summary of today's " +
    "agenda. Identify today's priorities, key events worth calling out, and a single suggested focus for the day. This is a suggestion only.",
  jsonShapeInstruction:
    '{"todayPriorities": string[], "keyEvents": string[], "suggestedFocus": string}',
  outputSchema: DAILY_SUMMARY_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.4,
  maxTokens: 500,
};

const CONFLICT_SCHEMA = z.object({
  description: z.string().min(1),
  items: z.array(z.string().min(1)).min(1),
});

const OVERLOADED_DAY_SCHEMA = z.object({
  date: z.string().regex(DATE_PATTERN),
  reason: z.string().min(1),
});

const SCHEDULE_CONFLICTS_SCHEMA = z.object({
  overlappingEvents: z.array(CONFLICT_SCHEMA).default([]),
  impossibleSchedules: z.array(z.string().min(1)).default([]),
  insufficientTravelTime: z.array(z.string().min(1)).default([]),
  overloadedDays: z.array(OVERLOADED_DAY_SCHEMA).default([]),
  recommendations: z.array(z.string().min(1)).min(1).max(8),
});
export type ScheduleConflictsResult = z.infer<typeof SCHEDULE_CONFLICTS_SCHEMA>;

export const SCHEDULE_CONFLICTS: AiCapabilityDefinition<ScheduleConflictsResult> = {
  id: 'daily-brief-schedule-conflicts',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    "Analyze the caller's Planner and Travel context above together. Detect overlapping events (a task and a trip/itinerary item competing for " +
    'the same time), impossible schedules, insufficient travel time between commitments, and overloaded days. Leave an array empty if you find ' +
    'nothing in that category — do not invent a conflict that is not actually supported by the data. Then generate 1 to 8 actionable recommendations.',
  jsonShapeInstruction:
    '{"overlappingEvents": [{"description": string, "items": string[]}], "impossibleSchedules": string[], "insufficientTravelTime": string[], "overloadedDays": [{"date": "YYYY-MM-DD", "reason": string}], "recommendations": string[]}',
  outputSchema: SCHEDULE_CONFLICTS_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.3,
  maxTokens: 700,
};

const PREPARATION_ITEM_SCHEMA = z.object({
  title: z.string().min(1),
  reason: z.string().min(1),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH']),
  relatedTo: z.enum(['PLANNER', 'TRAVEL', 'BOTH']),
});

const PREPARATION_SCHEMA = z.object({
  items: z.array(PREPARATION_ITEM_SCHEMA).min(1).max(10),
});
export type PreparationResult = z.infer<typeof PREPARATION_SCHEMA>;

export const PREPARATION_SUGGESTIONS: AiCapabilityDefinition<PreparationResult> = {
  id: 'daily-brief-preparation',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    "Based on the caller's upcoming Planner tasks and Travel trips above, generate 1 to 10 prioritized preparation recommendations — e.g. pack " +
    'luggage, prepare documents, finish work before departure, buy missing items. Tag each with whether it relates to Planner, Travel, or both.',
  jsonShapeInstruction:
    '{"items": [{"title": string, "reason": string, "priority": "LOW" | "MEDIUM" | "HIGH", "relatedTo": "PLANNER" | "TRAVEL" | "BOTH"}]}',
  outputSchema: PREPARATION_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.4,
  maxTokens: 700,
};

const UPCOMING_RISK_SCHEMA = z.object({
  category: z.enum(['DEADLINE', 'WORKLOAD', 'TRAVEL_CONFLICT', 'PREPARATION']),
  severity: z.enum(['LOW', 'MEDIUM', 'HIGH']),
  recommendation: z.string().min(1),
  reason: z.string().min(1),
});

const UPCOMING_RISK_ANALYSIS_SCHEMA = z.object({
  risks: z.array(UPCOMING_RISK_SCHEMA).max(10).default([]),
});
export type UpcomingRiskAnalysisResult = z.infer<typeof UPCOMING_RISK_ANALYSIS_SCHEMA>;

export const UPCOMING_RISK_ANALYSIS: AiCapabilityDefinition<UpcomingRiskAnalysisResult> = {
  id: 'daily-brief-upcoming-risks',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    "Analyze the caller's Planner and Travel context above, focusing only on the next 7 days. Identify deadline risks, workload imbalance, " +
    'travel conflicts, and missing preparation. For each risk found, give its category, severity, a recommendation, and the reason. Return an ' +
    'empty list if you find no meaningful risk in the next 7 days — do not invent one.',
  jsonShapeInstruction:
    '{"risks": [{"category": "DEADLINE" | "WORKLOAD" | "TRAVEL_CONFLICT" | "PREPARATION", "severity": "LOW" | "MEDIUM" | "HIGH", "recommendation": string, "reason": string}]}',
  outputSchema: UPCOMING_RISK_ANALYSIS_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.3,
  maxTokens: 800,
};

const DAILY_PRIORITY_SCHEMA = z.object({
  taskTitle: z.string().min(1),
  suggestedRank: z.number().int().positive(),
  reason: z.string().min(1),
});

const DAILY_PRIORITIES_SCHEMA = z.object({
  priorities: z.array(DAILY_PRIORITY_SCHEMA).min(1).max(5),
});
export type DailyPrioritiesResult = z.infer<typeof DAILY_PRIORITIES_SCHEMA>;

export const DAILY_PRIORITIES: AiCapabilityDefinition<DailyPrioritiesResult> = {
  id: 'daily-brief-priorities',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    "Recommend how the caller should rank today's existing tasks by priority, considering the Planner and Travel context above (e.g. an upcoming " +
    "trip today should outrank a low-priority task). This is a ranking RECOMMENDATION only — you are not changing any task's stored priority. " +
    '"suggestedRank" is a 1-based ordinal (1 = do first), not the stored priority field.',
  jsonShapeInstruction:
    '{"priorities": [{"taskTitle": string, "suggestedRank": number, "reason": string}]}',
  outputSchema: DAILY_PRIORITIES_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.3,
  maxTokens: 400,
};

const DAILY_MOTIVATION_SCHEMA = z.object({
  message: z.string().min(1).max(400),
});
export type DailyMotivationResult = z.infer<typeof DAILY_MOTIVATION_SCHEMA>;

export const DAILY_MOTIVATION: AiCapabilityDefinition<DailyMotivationResult> = {
  id: 'daily-brief-motivation',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    "Write one concise, friendly, motivational message for the caller based on today's actual workload from the Planner and Travel context above " +
    '— reference something specific and real from that context (e.g. a task, trip, or how busy/light the day is). Never a generic platitude that ' +
    'could apply to anyone on any day; it must clearly be about *this* day.',
  jsonShapeInstruction: '{"message": string}',
  outputSchema: DAILY_MOTIVATION_SCHEMA,
  preferredProvider: OPENROUTER_PROVIDER_ID,
  fallbackAllowed: true,
  // Motivation benefits from more variation and warmth than the analytical
  // capabilities above — a higher temperature helps avoid generic phrasing.
  temperature: 0.9,
  maxTokens: 150,
};
