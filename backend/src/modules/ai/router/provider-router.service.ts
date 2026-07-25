import { Injectable, Logger } from '@nestjs/common';
import { AiConfigService } from '../config/ai-config.service';
import { AiProvider } from '../interfaces/ai-provider.interface';
import { ProviderRegistry } from '../registry/provider-registry.service';
import { AiFeature, ProviderId } from '../types/ai.types';

export interface ProviderResolutionRequest {
  /**
   * Consulted against `AiConfigService.featureProviderOverrides` — if that
   * config map has an entry for this feature, it takes precedence over
   * `preferredProviderId` below (an operational config override always
   * wins over a code-level default, so ops can change routing without a
   * redeploy).
   */
  feature?: AiFeature;
  /**
   * A caller-level preference (e.g. `AiCapabilityDefinition.preferredProvider`)
   * — used only when no `featureProviderOverrides` entry applies. Either
   * way, the rest of `providerPriority` still follows the winning
   * preference in the chain, so expressing a preference changes *order*,
   * not the fallback safety net.
   */
  preferredProviderId?: ProviderId;
}

/**
 * Decides which registered `AiProvider`(s) handle a request. Selection
 * precedence (`resolveChain`): a config-driven `featureProviderOverrides`
 * entry, then a caller-supplied `preferredProviderId`, then plain
 * `providerPriority` order — still no cost/latency-based routing (see
 * Suggestions for Sprint 19). `resolveChain`'s ordered-list return shape,
 * introduced in Sprint 18A specifically so a future executor could walk
 * it, is exactly what `AiFallbackExecutor` does.
 */
@Injectable()
export class ProviderRouter {
  private readonly logger = new Logger(ProviderRouter.name);

  constructor(
    private readonly registry: ProviderRegistry,
    private readonly aiConfig: AiConfigService,
  ) {}

  /**
   * Every registered, enabled, currently-available provider, in
   * preference order — see `ProviderResolutionRequest`'s field docs for
   * the exact precedence. `AiFallbackExecutor` walks this list in order,
   * retrying each candidate's own transient failures via
   * `AiRetryExecutor` before advancing to the next.
   */
  resolveChain(request: ProviderResolutionRequest = {}): AiProvider[] {
    const enabled = new Set(this.aiConfig.enabledProviders);
    const priorityIds =
      this.aiConfig.providerPriority.length > 0
        ? this.aiConfig.providerPriority
        : [this.aiConfig.defaultProvider];

    const configOverrideId = request.feature
      ? this.aiConfig.featureProviderOverrides[request.feature]
      : undefined;
    const preferredId = configOverrideId ?? request.preferredProviderId;

    if (preferredId) {
      this.logger.debug(
        configOverrideId
          ? `Feature "${request.feature}" overridden to provider "${preferredId}" via config.`
          : `Preferring provider "${preferredId}" for this request.`,
      );
    }

    const orderedIds: ProviderId[] = preferredId
      ? [preferredId, ...priorityIds.filter((id) => id !== preferredId)]
      : priorityIds;

    return orderedIds
      .filter((id) => enabled.has(id) && this.registry.has(id))
      .map((id) => this.registry.resolve(id))
      .filter((provider) => provider.isAvailable());
  }
}
