import { Injectable, Logger } from '@nestjs/common';
import { AiConfigService } from '../config/ai-config.service';
import {
  ProviderFallbackExhaustedException,
  ProviderUnavailableException,
} from '../exceptions/ai.exceptions';
import { AiProvider } from '../interfaces/ai-provider.interface';
import { ProviderId } from '../types/ai.types';
import { AiRetryExecutor } from './ai-retry.executor';

export interface FallbackOutcome<T> {
  result: T;
  /** The provider that actually produced `result`. */
  providerId: ProviderId;
  /** Retries spent on the provider that ultimately succeeded (or, on total failure, is never read — see the thrown exception instead). */
  retryCount: number;
  /** How many earlier candidates in the chain were tried and failed before `providerId` succeeded — `0` means the first candidate succeeded. */
  fallbackCount: number;
  /** Every provider id tried, in order, ending with `providerId`. */
  attemptedProviderIds: ProviderId[];
}

export interface FallbackExecutionOptions {
  /** Default `true`. `false` restricts this call to the chain's first candidate only, regardless of `AiConfigService.fallbackEnabled` — see `execute`'s doc comment for the full precedence. */
  fallbackAllowed?: boolean;
}

/**
 * Walks `ProviderRouter.resolveChain`'s ordered candidate list, giving each
 * candidate its own full `AiRetryExecutor` attempt (bounded retries with
 * backoff, per Sprint 18A) before moving to the next — per Sprint 18B's
 * explicit "Fallback should only occur after the retry strategy from
 * Sprint 18A has been exhausted." `AiRetryExecutor` itself is untouched;
 * this class only adds the "try the next provider" loop around it.
 *
 * Whether fallback actually happens is `AiConfigService.fallbackEnabled`
 * (the global switch) AND `options.fallbackAllowed` (a per-call override,
 * e.g. from `AiCapabilityDefinition.fallbackAllowed`) — both must allow it.
 * This means global config can only ever turn fallback further OFF for a
 * capability that would otherwise allow it, never force it ON for one that
 * explicitly opts out; an operator disabling fallback platform-wide always
 * wins.
 */
@Injectable()
export class AiFallbackExecutor {
  private readonly logger = new Logger(AiFallbackExecutor.name);

  constructor(
    private readonly retryExecutor: AiRetryExecutor,
    private readonly aiConfig: AiConfigService,
  ) {}

  async execute<T>(
    chain: AiProvider[],
    operation: (provider: AiProvider) => Promise<T>,
    options: FallbackExecutionOptions = {},
  ): Promise<FallbackOutcome<T>> {
    if (chain.length === 0) {
      throw new ProviderUnavailableException(this.aiConfig.defaultProvider);
    }

    const fallbackAllowed = this.aiConfig.fallbackEnabled && (options.fallbackAllowed ?? true);
    const candidates = fallbackAllowed ? chain : chain.slice(0, 1);
    const attempted: ProviderId[] = [];

    for (let index = 0; index < candidates.length; index += 1) {
      const provider = candidates[index];
      attempted.push(provider.id);

      try {
        const { result, retryCount } = await this.retryExecutor.execute(() => operation(provider));
        return {
          result,
          providerId: provider.id,
          retryCount,
          fallbackCount: index,
          attemptedProviderIds: attempted,
        };
      } catch {
        const hasNext = index + 1 < candidates.length;
        this.logger.warn(
          `Provider "${provider.id}" failed after retries; ${hasNext ? 'falling back to the next provider.' : 'no more providers to try.'}`,
        );
      }
    }

    throw new ProviderFallbackExhaustedException(attempted);
  }
}
