import { Logger } from '@nestjs/common';
import { AiConfigService } from '../config/ai-config.service';
import {
  InvalidProviderResponseException,
  ProviderRateLimitException,
  ProviderTimeoutException,
  ProviderUnavailableException,
} from '../exceptions/ai.exceptions';
import { AiRetryExecutor } from './ai-retry.executor';

function makeExecutor(retryCount = 2): AiRetryExecutor {
  const executor = new AiRetryExecutor({ retryCount } as unknown as AiConfigService);
  // Skip the real backoff delay; only the number of attempts matters here.
  jest
    .spyOn(executor as unknown as { sleep: (ms: number) => Promise<void> }, 'sleep')
    .mockResolvedValue(undefined);
  return executor;
}

describe('AiRetryExecutor', () => {
  beforeEach(() => {
    jest.spyOn(Logger.prototype, 'warn').mockImplementation(() => undefined);
  });

  afterEach(() => jest.restoreAllMocks());

  it('does not retry a provider rate limit (429)', async () => {
    const operation = jest.fn().mockRejectedValue(new ProviderRateLimitException('gemini'));

    await expect(makeExecutor().execute(operation)).rejects.toBeInstanceOf(
      ProviderRateLimitException,
    );
    expect(operation).toHaveBeenCalledTimes(1);
  });

  it('does not retry an invalid provider response', async () => {
    const operation = jest
      .fn()
      .mockRejectedValue(new InvalidProviderResponseException('gemini', 'bad'));

    await expect(makeExecutor().execute(operation)).rejects.toBeInstanceOf(
      InvalidProviderResponseException,
    );
    expect(operation).toHaveBeenCalledTimes(1);
  });

  it.each([
    ['unavailable (5xx)', new ProviderUnavailableException('gemini')],
    ['timeout', new ProviderTimeoutException('gemini')],
  ])('retries a transient %s failure up to the configured count', async (_label, error) => {
    const operation = jest.fn().mockRejectedValue(error);

    await expect(makeExecutor(2).execute(operation)).rejects.toBe(error);
    expect(operation).toHaveBeenCalledTimes(3);
  });

  it('returns the result and retry count once a retry succeeds', async () => {
    const operation = jest
      .fn()
      .mockRejectedValueOnce(new ProviderUnavailableException('gemini'))
      .mockResolvedValue('ok');

    await expect(makeExecutor(2).execute(operation)).resolves.toEqual({
      result: 'ok',
      retryCount: 1,
    });
  });
});
