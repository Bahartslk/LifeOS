/**
 * A registered `AiProvider`'s id (e.g. `"gemini"`). Deliberately a plain
 * `string`, not a fixed union/enum — per this sprint's "future providers
 * should require configuration only, not code changes", enumerating
 * not-yet-implemented providers (OpenAI, Anthropic, ...) here would be
 * exactly the kind of speculative code this sprint's "Do NOT over-engineer"
 * rule warns against. `constants/ai.constants.ts` holds the one id that
 * actually exists today (`GEMINI_PROVIDER_ID`).
 */
export type ProviderId = string;

/**
 * A routing hint for a future feature-specific provider override (e.g.
 * "always use OpenAI for Travel") — accepted by `ProviderRouter` today but
 * not yet consulted (see that class's doc comment). Kept narrow (matches
 * the prompt templates below 1:1) rather than open-ended, since a feature
 * that doesn't correspond to a template has nothing to route differently
 * for yet.
 */
export type AiFeature = 'general' | 'planner' | 'travel' | 'assistant';

/** The known prompt template ids — see `templates/index.ts`. */
export type AiTemplateId = 'base-system' | 'general' | 'planner' | 'travel' | 'assistant';
