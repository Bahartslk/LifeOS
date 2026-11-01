import * as bcrypt from 'bcrypt';
import { createHttpTestApp, HttpTestApp, testUser } from '../../../test/support/http-test-app';

const PASSWORD = 'Current-pass-1';

/**
 * `PUT`/`DELETE /api/v1/notifications/devices` over real HTTP (real
 * AppModule, validation, guards; in-memory Prisma), plus the password
 * change forgetting the user's devices.
 */
describe('push devices (HTTP)', () => {
  const ada = testUser({
    email: 'ada-devices@example.com',
    passwordHash: bcrypt.hashSync(PASSWORD, 4),
  });
  const bob = testUser({ email: 'bob-devices@example.com' });
  let ctx: HttpTestApp;
  let adaToken: string;
  let bobToken: string;

  beforeAll(async () => {
    process.env.THROTTLE_PASSWORD_CHANGE_LIMIT = '100';
    ctx = await createHttpTestApp([ada, bob]);
    Object.assign(ctx.prisma.user, { updateMany: jest.fn(async () => ({ count: 1 })) });
    adaToken = await ctx.signAccessToken({ sub: ada.id, email: ada.email });
    bobToken = await ctx.signAccessToken({ sub: bob.id, email: bob.email });
  });

  afterAll(async () => {
    delete process.env.THROTTLE_PASSWORD_CHANGE_LIMIT;
    await ctx.close();
  });

  beforeEach(() => ctx.prisma.deviceToken.rows.splice(0));

  const register = (body: unknown, token?: string) =>
    ctx.http.put('/api/v1/notifications/devices', { token, body });
  const unregister = (body: unknown) => ctx.http.delete('/api/v1/notifications/devices', { body });
  const rows = () =>
    ctx.prisma.deviceToken.rows.map((row) => ({ userId: row.userId, token: row.token }));

  it('requires an access token to register', async () => {
    expect((await register({ token: 'fcm-1', platform: 'ANDROID' })).status).toBe(401);
    expect(rows()).toEqual([]);
  });

  it.each([
    ['no token', { platform: 'ANDROID' }],
    ['an empty token', { token: '', platform: 'ANDROID' }],
    ['an unknown platform', { token: 'fcm-1', platform: 'WINDOWS' }],
    ['an unexpected field', { token: 'fcm-1', platform: 'ANDROID', userId: 'someone' }],
  ])('rejects %s with 400', async (_label, body) => {
    expect((await register(body, adaToken)).status).toBe(400);
  });

  it('registers a device, and registering again changes nothing', async () => {
    const first = await register({ token: 'fcm-1', platform: 'ANDROID' }, adaToken);
    await register({ token: 'fcm-1', platform: 'ANDROID' }, adaToken);

    expect(first.status).toBe(200);
    expect(first.body).toEqual({ data: { success: true } });
    expect(rows()).toEqual([{ userId: ada.id, token: 'fcm-1' }]);
  });

  it('moves a token to the user who signs in on that device', async () => {
    await register({ token: 'shared-phone', platform: 'ANDROID' }, adaToken);
    await register({ token: 'shared-phone', platform: 'ANDROID' }, bobToken);

    expect(rows()).toEqual([{ userId: bob.id, token: 'shared-phone' }]);
  });

  it('unregisters without an access token, answering the same for an unknown token', async () => {
    await register({ token: 'fcm-1', platform: 'ANDROID' }, adaToken);

    const known = await unregister({ token: 'fcm-1' });
    const unknown = await unregister({ token: 'never-seen' });

    expect(known.status).toBe(200);
    expect(unknown.body).toEqual(known.body);
    expect(rows()).toEqual([]);
  });

  it('forgets every device of the user when the password changes', async () => {
    await register({ token: 'ada-phone', platform: 'ANDROID' }, adaToken);
    await register({ token: 'ada-tablet', platform: 'ANDROID' }, adaToken);
    await register({ token: 'bob-phone', platform: 'ANDROID' }, bobToken);

    const result = await ctx.http.patch('/api/v1/users/me/password', {
      token: adaToken,
      body: { currentPassword: PASSWORD, newPassword: 'Brand-new-pass-2' },
    });

    expect(result.status).toBe(200);
    expect(rows()).toEqual([{ userId: bob.id, token: 'bob-phone' }]);
  });
});

describe('DELETE /api/v1/notifications/devices rate limit (HTTP)', () => {
  let ctx: HttpTestApp;

  beforeAll(async () => {
    ctx = await createHttpTestApp([]);
  });

  afterAll(async () => {
    await ctx.close();
  });

  it('allows 10 requests a minute from one IP, then answers 429', async () => {
    const statuses: number[] = [];
    for (let attempt = 0; attempt < 11; attempt++) {
      const result = await ctx.http.delete('/api/v1/notifications/devices', {
        body: { token: `guess-${attempt}` },
      });
      statuses.push(result.status);
    }

    expect(statuses).toEqual([...Array<number>(10).fill(200), 429]);
  });
});
