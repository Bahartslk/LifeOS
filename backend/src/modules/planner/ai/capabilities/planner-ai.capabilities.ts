import { z } from 'zod';
import { DATE_PATTERN, TIME_PATTERN } from '../../../../common/utils/date-time.util';
import { AiCapabilityDefinition } from '../../../ai/capabilities/capability.types';
import { GEMINI_PROVIDER_ID, OPENROUTER_PROVIDER_ID } from '../../../ai/constants/ai.constants';

/**
 * Sprint 19 (Intelligent Planner) — six Planner-owned AI capabilities,
 * built entirely on Sprint 18A/18B/18B.5's existing execution engine
 * (`AiCapabilityService.run`, `ProviderRouter`, `ProviderRegistry`,
 * `AiFallbackExecutor`, `ContextBuilder`, `PromptBuilder`). Nothing here
 * duplicates that infrastructure — each definition is pure data, exactly
 * like `ai/capabilities/capability-definitions.ts`'s Sprint 18B
 * capabilities, just owned by (and living inside) `PlannerModule` instead
 * of `AiModule`, per this sprint's explicit "Planner owns these
 * capabilities" instruction.
 *
 * None of these write to `Task`/`TaskList` — every `result` is a draft or
 * suggestion; `PlannerAiController`'s callers (the mobile client) decide
 * whether to turn one into a real `POST/PATCH /planner/tasks` call.
 */

const TASK_DRAFT_SCHEMA = z.object({
  title: z.string().min(1).max(200),
  description: z.string().min(1).nullable(),
  date: z.string().regex(DATE_PATTERN).nullable(),
  time: z.string().regex(TIME_PATTERN).nullable(),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH']),
  estimatedDurationMinutes: z.number().int().positive().nullable(),
  tags: z.array(z.string().min(1)).max(5),
});
export type TaskDraftResult = z.infer<typeof TASK_DRAFT_SCHEMA>;

export const TASK_DRAFT: AiCapabilityDefinition<TaskDraftResult> = {
  id: 'planner-task-draft',
  templateId: 'general',
  feature: 'planner',
  contextScope: { includePlanner: false, includeTravel: false },
  basePrompt:
    "Read the caller's natural-language task description (given below as Input) and turn it into a single structured task draft. " +
    'If a date, time, priority, duration, or tags are not stated or clearly implied, use `null` (or an empty array for tags) rather than guessing a specific value. ' +
    'This is a DRAFT only — it will be shown to the user for review before anything is saved.',
  jsonShapeInstruction:
    '{"title": string, "description": string | null, "date": "YYYY-MM-DD" | null, "time": "HH:mm" | null, "priority": "LOW" | "MEDIUM" | "HIGH", "estimatedDurationMinutes": number | null, "tags": string[]}',
  outputSchema: TASK_DRAFT_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  // Extraction, not creative writing — favor a literal, consistent read of the input text.
  temperature: 0.2,
  maxTokens: 400,
};

const SUBTASK_SCHEMA = z.object({
  title: z.string().min(1).max(200),
  description: z.string().min(1).nullable(),
  estimatedDurationMinutes: z.number().int().positive().nullable(),
});

const TASK_BREAKDOWN_SCHEMA = z.object({
  subtasks: z.array(SUBTASK_SCHEMA).min(2).max(8),
});
export type TaskBreakdownResult = z.infer<typeof TASK_BREAKDOWN_SCHEMA>;

export const TASK_BREAKDOWN: AiCapabilityDefinition<TaskBreakdownResult> = {
  id: 'planner-task-breakdown',
  templateId: 'general',
  feature: 'planner',
  contextScope: { includePlanner: false, includeTravel: false },
  basePrompt:
    'The caller wants to break the task described below (given as Input) into 2 to 8 smaller, concrete, actionable subtasks. ' +
    'Each subtask should be a single clear action, not a restatement of the whole task.',
  jsonShapeInstruction:
    '{"subtasks": [{"title": string, "description": string | null, "estimatedDurationMinutes": number | null}]}',
  outputSchema: TASK_BREAKDOWN_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.5,
  maxTokens: 500,
};

const OVERLOADED_DAY_SCHEMA = z.object({
  date: z.string().regex(DATE_PATTERN),
  taskCount: z.number().int().nonnegative(),
  reason: z.string().min(1),
});

const SCHEDULE_CONFLICT_SCHEMA = z.object({
  description: z.string().min(1),
  taskTitles: z.array(z.string().min(1)).min(1),
});

const SCHEDULE_ANALYSIS_SCHEMA = z.object({
  overloadedDays: z.array(OVERLOADED_DAY_SCHEMA),
  freeDays: z.array(z.string().regex(DATE_PATTERN)),
  conflicts: z.array(SCHEDULE_CONFLICT_SCHEMA),
  workloadBalance: z.string().min(1),
  suggestions: z.array(z.string().min(1)).min(1).max(5),
});
export type ScheduleAnalysisResult = z.infer<typeof SCHEDULE_ANALYSIS_SCHEMA>;

export const SCHEDULE_ANALYSIS: AiCapabilityDefinition<ScheduleAnalysisResult> = {
  id: 'planner-schedule-analysis',
  templateId: 'planner',
  feature: 'planner',
  contextScope: { includeTravel: false },
  basePrompt:
    "Analyze the caller's Planner data (today's and upcoming tasks) given in context. Identify overloaded days (too many/too demanding tasks), " +
    'free days (no tasks scheduled), scheduling conflicts (e.g. multiple tasks that plausibly overlap or compete for the same time), and the overall ' +
    'workload balance across the period covered by the data. Then generate 1 to 5 concrete, actionable suggestions for the caller.',
  jsonShapeInstruction:
    '{"overloadedDays": [{"date": "YYYY-MM-DD", "taskCount": number, "reason": string}], "freeDays": ["YYYY-MM-DD"], "conflicts": [{"description": string, "taskTitles": string[]}], "workloadBalance": string, "suggestions": string[]}',
  outputSchema: SCHEDULE_ANALYSIS_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.3,
  maxTokens: 700,
};

const PRIORITY_SUGGESTION_SCHEMA = z.object({
  suggestedPriority: z.enum(['LOW', 'MEDIUM', 'HIGH']),
  reason: z.string().min(1),
});
export type PrioritySuggestionResult = z.infer<typeof PRIORITY_SUGGESTION_SCHEMA>;

export const PRIORITY_SUGGESTION: AiCapabilityDefinition<PrioritySuggestionResult> = {
  id: 'planner-priority-suggestion',
  templateId: 'planner',
  feature: 'planner',
  contextScope: { includeTravel: false },
  basePrompt:
    'Suggest an appropriate priority (LOW, MEDIUM, or HIGH) for the task described below (given as Input), based on its due date, its wording ' +
    "(urgency implied by the title/description), and the caller's current Planner context (how busy they already are). " +
    'This is a recommendation only — never claim the task has already been updated.',
  jsonShapeInstruction: '{"suggestedPriority": "LOW" | "MEDIUM" | "HIGH", "reason": string}',
  outputSchema: PRIORITY_SUGGESTION_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.2,
  maxTokens: 250,
};

const SMART_TAGS_SCHEMA = z.object({
  tags: z.array(z.string().min(1)).min(1).max(5),
});
export type SmartTagsResult = z.infer<typeof SMART_TAGS_SCHEMA>;

export const SMART_TAGS: AiCapabilityDefinition<SmartTagsResult> = {
  id: 'planner-smart-tags',
  templateId: 'general',
  feature: 'planner',
  contextScope: { includePlanner: false, includeTravel: false },
  basePrompt:
    'Generate 1 to 5 short, useful category tags for the task described below (given as Input) — e.g. Study, Work, Finance, Shopping, Travel, ' +
    'Health, Personal, or another concise category that genuinely fits. Prefer the examples given when one clearly applies.',
  jsonShapeInstruction: '{"tags": string[]}',
  outputSchema: SMART_TAGS_SCHEMA,
  preferredProvider: OPENROUTER_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.4,
  maxTokens: 200,
};

const DEADLINE_EXTRACTION_SCHEMA = z.object({
  date: z.string().regex(DATE_PATTERN).nullable(),
  time: z.string().regex(TIME_PATTERN).nullable(),
  isAllDay: z.boolean(),
  confidence: z.enum(['LOW', 'MEDIUM', 'HIGH']),
  originalPhrase: z.string().min(1),
});
export type DeadlineExtractionResult = z.infer<typeof DEADLINE_EXTRACTION_SCHEMA>;

export const DEADLINE_EXTRACTION: AiCapabilityDefinition<DeadlineExtractionResult> = {
  id: 'planner-deadline-extraction',
  templateId: 'general',
  feature: 'planner',
  contextScope: { includePlanner: false, includeTravel: false },
  basePrompt:
    'Extract a date and, if stated, a time from the natural-language text given below as Input — including incomplete or relative expressions ' +
    '(e.g. "next Friday", "tomorrow morning", "this weekend"). Resolve relative expressions against today\'s date (see above). ' +
    'If only a date is implied with no specific time, set "time" to null and "isAllDay" to true. If the text is too ambiguous to resolve at all, ' +
    'set "date" and "time" to null and "confidence" to "LOW" rather than guessing.',
  jsonShapeInstruction:
    '{"date": "YYYY-MM-DD" | null, "time": "HH:mm" | null, "isAllDay": boolean, "confidence": "LOW" | "MEDIUM" | "HIGH", "originalPhrase": string}',
  outputSchema: DEADLINE_EXTRACTION_SCHEMA,
  preferredProvider: GEMINI_PROVIDER_ID,
  fallbackAllowed: true,
  temperature: 0.1,
  maxTokens: 200,
};
