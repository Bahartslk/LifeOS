import { AiConfigService } from '../config/ai-config.service';
import { AiProvider } from '../interfaces/ai-provider.interface';
import { ProviderRegistry } from '../registry/provider-registry.service';
import { ProviderRouter } from './provider-router.service';

function fakeProvider(id: string): AiProvider {
  return {
    id,
    displayName: id,
    isAvailable: () => true,
    generate: jest.fn(),
  };
}

function fakeConfig(
  overrides: Partial<{
    defaultProvider: string;
    enabledProviders: string[];
    providerPriority: string[];
    featureProviderOverrides: Record<string, string>;
  }> = {},
): AiConfigService {
  return {
    defaultProvider: overrides.defaultProvider ?? 'gemini',
    enabledProviders: overrides.enabledProviders ?? ['gemini', 'openrouter'],
    providerPriority: overrides.providerPriority ?? ['gemini', 'openrouter'],
    featureProviderOverrides: overrides.featureProviderOverrides ?? {},
  } as unknown as AiConfigService;
}

describe('ProviderRouter', () => {
  let registry: ProviderRegistry;

  beforeEach(() => {
    registry = new ProviderRegistry([fakeProvider('gemini'), fakeProvider('openrouter')]);
    registry.onModuleInit();
  });

  it('resolves plain providerPriority order with no preference', () => {
    const router = new ProviderRouter(registry, fakeConfig());
    expect(router.resolveChain().map((p) => p.id)).toEqual(['gemini', 'openrouter']);
  });

  it('applies a caller-supplied preferredProviderId first, keeping the rest as fallback', () => {
    const router = new ProviderRouter(registry, fakeConfig());
    expect(router.resolveChain({ preferredProviderId: 'openrouter' }).map((p) => p.id)).toEqual([
      'openrouter',
      'gemini',
    ]);
  });

  it('prefers a config-driven feature override over a caller-supplied preference', () => {
    const router = new ProviderRouter(
      registry,
      fakeConfig({ featureProviderOverrides: { travel: 'gemini' } }),
    );
    expect(
      router
        .resolveChain({ feature: 'travel', preferredProviderId: 'openrouter' })
        .map((p) => p.id),
    ).toEqual(['gemini', 'openrouter']);
  });

  it('ignores a feature override for an unrelated feature', () => {
    const router = new ProviderRouter(
      registry,
      fakeConfig({ featureProviderOverrides: { travel: 'gemini' } }),
    );
    expect(router.resolveChain({ feature: 'planner' }).map((p) => p.id)).toEqual([
      'gemini',
      'openrouter',
    ]);
  });

  it('excludes a provider not listed in enabledProviders even if registered and available', () => {
    const router = new ProviderRouter(registry, fakeConfig({ enabledProviders: ['gemini'] }));
    expect(router.resolveChain().map((p) => p.id)).toEqual(['gemini']);
  });

  it('excludes a provider that reports itself unavailable', () => {
    const unavailableRegistry = new ProviderRegistry([
      { ...fakeProvider('gemini'), isAvailable: () => false },
      fakeProvider('openrouter'),
    ]);
    unavailableRegistry.onModuleInit();
    const router = new ProviderRouter(unavailableRegistry, fakeConfig());
    expect(router.resolveChain().map((p) => p.id)).toEqual(['openrouter']);
  });

  it('resolveChain() returns an empty array when no provider is enabled', () => {
    const router = new ProviderRouter(registry, fakeConfig({ enabledProviders: [] }));
    expect(router.resolveChain()).toEqual([]);
  });
});
