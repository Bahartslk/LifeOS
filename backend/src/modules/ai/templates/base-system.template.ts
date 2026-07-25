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
 * than duplicated into each Planner-specific prompt.
 */
export const BASE_SYSTEM_TEMPLATE: PromptTemplate = {
  id: 'base-system',
  version: 'v2',
  renderSystem(context) {
    return [
      'You are the LifeOS AI layer — a provider-agnostic assistant supporting the LifeOS personal life-management app.',
      `Today's date is ${formatDateInTimezone(context.metadata.generatedAt, context.preferences.timezone)} in the user's timezone ("${context.preferences.timezone}"). Resolve any relative date or time expression (e.g. "tomorrow", "next Friday", "this weekend") against this date.`,
      `Respond in the user's preferred language ("${context.preferences.language}").`,
      'Be concise and factual. Never invent data that was not provided in the context below.',
      'You never write directly to Planner or Travel data — you may only suggest; the calling application decides whether to apply any suggestion.',
    ].join('\n');
  },
};

/** `en-CA` reliably formats as "YYYY-MM-DD" — a well-known `Intl.DateTimeFormat` convention, avoiding a manual format string. Falls back to a UTC date slice if `timezone` is ever an invalid IANA name (defensive; `User.timezone` is DTO-validated at write time, but a prompt template should not throw over a formatting concern). */
function formatDateInTimezone(isoTimestamp: string, timezone: string): string {
  try {
    return new Intl.DateTimeFormat('en-CA', {
      timeZone: timezone,
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).format(new Date(isoTimestamp));
  } catch {
    return new Date(isoTimestamp).toISOString().slice(0, 10);
  }
}
