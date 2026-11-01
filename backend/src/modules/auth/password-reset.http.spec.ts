import * as bcrypt from 'bcrypt';
import { createHttpTestApp, HttpTestApp, testUser } from '../../../test/support/http-test-app';
import { MailMessage, MailService } from '../mail/mail.service';
import { PasswordResetEmailLimiter } from './password-reset-email-limiter';

const OLD_PASSWORD = 'Old-password-1';
const NEW_PASSWORD = 'New-password-2';

/** Polls until `check` passes; the code is issued after the forgot-password response. */
async function eventually(check: () => boolean): Promise<void> {
  for (let i = 0; i < 200 && !check(); i++) {
    await new Promise((resolve) => setTimeout(resolve, 10));
  }
  expect(check()).toBe(true);
}

/**
 * `POST /api/v1/auth/forgot-password` and `/reset-password` over real HTTP
 * (real AppModule, validation, throttler, error filter; in-memory Prisma).
 * The mail service is the real test-environment one (it only logs), spied
 * on to read the code a user would get.
 */
describe('password reset (HTTP)', () => {
  const user = testUser({
    email: 'forgetful@example.com',
    passwordHash: bcrypt.hashSync(OLD_PASSWORD, 4),
  });
  let ctx: HttpTestApp;
  let sent: MailMessage[];
  const updateUsers = jest.fn();
  const revokeTokens = jest.fn();
  let limiterSpy: jest.SpyInstance;

  beforeAll(async () => {
    process.env.THROTTLE_FORGOT_PASSWORD_LIMIT = '100';
    process.env.THROTTLE_RESET_PASSWORD_LIMIT = '100';
    process.env.BCRYPT_SALT_ROUNDS = '10';
    ctx = await createHttpTestApp([user]);
    Object.assign(ctx.prisma.user, { updateMany: updateUsers });
    Object.assign(ctx.prisma.refreshToken, { updateMany: revokeTokens });
    jest.spyOn(ctx.app.get(MailService), 'send').mockImplementation(async (message) => {
      sent.push(message);
    });
  });

  afterEach(() => limiterSpy.mockRestore());

  afterAll(async () => {
    delete process.env.THROTTLE_FORGOT_PASSWORD_LIMIT;
    delete process.env.THROTTLE_RESET_PASSWORD_LIMIT;
    await ctx.close();
  });

  beforeEach(() => {
    // The per-address hourly limit has its own test below; here it would carry over between tests.
    limiterSpy = jest
      .spyOn(ctx.app.get(PasswordResetEmailLimiter), 'tryConsume')
      .mockReturnValue(true);
    sent = [];
    updateUsers.mockReset().mockResolvedValue({ count: 1 });
    revokeTokens.mockReset().mockResolvedValue({ count: 2 });
  });

  const forgot = (body: unknown) => ctx.http.post('/api/v1/auth/forgot-password', { body });
  const reset = (body: unknown) => ctx.http.post('/api/v1/auth/reset-password', { body });

  async function requestCode(email: string): Promise<string> {
    const result = await forgot({ email });
    expect(result.status).toBe(200);
    await eventually(() => sent.length > 0);
    return /\b(\d{6})\b/.exec(sent[sent.length - 1].text)![1];
  }

  it.each([
    ['no body', undefined],
    ['a malformed email', { email: 'not-an-email' }],
  ])('rejects %s with 400', async (_label, body) => {
    expect((await forgot(body)).status).toBe(400);
  });

  it('answers a registered and an unknown address the same way, and emails only the registered one', async () => {
    const unknown = await forgot({ email: 'nobody-here@example.com' });
    const known = await forgot({ email: 'Forgetful@Example.com' });

    expect(unknown.status).toBe(200);
    expect(known.status).toBe(200);
    expect(unknown.body).toEqual(known.body);
    await eventually(() => sent.length === 1);
    expect(sent[0].to.email).toBe(user.email);
  });

  it('resets the password with the emailed code and signs out every session', async () => {
    const code = await requestCode(user.email);

    const result = await reset({ email: user.email, code, newPassword: NEW_PASSWORD });

    expect(result.status).toBe(200);
    expect(result.body).toEqual({ data: { success: true } });
    const [{ data }] = updateUsers.mock.calls[0];
    expect(bcrypt.compareSync(NEW_PASSWORD, data.passwordHash)).toBe(true);
    expect(revokeTokens).toHaveBeenCalledWith({
      where: { userId: user.id, revokedAt: null },
      data: { revokedAt: expect.any(Date) },
    });

    // The same code can't be used twice.
    const again = await reset({ email: user.email, code, newPassword: 'Another-password-3' });
    expect(again.status).toBe(400);
  });

  it('only the newest code works after asking twice', async () => {
    const first = await requestCode(user.email);
    sent = [];
    const second = await requestCode(user.email);

    const withOld = await reset({ email: user.email, code: first, newPassword: NEW_PASSWORD });
    expect(withOld.status).toBe(first === second ? 200 : 400);
    if (first !== second) {
      expect(
        (await reset({ email: user.email, code: second, newPassword: NEW_PASSWORD })).status,
      ).toBe(200);
    }
  });

  it('answers a wrong code with 400 INVALID_RESET_CODE, and stops accepting the code after 5 wrong tries', async () => {
    const code = await requestCode(user.email);
    const wrong = code === '000000' ? '111111' : '000000';

    for (let attempt = 0; attempt < 5; attempt++) {
      const result = await reset({ email: user.email, code: wrong, newPassword: NEW_PASSWORD });
      expect(result.status).toBe(400);
      expect(result.body).toEqual(expect.objectContaining({ code: 'INVALID_RESET_CODE' }));
    }

    const right = await reset({ email: user.email, code, newPassword: NEW_PASSWORD });
    expect(right.status).toBe(400);
    expect(updateUsers).not.toHaveBeenCalled();
  });

  it('answers an unknown address on reset with the same 400', async () => {
    const result = await reset({
      email: 'nobody-here@example.com',
      code: '123456',
      newPassword: NEW_PASSWORD,
    });

    expect(result.status).toBe(400);
    expect(result.body).toEqual(expect.objectContaining({ code: 'INVALID_RESET_CODE' }));
  });

  it.each([
    ['a 5-digit code', { email: user.email, code: '12345', newPassword: NEW_PASSWORD }],
    ['a non-numeric code', { email: user.email, code: '12a456', newPassword: NEW_PASSWORD }],
    ['a short new password', { email: user.email, code: '123456', newPassword: 'short' }],
    ['a new password over 72', { email: user.email, code: '123456', newPassword: 'x'.repeat(73) }],
  ])('rejects %s with 400 before checking any code', async (_label, body) => {
    const result = await reset(body);

    expect(result.status).toBe(400);
    expect(result.body.code).toBeUndefined();
  });

  it('allows 3 codes per address per hour, then 429 PASSWORD_RESET_EMAIL_LIMIT for that address only', async () => {
    limiterSpy.mockRestore();
    const email = 'limited-address@example.com';
    for (let attempt = 0; attempt < 3; attempt++) {
      expect((await forgot({ email })).status).toBe(200);
    }

    const blocked = await forgot({ email: email.toUpperCase() });
    expect(blocked.status).toBe(429);
    expect(blocked.body).toEqual(expect.objectContaining({ code: 'PASSWORD_RESET_EMAIL_LIMIT' }));
    expect((await forgot({ email: 'another-address@example.com' })).status).toBe(200);
  });
});

describe('forgot-password per-IP rate limit (HTTP)', () => {
  let ctx: HttpTestApp;

  beforeAll(async () => {
    ctx = await createHttpTestApp([]);
  });

  afterAll(async () => {
    await ctx.close();
  });

  it('allows 5 requests a minute from one IP, then answers 429', async () => {
    const statuses: number[] = [];
    for (let attempt = 0; attempt < 6; attempt++) {
      const result = await ctx.http.post('/api/v1/auth/forgot-password', {
        body: { email: `ip-limit-${attempt}@example.com` },
      });
      statuses.push(result.status);
    }

    expect(statuses).toEqual([200, 200, 200, 200, 200, 429]);
  });
});
