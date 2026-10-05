import { createHttpTestApp, HttpTestApp, testUser } from '../test/support/http-test-app';

// Read by ConfigModule when app.module is first imported, so set before any app is created.
process.env.CORS_ALLOWED_ORIGINS = 'https://admin.lifeos.example, http://localhost:5173';
process.env.TRUST_PROXY_HOPS = '1';

const LIMIT_KEYS = [
  'THROTTLE_LIMIT',
  'THROTTLE_LOGIN_LIMIT',
  'THROTTLE_REGISTER_LIMIT',
  'THROTTLE_REFRESH_LIMIT',
] as const;

describe('Rate limiting (HTTP)', () => {
  let ctx: HttpTestApp;

  // A fresh app per test: the throttler's counters live in the app instance.
  beforeEach(async () => {
    LIMIT_KEYS.forEach((key) => delete process.env[key]);
    ctx = await createHttpTestApp([testUser({ email: 'user@example.com' })]);
  });

  afterEach(async () => {
    await ctx.close();
    LIMIT_KEYS.forEach((key) => delete process.env[key]);
  });

  const login = (ip?: string) =>
    ctx.http.post('/api/v1/auth/login', {
      body: { email: 'nobody@example.com', password: 'wrong-password' },
      headers: ip ? { 'X-Forwarded-For': ip } : {},
    });

  it('allows 10 login attempts per minute and answers the 11th with 429', async () => {
    for (let attempt = 1; attempt <= 10; attempt++) {
      expect((await login()).status).toBe(401);
    }

    const blocked = await login();

    expect(blocked.status).toBe(429);
    expect(blocked.body).toMatchObject({ statusCode: 429 });
    expect(Number(blocked.headers['retry-after'])).toBeGreaterThan(0);
  }, 30_000);

  it('applies its own limit to register and to refresh', async () => {
    process.env.THROTTLE_REGISTER_LIMIT = '2';
    process.env.THROTTLE_REFRESH_LIMIT = '3';
    const register = () =>
      ctx.http.post('/api/v1/auth/register', { body: { email: 'not-an-email' } });
    const refresh = () =>
      ctx.http.post('/api/v1/auth/refresh', { body: { refreshToken: 'unknown' } });

    expect((await register()).status).toBe(400);
    expect((await register()).status).toBe(400);
    expect((await register()).status).toBe(429);

    expect((await refresh()).status).toBe(401);
    expect((await refresh()).status).toBe(401);
    expect((await refresh()).status).toBe(401);
    expect((await refresh()).status).toBe(429);
  });

  it('applies the general limit to ordinary routes but never to /health', async () => {
    process.env.THROTTLE_LIMIT = '3';
    const dashboard = () => ctx.http.get('/api/v1/planner/dashboard');

    expect((await dashboard()).status).toBe(401);
    expect((await dashboard()).status).toBe(401);
    expect((await dashboard()).status).toBe(401);
    expect((await dashboard()).status).toBe(429);

    for (let i = 0; i < 10; i++) {
      expect((await ctx.http.get('/health')).status).toBe(200);
    }
  });

  it('counts each client IP separately behind the reverse proxy (trust proxy)', async () => {
    process.env.THROTTLE_LOGIN_LIMIT = '2';

    expect((await login('203.0.113.10')).status).toBe(401);
    expect((await login('203.0.113.10')).status).toBe(401);
    expect((await login('203.0.113.10')).status).toBe(429);

    // A different client is unaffected by the first one's exhausted budget.
    expect((await login('198.51.100.7')).status).toBe(401);
  }, 30_000);

  it('reports the limit to the client in response headers', async () => {
    const res = await login();

    expect(res.headers['x-ratelimit-limit']).toBe('10');
    expect(res.headers['x-ratelimit-remaining']).toBe('9');
  });
});

describe('CORS allow-list (HTTP)', () => {
  let ctx: HttpTestApp;

  beforeAll(async () => {
    ctx = await createHttpTestApp();
  });

  afterAll(async () => {
    await ctx.close();
  });

  const preflight = (origin: string) =>
    ctx.http.options('/api/v1/admin/session', {
      headers: {
        Origin: origin,
        'Access-Control-Request-Method': 'GET',
        'Access-Control-Request-Headers': 'authorization',
      },
    });

  it.each(['https://admin.lifeos.example', 'http://localhost:5173'])(
    'allows the configured origin %s',
    async (origin) => {
      const res = await preflight(origin);

      expect(res.headers['access-control-allow-origin']).toBe(origin);
      expect(res.headers['access-control-allow-headers']).toContain('Authorization');
      expect(res.headers['access-control-allow-credentials']).toBeUndefined();
    },
  );

  it.each(['https://evil.example', 'https://admin.lifeos.example.evil.example', 'null'])(
    'gives no CORS permission to the unlisted origin %s',
    async (origin) => {
      const res = await preflight(origin);

      expect(res.headers['access-control-allow-origin']).toBeUndefined();
    },
  );

  it('still serves requests that carry no Origin at all (mobile clients, curl)', async () => {
    const res = await ctx.http.get('/health');

    expect(res.status).toBe(200);
    expect(res.headers['access-control-allow-origin']).toBeUndefined();
  });
});
