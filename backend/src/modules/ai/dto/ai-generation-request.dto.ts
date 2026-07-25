import { BuildContextOptions } from '../context/context-builder.service';
import { AiGenerateOptions } from '../interfaces/ai-provider.interface';
import { AiFeature, AiTemplateId, ProviderId } from '../types/ai.types';

/**
 * `AiService.generate()`'s input contract — the ONLY AI-module shape a
 * caller (today, only `AiCapabilityService`; in principle any future
 * feature module) needs to know about. It never sees `AiProvider`,
 * `GeminiProvider`, `AiContext`, or any other provider- or context-layer
 * type — "Business modules must never know whether Gemini, OpenAI, Claude,
 * Ollama or any future provider is being used."
 *
 * Not a class-validator DTO (no `@IsString()` etc.) — this is an internal
 * service-to-service contract, not an HTTP request body; `AiCapabilityRequestDto`
 * (validated) is the actual HTTP-facing shape `ai.controller.ts` accepts.
 */
export interface AiGenerationRequestDto {
  /** Whose Users/Planner/Travel context to build; omit for a context-free generation (e.g. a request with no user-specific data need). */
  userId?: string;
  templateId: AiTemplateId;
  /** The caller's actual ask, e.g. "Summarize today's tasks in one sentence." */
  instruction: string;
  variables?: Record<string, string>;
  /** Routing hint for `ProviderRouter`'s config-driven feature override (`AI_FEATURE_PROVIDER_OVERRIDES`) — see that class's precedence rules. */
  feature?: AiFeature;
  /** A caller-level provider preference (e.g. `AiCapabilityDefinition.preferredProvider`) — lower precedence than a config-driven `feature` override, higher than plain `providerPriority` order. See `ProviderRouter.resolveChain`. */
  preferredProviderId?: ProviderId;
  /** Default `true`. `false` disables fallback for this call regardless of `AI_FALLBACK_ENABLED` — never the reverse (global config can only further restrict, never force fallback on). See `AiFallbackExecutor`. */
  fallbackAllowed?: boolean;
  /** Logging-only metadata (e.g. `AiCapabilityDefinition.id`) — never used for routing/behavior, purely so `AiService.logCall` can attribute a log line to the capability that issued it. */
  capabilityId?: string;
  contextOptions?: BuildContextOptions;
  generateOptions?: AiGenerateOptions;
}
