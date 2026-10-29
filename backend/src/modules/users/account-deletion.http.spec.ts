import * as bcrypt from 'bcrypt';
import { createHttpTestApp, HttpTestApp, testUser } from '../../../test/support/http-test-app';

const PASSWORD = 'Correct-horse-1';

/**
 * `DELETE /api/v1/users/me` over real HTTP (real AppModule, validation pipe,
 * guards, throttler and error filter; in-memory Prisma). The cascade itself
 * is the database's job (`ON DELETE CASCADE`), checked against Postgres
 * separately; here the stub only records the delete.
 */
describe('DELETE /api/v1/users/me (HTTP)', () => {
  const user = testUser({
    email: 'delete-me@example.com',
    passwordHash: bcrypt.hashSync(PASSWORD, 4),
  });
  let ctx: HttpTestApp;
  let token: string;
  const deleteUser = jest.fn();

  beforeAll(async () => {
    // The throttler counts every request to this route (it runs before auth
    // and validation), so these behavior tests raise the limit; the default
    // limit of 5 has its own test below.
    process.env.THROTTLE_ACCOUNT_DELETION_LIMIT = '100';
    ctx = await createHttpTestApp([user]);
    Object.assign(ctx.prisma.user, { delete: deleteUser });
    token = await ctx.signAccessToken({ sub: user.id, email: user.email });
  });

  afterAll(async () => {
    delete process.env.THROTTLE_ACCOUNT_DELETION_LIMIT;
    await ctx.close();
  });

  beforeEach(() => deleteUser.mockReset().mockResolvedValue(user));

  const remove = (body: unknown, auth = true) =>
    ctx.http.delete('/api/v1/users/me', { token: auth ? token : undefined, body });

  it('requires authentication', async () => {
    const result = await remove({ password: PASSWORD }, false);

    expect(result.status).toBe(401);
    expect(deleteUser).not.toHaveBeenCalled();
  });

  it.each([
    ['no body', undefined],
    ['an empty password', { password: '' }],
    ['an unexpected field', { password: PASSWORD, force: true }],
  ])('rejects %s with 400', async (_label, body) => {
    const result = await remove(body);

    expect(result.status).toBe(400);
    expect(deleteUser).not.toHaveBeenCalled();
  });

  it('rejects a wrong password with 403 and deletes nothing', async () => {
    const result = await remove({ password: 'wrong-password' });

    expect(result.status).toBe(403);
    expect(result.body).toEqual(
      expect.objectContaining({ statusCode: 403, message: 'Password is incorrect.' }),
    );
    expect(deleteUser).not.toHaveBeenCalled();
  });

  it('deletes the account with the right password', async () => {
    const result = await remove({ password: PASSWORD });

    expect(result.status).toBe(200);
    expect(result.body).toEqual({ data: { success: true } });
    expect(deleteUser).toHaveBeenCalledWith({ where: { id: user.id } });
  });
});

describe('DELETE /api/v1/users/me rate limit (HTTP)', () => {
  const user = testUser({ email: 'guess@example.com', passwordHash: bcrypt.hashSync(PASSWORD, 4) });
  let ctx: HttpTestApp;

  beforeAll(async () => {
    ctx = await createHttpTestApp([user]);
    Object.assign(ctx.prisma.user, { delete: jest.fn() });
  });

  afterAll(async () => {
    await ctx.close();
  });

  it('answers 429 after 5 attempts in a minute, so a password can not be guessed', async () => {
    const token = await ctx.signAccessToken({ sub: user.id, email: user.email });
    const statuses: number[] = [];
    for (let i = 0; i < 6; i++) {
      const result = await ctx.http.delete('/api/v1/users/me', {
        token,
        body: { password: `guess-${i}` },
      });
      statuses.push(result.status);
    }

    expect(statuses).toEqual([403, 403, 403, 403, 403, 429]);
  });
});
