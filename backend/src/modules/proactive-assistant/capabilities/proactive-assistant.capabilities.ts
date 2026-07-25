import { z } from 'zod';
import { DATE_PATTERN } from '../../../common/utils/date-time.util';
import { AiCapabilityDefinition } from '../../ai/capabilities/capability.types';
import { GEMINI_PROVIDER_ID, OPENROUTER_PROVIDER_ID } from '../../ai/constants/ai.constants';
import { InsightCategory } from '../dto/insight.dto';
import { OpportunityType } from '../dto/opportunity.dto';
import { ReminderRelatedTo } from '../dto/reminder-recommendation.dto';
import { SuggestionConfidence, SuggestionPriority } from '../dto/suggestion.dto';

/**
 * Sprint 22 (Proactive Assistant Engine) — four AI-backed capabilities built
 * entirely on the existing execution engine (`AiCapabilityService.run`,
 * `ProviderRouter`, `ProviderRegistry`, `AiFallbackExecutor`,
 * `ContextBuilder`, `PromptBuilder`), the same declarative-definition
 * pattern Sprint 19/20/21 established — nothing here duplicates that
 * infrastructure. Every one uses `templateId: 'assistant'` and the default
 * `contextScope` (`{}`, both Planner and Travel loaded), reusing the exact
 * template Sprint 21's Daily Brief capabilities already combine both with —
 * no new template or context pipeline was added for this sprint.
 *
 * This is the platform's first genuinely *proactive* capability set: every
 * `basePrompt` below explicitly asks the model to analyze the caller's
 * situation unprompted rather than answer a specific question, and every
 * result array uses `.max(N).default([])` — an empty list is a legitimate,
 * expected answer ("nothing to proactively suggest right now"), never
 * something to fabricate to avoid returning empty. `expiresAt`/
 * `recommendedDate` reuse `common/utils/date-time.util.ts`'s `DATE_PATTERN`
 * rather than redeclaring it, continuing Sprint 21.5's "AI schemas should
 * reuse the promoted date/time patterns, not redeclare them" fix. Each
 * enum's Zod schema is `z.nativeEnum(...)` over the exact TS enum its
 * response DTO already declares (`dto/*.ts`), so the AI JSON contract and
 * the Swagger-documented response shape can never drift apart into two
 * separately-maintained value lists.
 *
 * None of these write to `Task`/`TaskList`/`Trip`/`ItineraryItem`, create a
 * reminder, or send a notification — every result is a suggestion the
 * client decides whether to act on, per this sprint's "the assistant
 * remains suggestion-only" rule. A 5th capability, prioritization
 * (`POST /proactive-assistant/prioritize`), is deliberately NOT here — it's
 * a deterministic algorithm over caller-supplied data, not another AI call;
 * see `prioritization/suggestion-prioritizer.util.ts`.
 */

const SUGGESTION_ITEM_SCHEMA = z.object({
  title: z.string().min(1).max(200),
  description: z.string().min(1),
  reason: z.string().min(1),
  priority: z.nativeEnum(SuggestionPriority),
  confidence: z.nativeEnum(SuggestionConfidence),
  expiresAt: z.string().regex(DATE_PATTERN).nullable(),
});

const PROACTIVE_SUGGESTIONS_SCHEMA = z.object({
  suggestions: z.array(SUGGESTION_ITEM_SCHEMA).max(10).default([]),
});
export type ProactiveSuggestionsResult = z.infer<typeof PROACTIVE_SUGGESTIONS_SCHEMA>;

export const PROACTIVE_SUGGESTIONS: AiCapabilityDefinition<ProactiveSuggestionsResult> = {
  id: 'proactive-assistant-suggestions',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    "Proactively analyze the caller's Planner and Travel context above without being asked a specific question. Identify concrete opportunities " +
    'to help them right now — e.g. preparing for tomorrow, completing an urgent task, packing luggage for an upcoming trip, leaving earlier for a ' +
    "commitment, buying something they'll need, taking a break, or finishing work before a departure. Generate 0 to 10 suggestions; return an empty " +
    'list if nothing genuinely warrants one right now — never invent a suggestion to fill the list. Every suggestion is a recommendation only; ' +
    'nothing is ever created, modified, or scheduled automatically.',
  jsonShapeInstruction:
    '{"suggestions": [{"title": string, "description": string, "reason": string, "priority": "LOW" | "MEDIUM" | "HIGH", "confidence": "LOW" | "MEDIUM" | "HIGH", "expiresAt": "YYYY-MM-DD" | null}]}',
  outputSchema: PROACTIVE_SUGGESTIONS_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.4,
  maxTokens: 700,
};

const OPPORTUNITY_SCHEMA = z.object({
  title: z.string().min(1).max(200),
  description: z.string().min(1),
  type: z.nativeEnum(OpportunityType),
  reason: z.string().min(1),
});

const OPPORTUNITY_DETECTION_SCHEMA = z.object({
  opportunities: z.array(OPPORTUNITY_SCHEMA).max(10).default([]),
});
export type OpportunityDetectionResult = z.infer<typeof OPPORTUNITY_DETECTION_SCHEMA>;

export const OPPORTUNITY_DETECTION: AiCapabilityDefinition<OpportunityDetectionResult> = {
  id: 'proactive-assistant-opportunities',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    "Analyze the caller's Planner and Travel context above for positive opportunities, not just problems — e.g. a free afternoon, an unusually " +
    'light workload, travel preparation that is already complete, or a good time to study or relax. Only report an opportunity genuinely supported ' +
    'by the data above; leave the list empty if nothing stands out rather than inventing one.',
  jsonShapeInstruction:
    '{"opportunities": [{"title": string, "description": string, "type": "FREE_TIME" | "LOW_WORKLOAD" | "PREPARATION_COMPLETE" | "GOOD_TIME_TO_STUDY" | "GOOD_TIME_TO_RELAX" | "OTHER", "reason": string}]}',
  outputSchema: OPPORTUNITY_DETECTION_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.5,
  maxTokens: 600,
};

const REMINDER_RECOMMENDATION_SCHEMA = z.object({
  title: z.string().min(1).max(200),
  reason: z.string().min(1),
  relatedTo: z.nativeEnum(ReminderRelatedTo),
  recommendedDate: z.string().regex(DATE_PATTERN).nullable(),
});

const REMINDER_RECOMMENDATIONS_SCHEMA = z.object({
  reminders: z.array(REMINDER_RECOMMENDATION_SCHEMA).max(10).default([]),
});
export type ReminderRecommendationsResult = z.infer<typeof REMINDER_RECOMMENDATIONS_SCHEMA>;

export const REMINDER_RECOMMENDATIONS: AiCapabilityDefinition<ReminderRecommendationsResult> = {
  id: 'proactive-assistant-reminders',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    'Recommend reminders the caller might want to set, based on the Planner and Travel context above — e.g. a reminder before a flight, before a ' +
    "task's deadline, or before a meeting. You are recommending reminders only; you never create, schedule, or send one yourself — that decision " +
    'and action always belongs to the caller. Leave the list empty if nothing upcoming warrants a reminder.',
  jsonShapeInstruction:
    '{"reminders": [{"title": string, "reason": string, "relatedTo": "PLANNER" | "TRAVEL" | "BOTH", "recommendedDate": "YYYY-MM-DD" | null}]}',
  outputSchema: REMINDER_RECOMMENDATIONS_SCHEMA,
  preferredProvider: OPENROUTER_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.3,
  maxTokens: 500,
};

const INSIGHT_SCHEMA = z.object({
  insight: z.string().min(1).max(300),
  category: z.nativeEnum(InsightCategory),
});

const SMART_INSIGHTS_SCHEMA = z.object({
  insights: z.array(INSIGHT_SCHEMA).max(5).default([]),
});
export type SmartInsightsResult = z.infer<typeof SMART_INSIGHTS_SCHEMA>;

export const SMART_INSIGHTS: AiCapabilityDefinition<SmartInsightsResult> = {
  id: 'proactive-assistant-insights',
  templateId: 'assistant',
  feature: 'assistant',
  contextScope: {},
  basePrompt:
    "Generate concise, non-generic insights about the caller's own situation, based on the Planner and Travel context above — e.g. a real pattern " +
    "in how they schedule work relative to travel, or a genuine observation about this week's workload. Each insight must be short, specific to " +
    'the data above, and actionable — never a generic statement that could apply to any user on any day. Leave the list empty rather than ' +
    'inventing an insight the data does not support.',
  jsonShapeInstruction:
    '{"insights": [{"insight": string, "category": "PATTERN" | "WORKLOAD" | "TRAVEL" | "SCHEDULING" | "GENERAL"}]}',
  outputSchema: SMART_INSIGHTS_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.5,
  maxTokens: 400,
};
