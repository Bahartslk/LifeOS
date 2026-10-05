import { buildCorsOptions } from './app.setup';
import configuration from './config/configuration';
import { throttleLimits, THROTTLE_WINDOW_MS } from './config/throttle.config';

describe('buildCorsOptions', () => {
  it('allows exactly the configured origins, without credentials', () => {
    const options = buildCorsOptions(['https://admin.lifeos.example']);

    expect(options.origin).toEqual(['https://admin.lifeos.example']);
    expect(options.credentials).toBe(false);
    expect(options.allowedHeaders).toEqual(['Authorization', 'Content-Type']);
  });

  it('disables cross-origin access entirely when nothing is configured', () => {
    expect(buildCorsOptions([]).origin).toBe(false);
  });
});

describe('http configuration', () => {
  const KEYS = ['NODE_ENV', 'CORS_ALLOWED_ORIGINS', 'TRUST_PROXY_HOPS'] as const;
  const saved: Record<string, string | undefined> = {};

  beforeEach(() => KEYS.forEach((key) => (saved[key] = process.env[key])));
  afterEach(() =>
    KEYS.forEach((key) =>
      saved[key] === undefined ? delete process.env[key] : (process.env[key] = saved[key]),
    ),
  );

  it("defaults to Vite's dev servers outside production", () => {
    process.env.NODE_ENV = 'development';
    delete process.env.CORS_ALLOWED_ORIGINS;

    expect(configuration().http.corsAllowedOrigins).toEqual([
      'http://localhost:5173',
      'http://localhost:4173',
    ]);
  });

  it('allows no origin in production unless one is configured', () => {
    process.env.NODE_ENV = 'production';
    delete process.env.CORS_ALLOWED_ORIGINS;
    expect(configuration().http.corsAllowedOrigins).toEqual([]);

    process.env.CORS_ALLOWED_ORIGINS = '';
    expect(configuration().http.corsAllowedOrigins).toEqual([]);
  });

  it('parses a comma-separated allow-list', () => {
    process.env.NODE_ENV = 'production';
    process.env.CORS_ALLOWED_ORIGINS = 'https://admin.lifeos.example , https://staging.example,';

    expect(configuration().http.corsAllowedOrigins).toEqual([
      'https://admin.lifeos.example',
      'https://staging.example',
    ]);
  });

  it('trusts one proxy hop by default', () => {
    delete process.env.TRUST_PROXY_HOPS;
    expect(configuration().http.trustProxyHops).toBe(1);
  });
});

describe('throttleLimits', () => {
  const KEYS = [
    'THROTTLE_LIMIT',
    'THROTTLE_LOGIN_LIMIT',
    'THROTTLE_REGISTER_LIMIT',
    'THROTTLE_REFRESH_LIMIT',
  ] as const;

  afterEach(() => KEYS.forEach((key) => delete process.env[key]));

  it('uses the approved defaults over a one-minute window', () => {
    KEYS.forEach((key) => delete process.env[key]);

    expect(THROTTLE_WINDOW_MS).toBe(60_000);
    expect(throttleLimits.global()).toBe(300);
    expect(throttleLimits.login()).toBe(10);
    expect(throttleLimits.register()).toBe(10);
    expect(throttleLimits.refresh()).toBe(30);
  });

  it('reads an override from the environment at call time', () => {
    process.env.THROTTLE_LOGIN_LIMIT = '25';

    expect(throttleLimits.login()).toBe(25);
  });

  it.each(['0', '-5', 'abc', ''])('falls back to the default for the invalid value %j', (value) => {
    process.env.THROTTLE_LOGIN_LIMIT = value;

    expect(throttleLimits.login()).toBe(10);
  });
});
