import { AiTemplateId } from '../types/ai.types';

/**
 * DI token `ProviderRegistry` injects to receive every registered
 * `AiProvider` implementation as a plain array — see `ai.module.ts`'s
 * `AI_PROVIDERS` factory provider for how providers "self-register": each
 * concrete provider class is a normal Nest provider (so its own
 * dependencies resolve through Nest DI as usual), and this factory just
 * collects the resolved instances. `ProviderRegistry` never imports a
 * concrete provider class directly.
 */
export const AI_PROVIDERS = Symbol('AI_PROVIDERS');

/** See `providers/gemini/gemini.provider.ts`. */
export const GEMINI_PROVIDER_ID = 'gemini';

/** Sprint 18B's second provider — see `providers/openrouter/openrouter.provider.ts`. */
export const OPENROUTER_PROVIDER_ID = 'openrouter';

export const AI_TEMPLATE_IDS: Record<
  'BASE_SYSTEM' | 'GENERAL' | 'PLANNER' | 'TRAVEL' | 'ASSISTANT',
  AiTemplateId
> = {
  BASE_SYSTEM: 'base-system',
  GENERAL: 'general',
  PLANNER: 'planner',
  TRAVEL: 'travel',
  ASSISTANT: 'assistant',
};

/**
 * Per-section item caps applied when `mapper/context.mapper.ts` shapes a
 * Planner/Travel dashboard into `AiContext` — docs/12-project-architecture.md
 * #context-management's "context size ceiling ... most recent/relevant
 * items are prioritized" rule. Planner/Travel's own dashboard queries are
 * already bounded (today + upcoming, never full history), but "upcoming"
 * itself has no cutoff (see `PlannerService.getDashboard`'s doc comment),
 * so these caps are this module's own defensive trim on top of that.
 */
export const MAX_CONTEXT_TASKS_PER_SECTION = 5;
export const MAX_CONTEXT_TRIPS_PER_SECTION = 5;
export const MAX_CONTEXT_ITINERARY_ITEMS = 5;
