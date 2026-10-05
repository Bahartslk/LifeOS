import { JwtService } from '@nestjs/jwt';
import { UserRole } from '@prisma/client';
import * as bcrypt from 'bcrypt';
import {
  createHttpTestApp,
  foreignKeys,
  HttpTestApp,
  testUser,
} from '../../../test/support/http-test-app';

/**
 * End-to-end authorization for the admin API, through the real guards:
 * `JwtAuthGuard` (signature/expiry) then `RolesGuard` (current role in the
 * database). `GET /api/v1/admin/session` stands for every admin route.
 */
describe('Admin access control (HTTP)', () => {
  const ADMIN_SESSION = '/api/v1/admin/session';

  const admin = testUser({
    email: 'admin@example.com',
    displayName: 'Admin',
    role: UserRole.ADMIN,
  });
  const regular = testUser({ email: 'user@example.com', role: UserRole.USER });
  const deletedAdmin = testUser({
    email: 'gone@example.com',
    role: UserRole.ADMIN,
    deletedAt: new Date('2026-10-02T00:00:00.000Z'),
  });

  let ctx: HttpTestApp;
  const get = (token?: string) => ctx.http.get(ADMIN_SESSION, { token });

  beforeAll(async () => {
    ctx = await createHttpTestApp([admin, regular, deletedAdmin]);
  });

  afterAll(async () => {
    await ctx.close();
  });

  it('rejects a request without a token with 401', async () => {
    const res = await get();

    expect(res.status).toBe(401);
  });

  it.each([
    ['a malformed token', async () => 'not-a-jwt'],
    [
      'an expired token',
      async () => ctx.signAccessToken({ sub: admin.id, role: UserRole.ADMIN }, { expiresIn: -60 }),
    ],
    [
      'a token signed with a different key',
      async () =>
        new JwtService().signAsync(
          { sub: admin.id, email: admin.email, role: UserRole.ADMIN },
          { privateKey: foreignKeys.privateKey, algorithm: 'RS256', expiresIn: '15m' },
        ),
    ],
    [
      'an unsigned ("alg: none") token',
      async () => {
        const part = (value: object) => Buffer.from(JSON.stringify(value)).toString('base64url');
        return `${part({ alg: 'none', typ: 'JWT' })}.${part({ sub: admin.id, role: 'ADMIN', exp: 9999999999 })}.`;
      },
    ],
  ])('rejects %s with 401', async (_name, makeToken) => {
    const res = await get(await makeToken());

    expect(res.status).toBe(401);
  });

  it('rejects a regular USER with 403', async () => {
    const res = await get(await ctx.signAccessToken({ sub: regular.id, role: UserRole.USER }));

    expect(res.status).toBe(403);
    expect(res.body).toMatchObject({ statusCode: 403, error: 'FORBIDDEN' });
    expect(JSON.stringify(res.body)).not.toContain(regular.id);
  });

  it('rejects a USER even when the token itself claims ADMIN (database role wins)', async () => {
    // A validly signed token whose role claim is stale or wrong — e.g. issued before a demotion.
    const res = await get(await ctx.signAccessToken({ sub: regular.id, role: UserRole.ADMIN }));

    expect(res.status).toBe(403);
  });

  it('allows an ADMIN and returns only the session fields', async () => {
    const res = await get(await ctx.signAccessToken({ sub: admin.id, role: UserRole.ADMIN }));

    expect(res.status).toBe(200);
    expect(res.body).toEqual({
      data: { id: admin.id, email: 'admin@example.com', displayName: 'Admin', role: 'ADMIN' },
    });
  });

  it('allows an ADMIN whose token still says USER (promoted after the token was issued)', async () => {
    const res = await get(await ctx.signAccessToken({ sub: admin.id, role: UserRole.USER }));

    expect(res.status).toBe(200);
  });

  it('allows an ADMIN whose token carries no role claim at all (issued before the claim existed)', async () => {
    const res = await get(await ctx.signAccessToken({ sub: admin.id }));

    expect(res.status).toBe(200);
  });

  it('rejects a soft-deleted account with 401, even if it was an admin', async () => {
    const res = await get(
      await ctx.signAccessToken({ sub: deletedAdmin.id, role: UserRole.ADMIN }),
    );

    expect(res.status).toBe(401);
  });

  it('rejects a token for an account that does not exist with 401', async () => {
    const res = await get(
      await ctx.signAccessToken({
        sub: '00000000-0000-4000-8000-000000000000',
        role: UserRole.ADMIN,
      }),
    );

    expect(res.status).toBe(401);
  });

  it('takes effect immediately when a role changes, for the same token', async () => {
    const account = testUser({ email: 'changing@example.com', role: UserRole.USER });
    ctx.prisma.user.findFirst.mockImplementation(async ({ where }: { where: { id?: string } }) =>
      where.id === account.id ? account : null,
    );
    const token = await ctx.signAccessToken({ sub: account.id, role: UserRole.USER });

    expect((await get(token)).status).toBe(403);
    account.role = UserRole.ADMIN;
    expect((await get(token)).status).toBe(200);
    account.role = UserRole.USER;
    expect((await get(token)).status).toBe(403);
  });
});

describe('Role can never be set through the API (HTTP)', () => {
  const regular = testUser({ email: 'user@example.com', role: UserRole.USER });
  const LOGIN_PASSWORD = 'correct-horse-battery';
  const loginAdmin = testUser({
    email: 'admin-login@example.com',
    role: UserRole.ADMIN,
    passwordHash: bcrypt.hashSync(LOGIN_PASSWORD, 4),
  });
  let ctx: HttpTestApp;

  beforeAll(async () => {
    ctx = await createHttpTestApp([regular, loginAdmin]);
  });

  afterAll(async () => {
    await ctx.close();
  });

  it('rejects a register request that tries to set a role, creating nothing', async () => {
    const res = await ctx.http.post('/api/v1/auth/register', {
      body: {
        email: 'attacker@example.com',
        password: 'correct-horse-battery',
        displayName: 'Attacker',
        role: 'ADMIN',
      },
    });

    expect(res.status).toBe(400);
    expect(JSON.stringify(res.body)).toContain('property role should not exist');
    expect(ctx.prisma.user.create).not.toHaveBeenCalled();
  });

  it.each(['/api/v1/users/me', '/api/v1/users/preferences'])(
    'rejects PATCH %s that tries to set a role',
    async (path) => {
      const token = await ctx.signAccessToken({ sub: regular.id, role: UserRole.USER });

      const res = await ctx.http.patch(path, { token, body: { role: 'ADMIN' } });

      expect(res.status).toBe(400);
      expect(JSON.stringify(res.body)).toContain('property role should not exist');
      expect(ctx.prisma.user.updateMany).not.toHaveBeenCalled();
      expect(ctx.prisma.user.update).not.toHaveBeenCalled();
    },
  );

  it('registers a normal account as USER', async () => {
    const res = await ctx.http.post('/api/v1/auth/register', {
      body: {
        email: 'new@example.com',
        password: 'correct-horse-battery',
        displayName: 'New User',
      },
    });

    expect(res.status).toBe(201);
    const created = ctx.prisma.user.create.mock.calls[0][0].data;
    expect(created).not.toHaveProperty('role');
    const payload = JSON.parse(
      Buffer.from(res.body.data.accessToken.split('.')[1], 'base64url').toString(),
    );
    expect(payload.role).toBe('USER');
  });

  it('returns the role on login, in the user object and as a token claim', async () => {
    const res = await ctx.http.post('/api/v1/auth/login', {
      body: { email: 'admin-login@example.com', password: LOGIN_PASSWORD },
    });

    expect(res.status).toBe(200);
    expect(res.body.data.user).toMatchObject({ email: 'admin-login@example.com', role: 'ADMIN' });
    const payload = JSON.parse(
      Buffer.from(res.body.data.accessToken.split('.')[1], 'base64url').toString(),
    );
    expect(payload).toMatchObject({ sub: loginAdmin.id, role: 'ADMIN' });
    expect(JSON.stringify(res.body)).not.toMatch(/passwordHash|password_hash|\$2b\$/);
  });

  it('exposes role on GET /users/me and never the password hash', async () => {
    const token = await ctx.signAccessToken({ sub: regular.id, role: UserRole.USER });

    const res = await ctx.http.get('/api/v1/users/me', { token });

    expect(res.status).toBe(200);
    expect(res.body.data.role).toBe('USER');
    expect(JSON.stringify(res.body)).not.toMatch(/passwordHash|password_hash|\$2b\$/);
  });

  it('leaves routes without @Roles open to any authenticated user', async () => {
    const token = await ctx.signAccessToken({ sub: regular.id, role: UserRole.USER });

    const res = await ctx.http.get('/api/v1/users/me', { token });

    expect(res.status).toBe(200);
  });
});
