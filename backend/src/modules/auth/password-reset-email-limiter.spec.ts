import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../config/configuration';
import { PasswordResetEmailLimiter } from './password-reset-email-limiter';

const HOUR_MS = 60 * 60 * 1000;

describe('PasswordResetEmailLimiter', () => {
  const config = {
    get: () => ({ codeTtlMinutes: 15, maxAttempts: 5, emailHourlyLimit: 3 }),
  } as unknown as ConfigService<AppConfig, true>;

  it('allows 3 requests per address per hour, ignoring case and spaces', () => {
    const limiter = new PasswordResetEmailLimiter(config);
    const start = 1_000_000;

    expect(limiter.tryConsume('ada@example.com', start)).toBe(true);
    expect(limiter.tryConsume('ADA@example.com', start + 1)).toBe(true);
    expect(limiter.tryConsume(' ada@example.com ', start + 2)).toBe(true);
    expect(limiter.tryConsume('ada@example.com', start + 3)).toBe(false);
    expect(limiter.tryConsume('other@example.com', start + 4)).toBe(true);
  });

  it('frees a slot once the oldest request is an hour old', () => {
    const limiter = new PasswordResetEmailLimiter(config);
    const start = 1_000_000;
    [0, 1, 2].forEach((offset) => limiter.tryConsume('ada@example.com', start + offset));

    expect(limiter.tryConsume('ada@example.com', start + HOUR_MS - 1)).toBe(false);
    expect(limiter.tryConsume('ada@example.com', start + HOUR_MS)).toBe(true);
  });
});
