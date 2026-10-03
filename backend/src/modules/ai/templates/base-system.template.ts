import { dateInTimeZone } from '../../../common/utils/date-time.util';
import { PromptTemplate } from '../interfaces/prompt-template.interface';

/**
 * The foundational system prompt every other template composes with —
 * identity, language/timezone awareness, and the "AI never writes
 * directly to business data" boundary from this module's Design
 * Principles. Never used standalone by `AiService` (it has no
 * feature-specific instructions of its own); `general.template.ts` is the
 * template to use when no more specific one applies.
 *
 * `v2` (Sprint 19): added an explicit "today's date" anchor, computed from
 * `context.metadata.generatedAt` in the caller's own timezone. Without
 * this, a model has no reliable sense of "today" and cannot correctly
 * resolve a relative expression like "tomorrow" or "next Friday" — needed
 * by Sprint 19's Planner capabilities (natural-language task creation,
 * deadline extraction) but harmless and mildly beneficial to every other
 * capability that composes this template too, so it was added here rather
 * than duplicated into each Planner-specific prompt. The date itself comes
 * from `common/utils/date-time.util.ts`'s `dateInTimeZone` — the same
 * helper Planner's dashboard uses for "today", so both always agree.
 */
export const BASE_SYSTEM_TEMPLATE: PromptTemplate = {
  id: 'base-system',
  version: 'v2',
  renderSystem(context) {
    return [
      'You are the LifeOS AI layer — a provider-agnostic assistant supporting the LifeOS personal life-management app.',
      `Today's date is ${dateInTimeZone(new Date(context.metadata.generatedAt), context.preferences.timezone)} in the user's timezone ("${context.preferences.timezone}"). Resolve any relative date or time expression (e.g. "tomorrow", "next Friday", "this weekend") against this date.`,
      `Respond in the user's preferred language ("${context.preferences.language}").`,
      'Be concise and factual. Never invent data that was not provided in the context below.',
      'You never write directly to Planner or Travel data — you may only suggest; the calling application decides whether to apply any suggestion.',
    ].join('\n');
  },
};
