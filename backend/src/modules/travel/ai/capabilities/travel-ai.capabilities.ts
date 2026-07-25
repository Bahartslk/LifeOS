import { z } from 'zod';
import { DATE_PATTERN } from '../../../../common/utils/date-time.util';
import { AiCapabilityDefinition } from '../../../ai/capabilities/capability.types';
import { GEMINI_PROVIDER_ID, OPENROUTER_PROVIDER_ID } from '../../../ai/constants/ai.constants';

/**
 * Sprint 20 (Intelligent Travel) — six Travel-owned AI capabilities, built
 * entirely on the existing execution engine (`AiCapabilityService.run`,
 * `ProviderRouter`, `ProviderRegistry`, `AiFallbackExecutor`,
 * `ContextBuilder`, `PromptBuilder`) — the exact same pattern Sprint 19
 * established for `modules/planner/ai/capabilities/planner-ai.capabilities.ts`,
 * just owned by (and living inside) `TravelModule` instead.
 *
 * None of these write to `Trip`/`ItineraryItem` — every `result` is a
 * draft or suggestion; `TravelAiController`'s callers (the mobile client)
 * decide whether to turn one into a real `POST/PATCH /travel/trips` or
 * `/travel/trips/:id/itinerary` call.
 */

const BUDGET_ESTIMATE_SCHEMA = z.object({
  estimate: z.number().nonnegative().nullable(),
  currency: z.string().min(1),
  uncertaintyNote: z.string().min(1).nullable(),
});

const TRIP_DRAFT_SCHEMA = z.object({
  destination: z.string().min(1),
  country: z.string().min(1).nullable(),
  startDate: z.string().regex(DATE_PATTERN).nullable(),
  endDate: z.string().regex(DATE_PATTERN).nullable(),
  estimatedDurationDays: z.number().int().positive().nullable(),
  travelPurpose: z.string().min(1),
  estimatedBudgetRange: BUDGET_ESTIMATE_SCHEMA.nullable(),
  suggestedTravelStyle: z.string().min(1),
  confidence: z.enum(['LOW', 'MEDIUM', 'HIGH']),
});
export type TripDraftResult = z.infer<typeof TRIP_DRAFT_SCHEMA>;

export const TRIP_DRAFT: AiCapabilityDefinition<TripDraftResult> = {
  id: 'travel-trip-draft',
  templateId: 'general',
  feature: 'travel',
  contextScope: { includePlanner: false, includeTravel: false },
  basePrompt:
    "Read the caller's natural-language trip description (given below as Input) and turn it into a single structured trip draft. " +
    'If a start/end date, budget, or any other field is not stated or clearly implied, use `null` rather than guessing a specific value; ' +
    'reflect how much you had to guess in the "confidence" field. This is a DRAFT only — it will be shown to the user for review before anything is saved.',
  jsonShapeInstruction:
    '{"destination": string, "country": string | null, "startDate": "YYYY-MM-DD" | null, "endDate": "YYYY-MM-DD" | null, "estimatedDurationDays": number | null, "travelPurpose": string, "estimatedBudgetRange": {"estimate": number | null, "currency": string, "uncertaintyNote": string | null} | null, "suggestedTravelStyle": string, "confidence": "LOW" | "MEDIUM" | "HIGH"}',
  outputSchema: TRIP_DRAFT_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.3,
  maxTokens: 500,
};

const ITINERARY_DAY_SCHEMA = z.object({
  day: z.number().int().positive(),
  date: z.string().regex(DATE_PATTERN).nullable(),
  morning: z.string().min(1),
  afternoon: z.string().min(1),
  evening: z.string().min(1),
});

const ITINERARY_GENERATION_SCHEMA = z.object({
  days: z.array(ITINERARY_DAY_SCHEMA).min(1).max(30),
});
export type ItineraryGenerationResult = z.infer<typeof ITINERARY_GENERATION_SCHEMA>;

export const ITINERARY_GENERATION: AiCapabilityDefinition<ItineraryGenerationResult> = {
  id: 'travel-itinerary-generation',
  templateId: 'general',
  feature: 'travel',
  contextScope: { includePlanner: false, includeTravel: false },
  basePrompt:
    'Generate a day-by-day itinerary for the trip described below (given as Input), covering every day of the trip exactly once. ' +
    'For each day, suggest a Morning, Afternoon, and Evening activity or plan. These are suggestions only — never claim anything has been booked or confirmed.',
  jsonShapeInstruction:
    '{"days": [{"day": number, "date": "YYYY-MM-DD" | null, "morning": string, "afternoon": string, "evening": string}]}',
  outputSchema: ITINERARY_GENERATION_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.6,
  maxTokens: 1200,
};

const BUDGET_ANALYSIS_SCHEMA = z.object({
  accommodation: BUDGET_ESTIMATE_SCHEMA,
  transportation: BUDGET_ESTIMATE_SCHEMA,
  food: BUDGET_ESTIMATE_SCHEMA,
  activities: BUDGET_ESTIMATE_SCHEMA,
  miscellaneous: BUDGET_ESTIMATE_SCHEMA,
  totalEstimatedBudget: BUDGET_ESTIMATE_SCHEMA,
  confidence: z.enum(['LOW', 'MEDIUM', 'HIGH']),
});
export type BudgetAnalysisResult = z.infer<typeof BUDGET_ANALYSIS_SCHEMA>;

export const BUDGET_ANALYSIS: AiCapabilityDefinition<BudgetAnalysisResult> = {
  id: 'travel-budget-analysis',
  templateId: 'general',
  feature: 'travel',
  contextScope: { includePlanner: false, includeTravel: false },
  basePrompt:
    'Estimate a budget for the trip described below (given as Input): accommodation, transportation, food, activities, and miscellaneous costs, ' +
    'plus a total. Use `null` for "estimate" and explain why in "uncertaintyNote" wherever you genuinely cannot produce a reasonable figure — ' +
    'never fabricate false precision (e.g. do not invent an exact number when the destination or trip length makes any estimate purely speculative).',
  jsonShapeInstruction:
    '{"accommodation": {"estimate": number | null, "currency": string, "uncertaintyNote": string | null}, "transportation": {...same shape...}, "food": {...}, "activities": {...}, "miscellaneous": {...}, "totalEstimatedBudget": {...}, "confidence": "LOW" | "MEDIUM" | "HIGH"}',
  outputSchema: BUDGET_ANALYSIS_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.2,
  maxTokens: 600,
};

const PACKING_CATEGORY_SCHEMA = z.object({
  category: z.string().min(1),
  items: z.array(z.string().min(1)).min(1),
});

const PACKING_LIST_SCHEMA = z.object({
  categories: z.array(PACKING_CATEGORY_SCHEMA).min(1).max(10),
});
export type PackingListResult = z.infer<typeof PACKING_LIST_SCHEMA>;

export const PACKING_LIST: AiCapabilityDefinition<PackingListResult> = {
  id: 'travel-packing-list',
  templateId: 'general',
  feature: 'travel',
  contextScope: { includePlanner: false, includeTravel: false },
  basePrompt:
    'Generate a categorized packing list for the trip described below (given as Input), considering the destination, trip duration, season/weather ' +
    "assumptions for that destination and time of year, and the trip's likely travel type. Use categories like Documents, Electronics, Clothing, " +
    'Medicine, and Personal Care where they apply, plus any other category genuinely useful for this specific trip.',
  jsonShapeInstruction: '{"categories": [{"category": string, "items": string[]}]}',
  outputSchema: PACKING_LIST_SCHEMA,
  preferredProvider: OPENROUTER_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.4,
  maxTokens: 700,
};

const RISK_ANALYSIS_SCHEMA = z.object({
  overlyBusyDays: z
    .array(
      z.object({
        date: z.string().regex(DATE_PATTERN),
        itemCount: z.number().int().nonnegative(),
        reason: z.string().min(1),
      }),
    )
    .default([]),
  unrealisticSchedules: z
    .array(
      z.object({
        description: z.string().min(1),
        itemTitles: z.array(z.string().min(1)).min(1),
      }),
    )
    .default([]),
  missingTransportation: z.array(z.string().min(1)).default([]),
  budgetRisks: z.array(z.string().min(1)).default([]),
  missingPreparation: z.array(z.string().min(1)).default([]),
  recommendations: z.array(z.string().min(1)).min(1).max(8),
});
export type RiskAnalysisResult = z.infer<typeof RISK_ANALYSIS_SCHEMA>;

export const RISK_ANALYSIS: AiCapabilityDefinition<RiskAnalysisResult> = {
  id: 'travel-risk-analysis',
  templateId: 'general',
  feature: 'travel',
  contextScope: { includePlanner: false, includeTravel: false },
  basePrompt:
    'Analyze the trip and itinerary described below (given as Input) for planning risk. Identify overly busy days (too many/too demanding items ' +
    'scheduled), unrealistic schedules (items that plausibly conflict or leave impossibly little time between them), missing transportation ' +
    '(gaps between locations with no travel arrangement mentioned), budget risks, and missing preparation (e.g. no accommodation for a given night). ' +
    'Leave an array empty if you find nothing in that category — do not invent a risk that is not actually supported by the trip data. ' +
    'Then generate 1 to 8 concrete, actionable recommendations.',
  jsonShapeInstruction:
    '{"overlyBusyDays": [{"date": "YYYY-MM-DD", "itemCount": number, "reason": string}], "unrealisticSchedules": [{"description": string, "itemTitles": string[]}], "missingTransportation": string[], "budgetRisks": string[], "missingPreparation": string[], "recommendations": string[]}',
  outputSchema: RISK_ANALYSIS_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.3,
  maxTokens: 800,
};

const TRAVEL_TAGS_SCHEMA = z.object({
  tags: z.array(z.string().min(1)).min(1).max(5),
});
export type TravelTagsResult = z.infer<typeof TRAVEL_TAGS_SCHEMA>;

export const TRAVEL_TAGS: AiCapabilityDefinition<TravelTagsResult> = {
  id: 'travel-tags',
  templateId: 'general',
  feature: 'travel',
  contextScope: { includePlanner: false, includeTravel: false },
  basePrompt:
    'Generate 1 to 5 short, useful category tags for the trip described below (given as Input) — e.g. Business, Vacation, Family, Adventure, ' +
    'Backpacking, Luxury, Nature, Food, Culture, or another concise category that genuinely fits. Prefer the examples given when one clearly applies.',
  jsonShapeInstruction: '{"tags": string[]}',
  outputSchema: TRAVEL_TAGS_SCHEMA,
  preferredProvider: OPENROUTER_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.4,
  maxTokens: 200,
};
