import { createHash } from 'crypto';
import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../config/configuration';

const WINDOW_MS = 60 * 60 * 1000;
/** Above this many tracked addresses, stale entries are swept on the next call. */
const SWEEP_THRESHOLD = 10_000;

/**
 * How many reset codes may be requested per email address per hour
 * (`PASSWORD_RESET_EMAIL_HOURLY_LIMIT`), counted for every address, known or
 * not, so the limit itself reveals nothing about which emails have an
 * account. Kept in memory like the per-IP throttler: it resets on restart
 * and assumes a single instance. Addresses are stored only as SHA-256 hashes.
 */
@Injectable()
export class PasswordResetEmailLimiter {
  private readonly requests = new Map<string, number[]>();

  constructor(private readonly config: ConfigService<AppConfig, true>) {}

  /** Records a request for `email`; `false` if the address is already at its hourly limit. */
  tryConsume(email: string, now: number = Date.now()): boolean {
    const limit = this.config.get('passwordReset', { infer: true }).emailHourlyLimit;
    const key = createHash('sha256').update(email.trim().toLowerCase()).digest('hex');
    if (this.requests.size > SWEEP_THRESHOLD) this.sweep(now);

    const recent = (this.requests.get(key) ?? []).filter((time) => now - time < WINDOW_MS);
    if (recent.length >= limit) {
      this.requests.set(key, recent);
      return false;
    }
    recent.push(now);
    this.requests.set(key, recent);
    return true;
  }

  private sweep(now: number): void {
    for (const [key, times] of this.requests) {
      if (times.every((time) => now - time >= WINDOW_MS)) this.requests.delete(key);
    }
  }
}
