import { ZodType } from 'zod';
import { BuildContextOptions } from '../context/context-builder.service';
import { AiFeature, AiTemplateId, ProviderId } from '../types/ai.types';

/**
 * A capability's own id, for logging/attribution only (see
 * `AiGenerationRequestDto.capabilityId`) — never switched on or used for
 * exhaustiveness checking, so a plain `string` rather than a closed union.
 * Widened from a 4-value union scoped to this module's own Sprint 18B
 * capabilities (`'planner-analyze' | 'planner-suggest' | 'travel-suggest' |
 * 'daily-brief'`) once Sprint 19 added capabilities owned by other modules
 * (`modules/planner/ai/`) — this module cannot enumerate every business
 * module's capability ids without depending on them.
 */
export type AiCapabilityId = string;

/**
 * A declarative, complete description of one capability-based AI endpoint
 * — data, not a class, mirroring `templates/index.ts`'s `PROMPT_TEMPLATES`
 * map convention. `AiCapabilityService.run` is the single generic executor
 * every definition shares; every behavioral difference between
 * capabilities lives here, in this metadata, never as a hardcoded branch
 * inside a service (Sprint 18B.5's "behavior should be driven by
 * capability metadata" rule).
 */
export interface AiCapabilityDefinition<TResult = unknown> {
  id: AiCapabilityId;
  templateId: AiTemplateId;
  /** Passed through to `AiService.generate`'s `feature` field — see `ProviderRouter`'s feature-based routing. */
  feature: AiFeature;
  /** Which `ContextBuilder` sections this capability needs — per Sprint 18B's "Planner endpoints should not load Travel data unless required" (and vice versa). */
  contextScope: BuildContextOptions;
  /** The capability's base ask, before JSON-formatting instructions and the caller's optional `notes` are appended (see `AiCapabilityService.buildInstruction`). */
  basePrompt: string;
  /**
   * Embedded verbatim in the final user prompt — a human-readable
   * description of the JSON shape the model should return. Deliberately
   * separate from `outputSchema` rather than generated from it: this
   * string is tuned for what makes a *model* produce the right shape,
   * `outputSchema` is tuned for what makes *validation* strict and
   * correct — the two audiences don't always want the same phrasing (e.g.
   * `outputSchema` enforces `min(1).max(3)` on a suggestions array; the
   * prompt already says "1 to 3" in prose). Keep both in sync when editing
   * a capability's shape.
   */
  jsonShapeInstruction: string;
  /** Runtime schema validated against the provider's parsed JSON response before it ever reaches an API consumer — see `capabilities/response-validation.util.ts`. */
  outputSchema: ZodType<TResult>;
  /**
   * `ProviderRouter.resolveChain`'s per-capability preference (e.g.
   * Planner capabilities prefer Gemini, Travel prefers OpenRouter) — takes
   * effect only when no `AI_FEATURE_PROVIDER_OVERRIDES` config entry exists
   * for this capability's `feature` (config always wins over a code
   * default, so ops can override behavior without a redeploy). Omit to
   * fall through to plain `providerPriority` order.
   */
  preferredProvider?: ProviderId;
  /** Default `true`. When `false`, this capability never falls through to a second provider even if `AI_FALLBACK_ENABLED=true` globally — global config can only turn fallback further OFF for a capability, never force it ON when disabled globally. */
  fallbackAllowed?: boolean;
  /** Per-capability generation parameter override — omitted fields fall through to `AiConfigService`'s global defaults, the same override chain `AiGenerateOptions` already supports per-call. */
  temperature?: number;
  maxTokens?: number;
}
