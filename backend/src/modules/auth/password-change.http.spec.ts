import * as bcrypt from 'bcrypt';
import { createHttpTestApp, HttpTestApp, testUser } from '../../../test/support/http-test-app';

const PASSWORD = 'Current-pass-1';
const NEW_PASSWORD = 'Brand-new-pass-2';

/**
 * `PATCH /api/v1/users/me/password` over real HTTP (real AppModule,
 * validation pipe, guards, throttler and error filter; in-memory Prisma
 * whose writes are recorded).
 */
describe('PATCH /api/v1/users/me/password (HTTP)', () => {
  const user = testUser({
    email: 'change-pass@example.com',
    passwordHash: bcrypt.hashSync(PASSWORD, 4),
  });
  let ctx: HttpTestApp;
  let token: string;
  const updateUsers = jest.fn();
  const revokeTokens = jest.fn();

  beforeAll(async () => {
    // Every request to the route counts against its limit, so the behavior
    // tests raise it; the default limit has its own test below.
    process.env.THROTTLE_PASSWORD_CHANGE_LIMIT = '100';
    ctx = await createHttpTestApp([user]);
    Object.assign(ctx.prisma.user, { updateMany: updateUsers });
    Object.assign(ctx.prisma.refreshToken, { updateMany: revokeTokens });
    token = await ctx.signAccessToken({ sub: user.id, email: user.email });
  });

  afterAll(async () => {
    delete process.env.THROTTLE_PASSWORD_CHANGE_LIMIT;
    await ctx.close();
  });

  beforeEach(() => {
    updateUsers.mockReset().mockResolvedValue({ count: 1 });
    revokeTokens.mockReset().mockResolvedValue({ count: 2 });
    ctx.prisma.refreshToken.create.mockClear();
  });

  const change = (body: unknown, auth = true) =>
    ctx.http.patch('/api/v1/users/me/password', { token: auth ? token : undefined, body });

  it('requires authentication', async () => {
    const result = await change({ currentPassword: PASSWORD, newPassword: NEW_PASSWORD }, false);

    expect(result.status).toBe(401);
    expect(updateUsers).not.toHaveBeenCalled();
  });

  it.each([
    ['no body', undefined],
    ['a missing current password', { newPassword: NEW_PASSWORD }],
    ['a new password shorter than 8', { currentPassword: PASSWORD, newPassword: 'short' }],
    ['a new password longer than 72', { currentPassword: PASSWORD, newPassword: 'x'.repeat(73) }],
    [
      'an unexpected field',
      { currentPassword: PASSWORD, newPassword: NEW_PASSWORD, signOutOthers: false },
    ],
  ])('rejects %s with 400', async (_label, body) => {
    const result = await change(body);

    expect(result.status).toBe(400);
    expect(updateUsers).not.toHaveBeenCalled();
  });

  it('rejects a wrong current password with 403 and changes nothing', async () => {
    const result = await change({ currentPassword: 'wrong-password', newPassword: NEW_PASSWORD });

    expect(result.status).toBe(403);
    expect(result.body).toEqual(
      expect.objectContaining({ statusCode: 403, message: 'Current password is incorrect.' }),
    );
    expect(updateUsers).not.toHaveBeenCalled();
    expect(revokeTokens).not.toHaveBeenCalled();
  });

  it('rejects reusing the current password with 400', async () => {
    const result = await change({ currentPassword: PASSWORD, newPassword: PASSWORD });

    expect(result.status).toBe(400);
    expect(updateUsers).not.toHaveBeenCalled();
  });

  it('changes the password, signs out every session and returns a new token pair', async () => {
    const result = await change({ currentPassword: PASSWORD, newPassword: NEW_PASSWORD });

    expect(result.status).toBe(200);
    expect(result.body).toEqual({
      data: { accessToken: expect.any(String), refreshToken: expect.any(String) },
    });
    const [{ where, data }] = updateUsers.mock.calls[0];
    expect(where).toEqual({ id: user.id, deletedAt: null });
    expect(bcrypt.compareSync(NEW_PASSWORD, data.passwordHash)).toBe(true);
    expect(revokeTokens).toHaveBeenCalledWith({
      where: { userId: user.id, revokedAt: null },
      data: { revokedAt: expect.any(Date) },
    });
    expect(ctx.prisma.refreshToken.create).toHaveBeenCalledTimes(1);
    expect(JSON.stringify(result.body)).not.toContain(data.passwordHash);
  });
});

describe('PATCH /api/v1/users/me/password rate limit (HTTP)', () => {
  const user = testUser({
    email: 'guess-pass@example.com',
    passwordHash: bcrypt.hashSync(PASSWORD, 4),
  });
  let ctx: HttpTestApp;
  let token: string;

  beforeAll(async () => {
    ctx = await createHttpTestApp([user]);
    token = await ctx.signAccessToken({ sub: user.id, email: user.email });
  });

  afterAll(async () => {
    await ctx.close();
  });

  it('allows 5 attempts a minute, then answers 429', async () => {
    const statuses: number[] = [];
    for (let attempt = 0; attempt < 6; attempt++) {
      const result = await ctx.http.patch('/api/v1/users/me/password', {
        token,
        body: { currentPassword: 'wrong-password', newPassword: NEW_PASSWORD },
      });
      statuses.push(result.status);
    }

    expect(statuses).toEqual([403, 403, 403, 403, 403, 429]);
  });
});
