import { Inject, Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { AI_PROVIDERS } from '../constants/ai.constants';
import {
  AiConfigurationException,
  ProviderUnavailableException,
} from '../exceptions/ai.exceptions';
import { AiProvider } from '../interfaces/ai-provider.interface';
import { ProviderId } from '../types/ai.types';

/**
 * Discovers, registers, resolves, and validates every `AiProvider`
 * implementation registered in `AiModule`. Providers self-register purely
 * through Nest DI — `ai.module.ts`'s `AI_PROVIDERS` factory collects every
 * concrete provider class Nest constructs into a plain array, which this
 * class injects via `@Inject(AI_PROVIDERS)`. This class never imports a
 * concrete provider (e.g. `GeminiProvider`) and never constructs one with
 * `new` — adding a second provider later means adding it to that one
 * factory, not touching this file.
 */
@Injectable()
export class ProviderRegistry implements OnModuleInit {
  private readonly logger = new Logger(ProviderRegistry.name);
  private readonly providers = new Map<ProviderId, AiProvider>();

  constructor(@Inject(AI_PROVIDERS) private readonly injectedProviders: AiProvider[]) {}

  onModuleInit(): void {
    for (const provider of this.injectedProviders) {
      this.register(provider);
    }
  }

  register(provider: AiProvider): void {
    if (this.providers.has(provider.id)) {
      throw new AiConfigurationException(`Duplicate AI provider id "${provider.id}".`);
    }
    this.providers.set(provider.id, provider);
    this.logger.log(`Registered AI provider "${provider.id}" (${provider.displayName}).`);
  }

  has(id: ProviderId): boolean {
    return this.providers.has(id);
  }

  resolve(id: ProviderId): AiProvider {
    const provider = this.providers.get(id);
    if (!provider) {
      throw new ProviderUnavailableException(id);
    }
    return provider;
  }

  /** Registered AND currently config-ready (e.g. API key present) — what `ProviderRouter` actually routes to. */
  listAvailable(): AiProvider[] {
    return [...this.providers.values()].filter((provider) => provider.isAvailable());
  }

  /** Throws if `id` isn't registered, or is registered but not currently usable. */
  validate(id: ProviderId): void {
    const provider = this.resolve(id);
    if (!provider.isAvailable()) {
      throw new ProviderUnavailableException(id);
    }
  }
}
