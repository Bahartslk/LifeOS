import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../../config/configuration';
import { AiFeature, ProviderId } from '../types/ai.types';

/**
 * Strongly-typed accessor for the `ai` slice of `AppConfig` — every AI
 * internal (`ProviderRegistry`, `ProviderRouter`, `GeminiProvider`,
 * `AiRetryExecutor`, ...) reads configuration through this class instead of
 * calling `ConfigService.get('ai.xxx', ...)` ad hoc, the same "one typed
 * accessor" convention the rest of the app already follows via
 * `ConfigService<AppConfig, true>`. This class reads no environment
 * variable directly — `config/configuration.ts` remains the single place
 * that does, per that file's own doc comment.
 */
@Injectable()
export class AiConfigService {
  constructor(private readonly configService: ConfigService<AppConfig, true>) {}

  get geminiApiKey(): string | undefined {
    return this.configService.get('ai.geminiApiKey', { infer: true });
  }

  get openRouterApiKey(): string | undefined {
    return this.configService.get('ai.openRouterApiKey', { infer: true });
  }

  get openRouterModel(): string {
    return this.configService.get('ai.openRouterModel', { infer: true });
  }

  get defaultProvider(): string {
    return this.configService.get('ai.defaultProvider', { infer: true });
  }

  get enabledProviders(): string[] {
    return this.configService.get('ai.enabledProviders', { infer: true });
  }

  get providerPriority(): string[] {
    return this.configService.get('ai.providerPriority', { infer: true });
  }

  get fallbackEnabled(): boolean {
    return this.configService.get('ai.fallbackEnabled', { infer: true });
  }

  get model(): string {
    return this.configService.get('ai.model', { infer: true });
  }

  get temperature(): number {
    return this.configService.get('ai.temperature', { infer: true });
  }

  get topP(): number {
    return this.configService.get('ai.topP', { infer: true });
  }

  get topK(): number {
    return this.configService.get('ai.topK', { infer: true });
  }

  get maxTokens(): number {
    return this.configService.get('ai.maxTokens', { infer: true });
  }

  get timeoutMs(): number {
    return this.configService.get('ai.timeoutMs', { infer: true });
  }

  get retryCount(): number {
    return this.configService.get('ai.retryCount', { infer: true });
  }

  /**
   * Narrows `configuration.ts`'s feature-agnostic `Record<string, string>`
   * to the AI module's own `AiFeature`/`ProviderId` types — this is the one
   * place that narrowing happens, since `config/` itself never imports a
   * feature module's types (see that field's doc comment in
   * `configuration.ts`). Values that aren't a real `AiFeature` are simply
   * never looked up by `ProviderRouter` (which only ever queries by a real
   * `AiFeature`), so a typo in `AI_FEATURE_PROVIDER_OVERRIDES` is inert
   * rather than a boot-time failure.
   */
  get featureProviderOverrides(): Partial<Record<AiFeature, ProviderId>> {
    return this.configService.get('ai.featureProviderOverrides', { infer: true }) as Partial<
      Record<AiFeature, ProviderId>
    >;
  }
}
