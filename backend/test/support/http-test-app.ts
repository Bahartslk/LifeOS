import { generateKeyPairSync, randomUUID } from 'crypto';
import { AddressInfo } from 'net';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { NestExpressApplication } from '@nestjs/platform-express';
import { Test } from '@nestjs/testing';
import { ThemeMode, User, UserRole } from '@prisma/client';
import { AdminSeed, createAdminPrismaDelegates } from './in-memory-admin-prisma';

/**
 * Boots the REAL `AppModule` behind the REAL HTTP pipeline (`configureApp`:
 * validation, prefix, versioning, CORS, proxy trust; plus the global
 * throttler, `JwtAuthGuard` and `RolesGuard`) for HTTP-level specs. Only
 * `PrismaService` is replaced, by an in-memory user store (plus, for the
 * admin API, the read-only delegates of `in-memory-admin-prisma`), so no
 * database is needed. Access tokens are signed with a throwaway RSA key pair
 * generated here — never a real key. The app listens on a random local
 * port and specs talk to it over real HTTP with Node's built-in `fetch`.
 *
 * `ConfigModule` reads the environment once, when `app.module` is first
 * imported in a spec file, so anything read through `ConfigService`
 * (e.g. CORS origins) must be set in `process.env` before the first call
 * in that file. Rate limits are read per request and can be changed any
 * time.
 */

const signingKeys = generateKeyPairSync('rsa', {
  modulusLength: 2048,
  publicKeyEncoding: { type: 'spki', format: 'pem' },
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' },
});

/** A second, unrelated key pair — for "token signed by someone else" cases. */
export const foreignKeys = generateKeyPairSync('rsa', {
  modulusLength: 2048,
  publicKeyEncoding: { type: 'spki', format: 'pem' },
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' },
});

export function testUser(overrides: Partial<User> = {}): User {
  const id = overrides.id ?? randomUUID();
  return {
    id,
    email: `${id}@example.com`,
    passwordHash: '$2b$10$not-a-real-hash',
    displayName: 'Test User',
    avatarUrl: null,
    bio: null,
    timezone: 'Europe/Istanbul',
    language: 'tr',
    themePreference: ThemeMode.SYSTEM,
    notificationPreferences: { taskReminders: true, tripReminders: true, aiInsights: true },
    role: UserRole.USER,
    createdAt: new Date('2026-10-01T10:00:00.000Z'),
    updatedAt: new Date('2026-10-01T10:00:00.000Z'),
    deletedAt: null,
    ...overrides,
  };
}

type UserWhere = {
  id?: string;
  email?: { equals: string; mode?: string };
  deletedAt?: null;
};

function createPrismaStub(users: User[], adminSeed: AdminSeed) {
  const admin = createAdminPrismaDelegates(users, adminSeed);

  const matches = (user: User, where: UserWhere) =>
    (where.id === undefined || user.id === where.id) &&
    (where.email === undefined || user.email.toLowerCase() === where.email.equals.toLowerCase()) &&
    (!('deletedAt' in where) || user.deletedAt === null);

  return {
    onModuleInit: async () => undefined,
    onModuleDestroy: async () => undefined,
    $connect: async () => undefined,
    $disconnect: async () => undefined,
    user: {
      ...admin.user,
      findFirst: jest.fn(
        async ({ where }: { where: UserWhere }) => users.find((u) => matches(u, where)) ?? null,
      ),
      create: jest.fn(async ({ data }: { data: Partial<User> }) => {
        const created = testUser(data);
        users.push(created);
        return created;
      }),
      update: jest.fn(),
      updateMany: jest.fn(async () => ({ count: 0 })),
    },
    task: admin.task,
    trip: admin.trip,
    refreshToken: {
      ...admin.refreshToken,
      create: jest.fn(async () => ({})),
      findFirst: jest.fn(async () => null),
      update: jest.fn(),
      updateMany: jest.fn(async () => ({ count: 0 })),
    },
  };
}

export interface HttpResult {
  status: number;
  /** Response headers, lower-cased names. */
  headers: Record<string, string>;
  /** Parsed JSON body, or `undefined` when the response has none. */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  body: any;
}

export interface HttpRequestOptions {
  /** Sent as `Authorization: Bearer <token>`. */
  token?: string;
  headers?: Record<string, string>;
  body?: unknown;
}

export interface HttpTestApp {
  app: NestExpressApplication;
  http: {
    get(path: string, options?: HttpRequestOptions): Promise<HttpResult>;
    post(path: string, options?: HttpRequestOptions): Promise<HttpResult>;
    patch(path: string, options?: HttpRequestOptions): Promise<HttpResult>;
    options(path: string, options?: HttpRequestOptions): Promise<HttpResult>;
  };
  prisma: ReturnType<typeof createPrismaStub>;
  /** Signs an access token with the app's own key; `payload` is taken as-is, so a test can forge any claim. */
  signAccessToken(
    payload: { sub: string; email?: string; role?: UserRole },
    options?: { expiresIn?: string | number },
  ): Promise<string>;
  close(): Promise<void>;
}

export async function createHttpTestApp(
  users: User[] = [],
  adminSeed: AdminSeed = {},
): Promise<HttpTestApp> {
  process.env.NODE_ENV = 'test';
  process.env.DATABASE_URL = 'postgresql://test:test@localhost:5432/unused';
  process.env.JWT_ACCESS_TOKEN_PRIVATE_KEY = signingKeys.privateKey;
  process.env.JWT_ACCESS_TOKEN_PUBLIC_KEY = signingKeys.publicKey;
  process.env.JWT_ACCESS_TOKEN_TTL = '15m';
  process.env.BCRYPT_SALT_ROUNDS = '10';

  // Imported lazily so the environment above is in place before
  // `ConfigModule.forRoot()` runs.
  const { AppModule } = await import('../../src/app.module');
  const { configureApp } = await import('../../src/app.setup');
  const { PrismaService } = await import('../../src/prisma/prisma.service');

  const prisma = createPrismaStub(users, adminSeed);
  const moduleRef = await Test.createTestingModule({ imports: [AppModule] })
    .overrideProvider(PrismaService)
    .useValue(prisma)
    .compile();

  const app = moduleRef.createNestApplication<NestExpressApplication>({ logger: false });
  configureApp(app, app.get(ConfigService));
  await app.listen(0, '127.0.0.1');
  const address = app.getHttpServer().address() as AddressInfo;
  const baseUrl = `http://127.0.0.1:${address.port}`;

  const send = async (
    method: string,
    path: string,
    options: HttpRequestOptions = {},
  ): Promise<HttpResult> => {
    const response = await fetch(baseUrl + path, {
      method,
      headers: {
        ...(options.body === undefined ? {} : { 'Content-Type': 'application/json' }),
        ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
        ...options.headers,
      },
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    });
    const text = await response.text();
    return {
      status: response.status,
      headers: Object.fromEntries(response.headers.entries()),
      body: text ? JSON.parse(text) : undefined,
    };
  };

  const jwtService = app.get(JwtService, { strict: false });

  return {
    app,
    http: {
      get: (path, options) => send('GET', path, options),
      post: (path, options) => send('POST', path, options),
      patch: (path, options) => send('PATCH', path, options),
      options: (path, options) => send('OPTIONS', path, options),
    },
    prisma,
    signAccessToken: (payload, options = {}) =>
      jwtService.signAsync(
        { email: `${payload.sub}@example.com`, ...payload },
        options.expiresIn === undefined ? {} : { expiresIn: options.expiresIn },
      ),
    close: () => app.close(),
  };
}
