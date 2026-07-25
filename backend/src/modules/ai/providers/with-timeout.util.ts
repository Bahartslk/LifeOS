/**
 * Runs `operation` with an `AbortSignal` that fires after `timeoutMs`,
 * guaranteeing the timer is cleared either way — shared by every provider
 * (`GeminiProvider`, `OpenRouterProvider`) instead of each reimplementing
 * its own `AbortController`/`setTimeout`/`try`-`finally` boilerplate. A
 * provider only needs to forward the signal into its own SDK/`fetch` call;
 * classifying an abort into `ProviderTimeoutException` stays the caller's
 * job (each provider's own `normalizeError`), since only the caller knows
 * how its underlying client surfaces an aborted request.
 */
export async function withTimeout<T>(
  operation: (signal: AbortSignal) => Promise<T>,
  timeoutMs: number,
): Promise<T> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    return await operation(controller.signal);
  } finally {
    clearTimeout(timer);
  }
}
