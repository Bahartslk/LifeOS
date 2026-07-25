import { AiConfigService } from '../config/ai-config.service';
import {
  ProviderFallbackExhaustedException,
  ProviderUnavailableException,
} from '../exceptions/ai.exceptions';
import { ProviderTimeoutException } from '../exceptions/ai.exceptions';
import { AiProvider } from '../interfaces/ai-provider.interface';
import { AiFallbackExecutor } from './ai-fallback.executor';
import { AiRetryExecutor } from './ai-retry.executor';

function fakeProvider(id: string, behavior: 'succeed' | 'fail'): AiProvider {
  return {
    id,
    displayName: id,
    isAvailable: () => true,
    generate: async () => {
      if (behavior === 'succeed') {
        return {
          text: 'ok',
          providerId: id,
          model: 'fake-model',
          usage: { estimatedInputTokens: 1, estimatedOutputTokens: 1 },
          latencyMs: 1,
        };
      }
      throw new ProviderTimeoutException(id);
    },
  };
}

function fakeConfig(fallbackEnabled: boolean, retryCount = 0): AiConfigService {
  return { fallbackEnabled, retryCount, defaultProvider: 'gemini' } as unknown as AiConfigService;
}

function run(chain: AiProvider[], executor: AiFallbackExecutor, fallbackAllowed?: boolean) {
  return executor.execute(
    chain,
    (provider) => provider.generate({ systemPrompt: '', userPrompt: '' }),
    {
      fallbackAllowed,
    },
  );
}

describe('AiFallbackExecutor', () => {
  it('throws ProviderUnavailableException for an empty chain', async () => {
    const config = fakeConfig(true);
    const executor = new AiFallbackExecutor(new AiRetryExecutor(config), config);
    await expect(run([], executor)).rejects.toThrow(ProviderUnavailableException);
  });

  it('only tries the first candidate when fallback is globally disabled', async () => {
    const config = fakeConfig(false);
    const executor = new AiFallbackExecutor(new AiRetryExecutor(config), config);
    const chain = [fakeProvider('primary', 'fail'), fakeProvider('secondary', 'succeed')];
    await expect(run(chain, executor)).rejects.toThrow(ProviderFallbackExhaustedException);
  });

  it('falls back to the next provider once the first is exhausted', async () => {
    const config = fakeConfig(true);
    const executor = new AiFallbackExecutor(new AiRetryExecutor(config), config);
    const chain = [fakeProvider('primary', 'fail'), fakeProvider('secondary', 'succeed')];
    const outcome = await run(chain, executor);
    expect(outcome.providerId).toBe('secondary');
    expect(outcome.fallbackCount).toBe(1);
    expect(outcome.attemptedProviderIds).toEqual(['primary', 'secondary']);
  });

  it('succeeds on the first candidate with fallbackCount 0 when it works', async () => {
    const config = fakeConfig(true);
    const executor = new AiFallbackExecutor(new AiRetryExecutor(config), config);
    const chain = [fakeProvider('primary', 'succeed'), fakeProvider('secondary', 'succeed')];
    const outcome = await run(chain, executor);
    expect(outcome.providerId).toBe('primary');
    expect(outcome.fallbackCount).toBe(0);
    expect(outcome.attemptedProviderIds).toEqual(['primary']);
  });

  it('respects a per-call fallbackAllowed:false override even when globally enabled', async () => {
    const config = fakeConfig(true);
    const executor = new AiFallbackExecutor(new AiRetryExecutor(config), config);
    const chain = [fakeProvider('primary', 'fail'), fakeProvider('secondary', 'succeed')];
    await expect(run(chain, executor, false)).rejects.toThrow(ProviderFallbackExhaustedException);
  });

  it('a per-call fallbackAllowed:true cannot override global fallbackEnabled:false', async () => {
    const config = fakeConfig(false);
    const executor = new AiFallbackExecutor(new AiRetryExecutor(config), config);
    const chain = [fakeProvider('primary', 'fail'), fakeProvider('secondary', 'succeed')];
    await expect(run(chain, executor, true)).rejects.toThrow(ProviderFallbackExhaustedException);
  });

  it('throws ProviderFallbackExhaustedException listing every attempted provider when all fail', async () => {
    const config = fakeConfig(true);
    const executor = new AiFallbackExecutor(new AiRetryExecutor(config), config);
    const chain = [fakeProvider('primary', 'fail'), fakeProvider('secondary', 'fail')];
    await expect(run(chain, executor)).rejects.toThrow(/primary, secondary/);
  });
});
