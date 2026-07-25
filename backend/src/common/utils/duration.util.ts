/**
 * Parses a short duration string ("30d", "15m", "1h", "45s") into
 * milliseconds. Supports exactly the format already used by this project's
 * TTL env vars (`JWT_ACCESS_TOKEN_TTL`, `JWT_REFRESH_TOKEN_TTL`) — a single
 * integer followed by one of s/m/h/d. Not a general-purpose duration parser
 * (no "2 days", no fractional units, no compound durations); extend only if
 * a real caller needs a format beyond this.
 */
const DURATION_PATTERN = /^(\d+)(s|m|h|d)$/;

const UNIT_TO_MS: Record<string, number> = {
  s: 1_000,
  m: 60_000,
  h: 3_600_000,
  d: 86_400_000,
};

export function parseDurationMs(value: string): number {
  const match = DURATION_PATTERN.exec(value.trim());
  if (!match) {
    throw new Error(
      `Invalid duration format: "${value}". Expected e.g. "30d", "15m", "1h", "45s".`,
    );
  }
  const [, amount, unit] = match;
  return Number(amount) * UNIT_TO_MS[unit];
}
