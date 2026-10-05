import type { TaskStats, TripStats } from '../shared/stats/types';

/**
 * An in-memory stand-in for the LifeOS backend, used ONLY by tests. It
 * implements the parts of docs/15-api-design.md the panel talks to — the
 * `{ data }` / `{ data, meta }` envelopes, the error body, rotating refresh
 * tokens with reuse detection, role checks read "from the database" on
 * every request, and the user list's filters, sort and cursor paging — so
 * the tests exercise the real client code against realistic behaviour.
 *
 * Every user carries fields the admin API must never return (password
 * hash, bio, avatar...). They are never serialized here either, and tests
 * assert they never reach the screen.
 */
export interface FakeUser {
  id: string;
  email: string;
  password: string;
  displayName: string;
  role: 'USER' | 'ADMIN';
  timezone: string;
  language: string;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
  lastActiveAt: string | null;
  tasks: TaskStats;
  trips: TripStats;
  passwordHash: string;
  bio: string;
  avatarUrl: string;
}

export interface RecordedCall {
  method: string;
  path: string;
  query: Record<string, string>;
  body: unknown;
  authorization: string | null;
}

const NO_TASKS: TaskStats = {
  total: 0,
  todo: 0,
  inProgress: 0,
  done: 0,
  overdue: 0,
  completionRate: 0,
};
const NO_TRIPS: TripStats = { total: 0, planned: 0, ongoing: 0, completed: 0, cancelled: 0 };

let sequence = 0;

export function fakeUser(overrides: Partial<FakeUser> = {}): FakeUser {
  sequence += 1;
  const suffix = String(sequence).padStart(12, '0');
  return {
    id: `00000000-0000-4000-8000-${suffix}`,
    email: `user${sequence}@example.com`,
    password: 'correct-horse-battery',
    displayName: `Kullanıcı ${sequence}`,
    role: 'USER',
    timezone: 'Europe/Istanbul',
    language: 'tr',
    createdAt: new Date(Date.UTC(2026, 8, 1, 9, 0, sequence)).toISOString(),
    updatedAt: new Date(Date.UTC(2026, 8, 2, 9, 0, sequence)).toISOString(),
    deletedAt: null,
    lastActiveAt: null,
    tasks: NO_TASKS,
    trips: NO_TRIPS,
    passwordHash: '$2b$10$SECRET-PASSWORD-HASH',
    bio: 'SECRET-BIO',
    avatarUrl: 'https://example.com/SECRET-AVATAR.png',
    ...overrides,
  };
}

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const ROLES = ['USER', 'ADMIN'];
const STATUSES = ['active', 'deleted', 'all'];
const SORTS = ['createdAt', '-createdAt', 'email', '-email'];
const LIST_PARAMS = ['limit', 'cursor', 'q', 'role', 'status', 'sort'];

function json(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

function failure(
  status: number,
  error: string,
  message: string | string[],
  path: string,
): Response {
  return json(status, {
    statusCode: status,
    error,
    message,
    path,
    timestamp: new Date().toISOString(),
  });
}

type Override = (call: RecordedCall) => Response | undefined | Promise<Response | undefined>;

export function createFakeBackend(users: FakeUser[] = []) {
  const accessTokens = new Map<string, string>();
  const refreshTokens = new Map<string, { userId: string; revoked: boolean }>();
  const calls: RecordedCall[] = [];
  const overrides = new Map<string, Override>();
  let tokenCounter = 0;

  const dashboardStats = {
    tasks: { total: 12, todo: 6, inProgress: 2, done: 4, overdue: 3, completionRate: 33 },
    trips: { total: 8, planned: 4, ongoing: 1, completed: 2, cancelled: 1 },
    newLast7Days: 5,
    newLast30Days: 9,
    activeLast7Days: 4,
  };

  function issueTokens(userId: string) {
    tokenCounter += 1;
    const accessToken = `access-${tokenCounter}`;
    const refreshToken = `refresh-${tokenCounter}`;
    accessTokens.set(accessToken, userId);
    refreshTokens.set(refreshToken, { userId, revoked: false });
    return { accessToken, refreshToken };
  }

  function authenticate(call: RecordedCall): FakeUser | Response {
    const token = call.authorization?.replace(/^Bearer /, '') ?? '';
    const user = users.find((candidate) => candidate.id === accessTokens.get(token));
    if (!user || user.deletedAt) {
      return failure(401, 'UNAUTHORIZED', 'Unauthorized', call.path);
    }
    return user;
  }

  /** The role is read fresh on every request, like the real `RolesGuard`. */
  function authorizeAdmin(call: RecordedCall): FakeUser | Response {
    const user = authenticate(call);
    if (user instanceof Response) return user;
    if (user.role !== 'ADMIN') {
      return failure(403, 'FORBIDDEN', 'Forbidden resource', call.path);
    }
    return user;
  }

  const summary = (user: FakeUser) => ({
    id: user.id,
    email: user.email,
    displayName: user.displayName,
    role: user.role,
    timezone: user.timezone,
    createdAt: user.createdAt,
    deletedAt: user.deletedAt,
    isActive: user.deletedAt === null,
  });

  function listUsers(call: RecordedCall): Response {
    const { query } = call;
    const errors: string[] = [];
    for (const key of Object.keys(query)) {
      if (!LIST_PARAMS.includes(key)) errors.push(`property ${key} should not exist`);
    }
    const limit = query.limit === undefined ? 20 : Number(query.limit);
    if (!Number.isInteger(limit) || limit < 1 || limit > 100) errors.push('limit is invalid');
    if (query.cursor !== undefined && !UUID.test(query.cursor))
      errors.push('cursor must be a UUID');
    if (query.q !== undefined && (query.q.length < 2 || query.q.length > 100)) {
      errors.push('q must be longer than or equal to 2 characters');
    }
    if (query.role !== undefined && !ROLES.includes(query.role)) errors.push('role is invalid');
    const status = query.status ?? 'active';
    if (!STATUSES.includes(status)) errors.push('status must be one of: active, deleted, all');
    const sort = query.sort ?? '-createdAt';
    if (!SORTS.includes(sort))
      errors.push('sort must be one of: createdAt, -createdAt, email, -email');
    if (errors.length > 0) return failure(400, 'BAD_REQUEST', errors, call.path);

    const descending = sort.startsWith('-');
    const field = (descending ? sort.slice(1) : sort) as 'createdAt' | 'email';
    const needle = query.q?.toLowerCase();
    const matching = users
      .filter((user) => status === 'all' || (status === 'active') === (user.deletedAt === null))
      .filter((user) => !query.role || user.role === query.role)
      .filter(
        (user) =>
          !needle ||
          user.email.toLowerCase().includes(needle) ||
          user.displayName.toLowerCase().includes(needle),
      )
      .sort((a, b) => {
        const order = a[field] < b[field] ? -1 : a[field] > b[field] ? 1 : 0;
        return (descending ? -order : order) || (a.id < b.id ? -1 : 1);
      });

    const start = query.cursor ? matching.findIndex((user) => user.id === query.cursor) + 1 : 0;
    const page = matching.slice(start, start + limit);
    const hasMore = start + limit < matching.length;
    return json(200, {
      data: page.map(summary),
      meta: { nextCursor: hasMore ? (page[page.length - 1]?.id ?? null) : null, limit, hasMore },
    });
  }

  function route(call: RecordedCall): Response {
    const key = `${call.method} ${call.path}`;
    const body = (call.body ?? {}) as Record<string, string>;

    if (key === 'POST /auth/login') {
      const user = users.find(
        (candidate) => candidate.email === body.email && candidate.deletedAt === null,
      );
      if (!user || user.password !== body.password) {
        return failure(401, 'UNAUTHORIZED', 'Invalid email or password.', call.path);
      }
      return json(200, {
        data: {
          ...issueTokens(user.id),
          user: {
            id: user.id,
            email: user.email,
            displayName: user.displayName,
            avatarUrl: null,
            role: user.role,
            createdAt: user.createdAt,
          },
        },
      });
    }

    if (key === 'POST /auth/refresh') {
      const record = refreshTokens.get(body.refreshToken ?? '');
      if (!record) return failure(401, 'UNAUTHORIZED', 'Invalid refresh token.', call.path);
      if (record.revoked) {
        // Reuse of a rotated token: the whole session family is revoked.
        for (const other of refreshTokens.values()) {
          if (other.userId === record.userId) other.revoked = true;
        }
        return failure(401, 'UNAUTHORIZED', 'Refresh token has been revoked.', call.path);
      }
      record.revoked = true;
      return json(200, { data: issueTokens(record.userId) });
    }

    if (key === 'POST /auth/logout') {
      const user = authenticate(call);
      if (user instanceof Response) return user;
      const record = refreshTokens.get(body.refreshToken ?? '');
      if (record && record.userId === user.id) record.revoked = true;
      return json(200, { data: { success: true } });
    }

    if (call.path.startsWith('/admin/')) {
      const admin = authorizeAdmin(call);
      if (admin instanceof Response) return admin;

      if (key === 'GET /admin/session') {
        return json(200, {
          data: {
            id: admin.id,
            email: admin.email,
            displayName: admin.displayName,
            role: admin.role,
          },
        });
      }
      if (key === 'GET /admin/dashboard') {
        const active = users.filter((user) => user.deletedAt === null);
        return json(200, {
          data: {
            users: {
              total: active.length,
              deleted: users.length - active.length,
              admins: active.filter((user) => user.role === 'ADMIN').length,
              newLast7Days: dashboardStats.newLast7Days,
              newLast30Days: dashboardStats.newLast30Days,
              activeLast7Days: dashboardStats.activeLast7Days,
            },
            tasks: dashboardStats.tasks,
            trips: dashboardStats.trips,
            generatedAt: '2026-10-05T12:00:00.000Z',
          },
        });
      }
      if (key === 'GET /admin/users') return listUsers(call);

      const detail = /^\/admin\/users\/([^/]+)$/.exec(call.path);
      if (call.method === 'GET' && detail) {
        const id = decodeURIComponent(detail[1] ?? '');
        if (!UUID.test(id)) {
          return failure(400, 'BAD_REQUEST', 'Validation failed (uuid is expected)', call.path);
        }
        const user = users.find((candidate) => candidate.id === id);
        if (!user) return failure(404, 'NOT_FOUND', 'User not found.', call.path);
        return json(200, {
          data: {
            ...summary(user),
            language: user.language,
            updatedAt: user.updatedAt,
            lastActiveAt: user.lastActiveAt,
            tasks: user.tasks,
            trips: user.trips,
          },
        });
      }
    }

    return failure(404, 'NOT_FOUND', `Cannot ${key}`, call.path);
  }

  const fakeFetch: typeof fetch = async (input, init) => {
    const url = new URL(String(input));
    const headers = new Headers(init?.headers);
    const call: RecordedCall = {
      method: init?.method ?? 'GET',
      path: url.pathname.replace(/^\/api\/v1/, ''),
      query: Object.fromEntries(url.searchParams.entries()),
      body: typeof init?.body === 'string' ? JSON.parse(init.body) : undefined,
      authorization: headers.get('Authorization'),
    };
    calls.push(call);
    const override = overrides.get(`${call.method} ${call.path}`);
    const overridden = override ? await override(call) : undefined;
    return overridden ?? route(call);
  };

  return {
    fetch: fakeFetch,
    users,
    calls,
    dashboardStats,
    callsTo: (method: string, path: string) =>
      calls.filter((call) => call.method === method && call.path === path),
    /** Replaces the answer of one route; return `undefined` to fall through to the real behaviour. */
    override: (method: string, path: string, handler: Override) =>
      overrides.set(`${method} ${path}`, handler),
    clearOverrides: () => overrides.clear(),
    fail: (method: string, path: string, status: number, message = 'Failure') =>
      overrides.set(`${method} ${path}`, (call) => failure(status, 'ERROR', message, call.path)),
    /** Keeps answers to one route pending until the returned function is called — to observe loading states. */
    hold: (method: string, path: string) => {
      let release: () => void = () => undefined;
      const gate = new Promise<void>((resolve) => {
        release = resolve;
      });
      overrides.set(`${method} ${path}`, async () => {
        await gate;
        return undefined;
      });
      return release;
    },
    /** Simulates every access token running past its 15-minute lifetime. */
    expireAccessTokens: () => accessTokens.clear(),
    revokeRefreshTokens: () => {
      for (const record of refreshTokens.values()) record.revoked = true;
    },
    activeRefreshTokens: () =>
      [...refreshTokens.entries()].filter(([, record]) => !record.revoked).map(([token]) => token),
    failure,
    json,
  };
}

export type FakeBackend = ReturnType<typeof createFakeBackend>;
