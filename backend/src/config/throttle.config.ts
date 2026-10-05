/**
 * Rate-limit settings, per client IP, over a one-minute window.
 *
 * Each limit is a function rather than a constant because `@Throttle(...)`
 * decorators are evaluated when their controller file is first imported —
 * before `ConfigModule` has loaded `.env` — so a plain value would freeze
 * the fallback in. `@nestjs/throttler` calls these per request instead.
 * Like `configuration.ts`, this file lives in `config/` because it is one
 * of the few places allowed to read `process.env`.
 *
 * Defaults: 300/min overall (a single Home screen load is 4-5 requests,
 * and mobile carriers put many users behind one IP), and much tighter
 * limits on the credential endpoints, where the point is to make password
 * guessing and mass sign-up impractical.
 */
export const THROTTLE_WINDOW_MS = 60_000;

function limitFromEnv(key: string, fallback: number): () => number {
  return () => {
    const parsed = Number.parseInt(process.env[key] ?? '', 10);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
  };
}

export const throttleLimits = {
  global: limitFromEnv('THROTTLE_LIMIT', 300),
  login: limitFromEnv('THROTTLE_LOGIN_LIMIT', 10),
  register: limitFromEnv('THROTTLE_REGISTER_LIMIT', 10),
  refresh: limitFromEnv('THROTTLE_REFRESH_LIMIT', 30),
};
