import { Injectable, Logger } from '@nestjs/common';
import { AiConfigService } from '../config/ai-config.service';
import {
  ProviderTimeoutException,
  ProviderUnavailableException,
} from '../exceptions/ai.exceptions';

export interface RetryOutcome<T> {
  result: T;
  retryCount: number;
}

/** Backoff base delay, per this module's Retry Strategy ("exponential backoff and jitter") — doubles each attempt (`RETRY_BASE_DELAY_MS * 2 ** attempt`). Not deployment-tunable via env: this is a low-level algorithm constant, not an operational setting (unlike `AI_RETRY_COUNT`, which bounds how many attempts happen at all). */
const RETRY_BASE_DELAY_MS = 250;

/** +/- this fraction of the base delay, applied as random jitter, to avoid many concurrent requests retrying in lockstep. */
const RETRY_JITTER_RATIO = 0.2;

/**
 * Central retry policy for every provider call — per
 * docs/12-project-architecture.md#retry-strategy, "transient failures
 * (timeouts, 5xx) are retried a small, bounded number of times with
 * exponential backoff and jitter." Only `ProviderTimeoutException` and
 * `ProviderUnavailableException` (Gemini 5xx / provider-outage, per
 * `GeminiProvider.normalizeError`) are treated as transient —
 * `ProviderRateLimitException` is deliberately excluded, since
 * docs/12-project-architecture.md#error-handling requires a rate limit to
 * be "rejected immediately ... not silently retried in a way that could
 * compound the limit," and `InvalidProviderResponseException` is a
 * template/context bug, not a transient failure, so retrying it would
 * just repeat the same failure. Providers never implement their own retry
 * loop — they only need to throw the right exception type for this
 * executor to classify correctly.
 */
@Injectable()
export class AiRetryExecutor {
  private readonly logger = new Logger(AiRetryExecutor.name);

  constructor(private readonly aiConfig: AiConfigService) {}

  async execute<T>(operation: () => Promise<T>): Promise<RetryOutcome<T>> {
    const maxRetries = this.aiConfig.retryCount;
    let attempt = 0;

    for (;;) {
      try {
        const result = await operation();
        return { result, retryCount: attempt };
      } catch (error) {
        if (attempt >= maxRetries || !this.isTransient(error)) {
          throw error;
        }
        const delayMs = this.backoffDelay(attempt);
        this.logger.warn(
          `AI provider call failed (attempt ${attempt + 1}/${maxRetries + 1}), retrying in ${delayMs}ms.`,
        );
        await this.sleep(delayMs);
        attempt += 1;
      }
    }
  }

  private isTransient(error: unknown): boolean {
    return (
      error instanceof ProviderTimeoutException || error instanceof ProviderUnavailableException
    );
  }

  /** Exponential backoff with jitter, per this module's Retry Strategy ("bounded ... exponential backoff and jitter"). */
  private backoffDelay(attempt: number): number {
    const base = RETRY_BASE_DELAY_MS * 2 ** attempt;
    const jitter = base * RETRY_JITTER_RATIO * (Math.random() * 2 - 1);
    return Math.round(base + jitter);
  }

  private sleep(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
}
