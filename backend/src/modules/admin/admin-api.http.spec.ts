import { TaskStatus, TripStatus, UserRole } from '@prisma/client';
import { createHttpTestApp, HttpTestApp, testUser } from '../../../test/support/http-test-app';
import { testRefreshToken, testTask, testTrip } from '../../../test/support/in-memory-admin-prisma';

/**
 * The read-only admin API over real HTTP: the real guards, validation
 * pipe, controller, service and `AdminRepository`, against an in-memory
 * store that evaluates the repository's actual queries.
 *
 * Every piece of personal content in the seed carries a `SECRET-` marker,
 * so "nothing sensitive leaks" is checked against the raw response text.
 */
describe('Admin API (HTTP)', () => {
  const DASHBOARD = '/api/v1/admin/dashboard';
  const USERS = '/api/v1/admin/users';
  const UNKNOWN_ID = '00000000-0000-4000-8000-00000000ffff';

  const DAY_MS = 24 * 60 * 60 * 1000;
  const daysAgo = (days: number) => new Date(Date.now() - days * DAY_MS);
  const day = (iso: string) => new Date(`${iso}T00:00:00.000Z`);

  const sensitive = {
    passwordHash: '$2b$10$SECRET-PASSWORD-HASH',
    bio: 'SECRET-BIO',
    avatarUrl: 'https://example.com/SECRET-AVATAR.png',
  };

  const admin = testUser({
    id: '00000000-0000-4000-8000-000000000001',
    email: 'root@example.com',
    displayName: 'Root Admin',
    role: UserRole.ADMIN,
    createdAt: daysAgo(100),
    ...sensitive,
  });
  const ada = testUser({
    id: '00000000-0000-4000-8000-000000000002',
    email: 'ada@example.com',
    displayName: 'Ada Lovelace',
    timezone: 'Europe/Istanbul',
    createdAt: daysAgo(20),
    ...sensitive,
  });
  const bob = testUser({
    id: '00000000-0000-4000-8000-000000000003',
    email: 'bob@example.com',
    displayName: 'Bob',
    createdAt: daysAgo(2),
    ...sensitive,
  });
  const gone = testUser({
    id: '00000000-0000-4000-8000-000000000004',
    email: 'gone@example.com',
    displayName: 'Gone Ada',
    createdAt: daysAgo(1),
    deletedAt: daysAgo(0.5),
    ...sensitive,
  });
  const deletedAdmin = testUser({
    id: '00000000-0000-4000-8000-000000000005',
    email: 'former-admin@example.com',
    displayName: 'Former Admin',
    role: UserRole.ADMIN,
    createdAt: daysAgo(50),
    deletedAt: daysAgo(10),
    ...sensitive,
  });

  const taskContent = { title: 'SECRET-TASK-TITLE', description: 'SECRET-TASK-DESCRIPTION' };
  const tripContent = {
    title: 'SECRET-TRIP-TITLE',
    destination: 'SECRET-DESTINATION',
    country: 'SECRET-COUNTRY',
  };
  const PAST = day('2020-01-01');
  const FUTURE = day('2099-01-01');

  const tasks = [
    testTask({ userId: ada.id, status: TaskStatus.TODO, dueDate: PAST, ...taskContent }),
    testTask({ userId: ada.id, status: TaskStatus.IN_PROGRESS, dueDate: PAST, ...taskContent }),
    testTask({ userId: ada.id, status: TaskStatus.TODO, dueDate: FUTURE, ...taskContent }),
    testTask({ userId: ada.id, status: TaskStatus.DONE, dueDate: PAST, ...taskContent }),
    testTask({
      userId: ada.id,
      status: TaskStatus.TODO,
      dueDate: PAST,
      deletedAt: daysAgo(1),
      ...taskContent,
    }),
    testTask({ userId: bob.id, status: TaskStatus.DONE, dueDate: FUTURE, ...taskContent }),
    testTask({ userId: gone.id, status: TaskStatus.TODO, dueDate: PAST, ...taskContent }),
  ];
  const trips = [
    testTrip({ userId: ada.id, status: TripStatus.PLANNED, ...tripContent }),
    testTrip({ userId: ada.id, status: TripStatus.COMPLETED, ...tripContent }),
    testTrip({ userId: ada.id, status: TripStatus.PLANNED, deletedAt: daysAgo(1), ...tripContent }),
    testTrip({ userId: bob.id, status: TripStatus.CANCELLED, ...tripContent }),
    testTrip({ userId: gone.id, status: TripStatus.ONGOING, ...tripContent }),
  ];
  const adaLastSession = daysAgo(1);
  const refreshTokens = [
    testRefreshToken({ userId: ada.id, createdAt: daysAgo(15), tokenHash: 'SECRET-TOKEN-HASH' }),
    testRefreshToken({ userId: ada.id, createdAt: adaLastSession, tokenHash: 'SECRET-TOKEN-HASH' }),
    testRefreshToken({ userId: ada.id, createdAt: daysAgo(3), tokenHash: 'SECRET-TOKEN-HASH' }),
    testRefreshToken({ userId: bob.id, createdAt: daysAgo(12), tokenHash: 'SECRET-TOKEN-HASH' }),
    testRefreshToken({ userId: gone.id, createdAt: daysAgo(1), tokenHash: 'SECRET-TOKEN-HASH' }),
  ];

  const SUMMARY_FIELDS = [
    'createdAt',
    'deletedAt',
    'displayName',
    'email',
    'id',
    'isActive',
    'role',
    'timezone',
  ];
  const DETAIL_FIELDS = [
    ...SUMMARY_FIELDS,
    'language',
    'lastActiveAt',
    'tasks',
    'trips',
    'updatedAt',
  ].sort();
  const FORBIDDEN_IN_ANY_RESPONSE =
    /SECRET-|\$2b\$|passwordHash|password_hash|tokenHash|token_hash|refreshToken|bio|avatarUrl|notificationPreferences|taskReminders|"title"|"description"|destination|country/;

  let ctx: HttpTestApp;
  let adminToken: string;
  const asAdmin = (path: string) => ctx.http.get(path, { token: adminToken });
  const emailsOf = (body: { data: { email: string }[] }) => body.data.map((user) => user.email);

  beforeAll(async () => {
    ctx = await createHttpTestApp([admin, ada, bob, gone, deletedAdmin], {
      tasks,
      trips,
      refreshTokens,
    });
    adminToken = await ctx.signAccessToken({ sub: admin.id, role: UserRole.ADMIN });
  });

  afterAll(async () => {
    await ctx.close();
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  afterEach(() => {
    // No admin request may ever load rows of user content or of tokens.
    for (const reader of [ctx.prisma.task, ctx.prisma.trip]) {
      expect(reader.findMany).not.toHaveBeenCalled();
      expect(reader.findFirst).not.toHaveBeenCalled();
      expect(reader.findUnique).not.toHaveBeenCalled();
    }
    expect(ctx.prisma.refreshToken.findMany).not.toHaveBeenCalled();
  });

  describe.each([
    ['GET /admin/dashboard', DASHBOARD],
    ['GET /admin/users', USERS],
    ['GET /admin/users/:id', `${USERS}/${ada.id}`],
  ])('access control of %s', (_name, path) => {
    it('returns 200 to an ADMIN', async () => {
      expect((await asAdmin(path)).status).toBe(200);
    });

    it('returns 403 to a regular USER, without running any admin query', async () => {
      const token = await ctx.signAccessToken({ sub: ada.id, role: UserRole.USER });

      const res = await ctx.http.get(path, { token });

      expect(res.status).toBe(403);
      expect(res.body).toMatchObject({ statusCode: 403, error: 'FORBIDDEN' });
      expect(res.body).not.toHaveProperty('data');
      expect(ctx.prisma.user.findMany).not.toHaveBeenCalled();
      expect(ctx.prisma.user.findUnique).not.toHaveBeenCalled();
      expect(ctx.prisma.user.count).not.toHaveBeenCalled();
      expect(ctx.prisma.task.groupBy).not.toHaveBeenCalled();
    });

    it('returns 401 without a token', async () => {
      const res = await ctx.http.get(path);

      expect(res.status).toBe(401);
      expect(res.body).not.toHaveProperty('data');
    });

    it('returns 401 for an invalid token', async () => {
      expect((await ctx.http.get(path, { token: 'not-a-jwt' })).status).toBe(401);
    });

    it('returns 401 for an expired token', async () => {
      const token = await ctx.signAccessToken(
        { sub: admin.id, role: UserRole.ADMIN },
        { expiresIn: -60 },
      );

      expect((await ctx.http.get(path, { token })).status).toBe(401);
    });

    it('returns 401 to a soft-deleted account, even a former admin', async () => {
      const token = await ctx.signAccessToken({ sub: deletedAdmin.id, role: UserRole.ADMIN });

      expect((await ctx.http.get(path, { token })).status).toBe(401);
    });

    it('returns 403 when the token claims ADMIN but the database says USER', async () => {
      const token = await ctx.signAccessToken({ sub: ada.id, role: UserRole.ADMIN });

      expect((await ctx.http.get(path, { token })).status).toBe(403);
    });

    it('returns 200 when the token claims USER but the database says ADMIN', async () => {
      const token = await ctx.signAccessToken({ sub: admin.id, role: UserRole.USER });

      expect((await ctx.http.get(path, { token })).status).toBe(200);
    });

    it.each(['post', 'patch'] as const)('exposes no %s (read-only API)', async (method) => {
      const res = await ctx.http[method](path, { token: adminToken, body: {} });

      expect(res.status).toBe(404);
    });
  });

  describe('GET /admin/dashboard', () => {
    it('returns system-wide aggregates of active accounts and their active records', async () => {
      const res = await asAdmin(DASHBOARD);

      expect(res.status).toBe(200);
      expect(res.body.data).toEqual({
        users: {
          total: 3,
          deleted: 2,
          admins: 1,
          newLast7Days: 1,
          newLast30Days: 2,
          activeLast7Days: 1,
        },
        // ada: TODO x2, IN_PROGRESS, DONE; bob: DONE. Deleted tasks and deleted accounts are excluded.
        tasks: { total: 5, todo: 2, inProgress: 1, done: 2, overdue: 2, completionRate: 40 },
        trips: { total: 3, planned: 1, ongoing: 0, completed: 1, cancelled: 1 },
        generatedAt: expect.any(String),
      });
      expect(Date.now() - new Date(res.body.data.generatedAt).getTime()).toBeLessThan(60_000);
    });

    it('contains no personal content', async () => {
      const res = await asAdmin(DASHBOARD);

      expect(JSON.stringify(res.body)).not.toMatch(FORBIDDEN_IN_ANY_RESPONSE);
      expect(JSON.stringify(res.body)).not.toContain('@example.com');
    });

    it('runs a fixed set of aggregate queries and never iterates over users', async () => {
      await asAdmin(DASHBOARD);

      expect(ctx.prisma.user.count).toHaveBeenCalledTimes(5);
      expect(ctx.prisma.refreshToken.groupBy).toHaveBeenCalledTimes(1);
      expect(ctx.prisma.task.groupBy).toHaveBeenCalledTimes(1);
      expect(ctx.prisma.task.count).toHaveBeenCalledTimes(1);
      expect(ctx.prisma.trip.groupBy).toHaveBeenCalledTimes(1);
      expect(ctx.prisma.user.findMany).not.toHaveBeenCalled();
      expect(ctx.prisma.user.findUnique).not.toHaveBeenCalled();
      expect(ctx.prisma.refreshToken.aggregate).not.toHaveBeenCalled();
    });
  });

  describe('GET /admin/users', () => {
    it('lists active accounts by default, newest first, in the paginated envelope', async () => {
      const res = await asAdmin(USERS);

      expect(res.status).toBe(200);
      expect(emailsOf(res.body)).toEqual([
        'bob@example.com',
        'ada@example.com',
        'root@example.com',
      ]);
      expect(res.body.meta).toEqual({ nextCursor: null, limit: 20, hasMore: false });
      expect(res.body.data[1]).toEqual({
        id: ada.id,
        email: 'ada@example.com',
        displayName: 'Ada Lovelace',
        role: 'USER',
        timezone: 'Europe/Istanbul',
        createdAt: ada.createdAt.toISOString(),
        deletedAt: null,
        isActive: true,
      });
    });

    it('returns exactly the summary fields for every user, and nothing sensitive', async () => {
      const res = await asAdmin(`${USERS}?status=all`);

      expect(res.body.data).toHaveLength(5);
      for (const user of res.body.data) {
        expect(Object.keys(user).sort()).toEqual(SUMMARY_FIELDS);
      }
      expect(JSON.stringify(res.body)).not.toMatch(FORBIDDEN_IN_ANY_RESPONSE);
    });

    it('never selects the password hash or other private columns', async () => {
      await asAdmin(USERS);

      const { select } = ctx.prisma.user.findMany.mock.calls[0][0];
      expect(Object.keys(select ?? {}).sort()).toEqual(
        ['createdAt', 'deletedAt', 'displayName', 'email', 'id', 'role', 'timezone'].sort(),
      );
    });

    it('lists only soft-deleted accounts for status=deleted', async () => {
      const res = await asAdmin(`${USERS}?status=deleted`);

      expect(emailsOf(res.body)).toEqual(['gone@example.com', 'former-admin@example.com']);
      expect(res.body.data.every((user: { isActive: boolean }) => !user.isActive)).toBe(true);
      expect(res.body.data[0].deletedAt).toBe(gone.deletedAt?.toISOString());
    });

    it('lists every account for status=all', async () => {
      const res = await asAdmin(`${USERS}?status=all`);

      expect(emailsOf(res.body)).toEqual([
        'gone@example.com',
        'bob@example.com',
        'ada@example.com',
        'former-admin@example.com',
        'root@example.com',
      ]);
    });

    it('searches email and display name case-insensitively with q', async () => {
      expect(emailsOf((await asAdmin(`${USERS}?q=LOVELACE`)).body)).toEqual(['ada@example.com']);
      expect(emailsOf((await asAdmin(`${USERS}?q=bob%40`)).body)).toEqual(['bob@example.com']);
      expect(emailsOf((await asAdmin(`${USERS}?q=nobody`)).body)).toEqual([]);
      // "ada" also matches the deleted "Gone Ada", which stays hidden by default.
      expect(emailsOf((await asAdmin(`${USERS}?q=ada`)).body)).toEqual(['ada@example.com']);
      expect(emailsOf((await asAdmin(`${USERS}?q=ada&status=all`)).body)).toEqual([
        'gone@example.com',
        'ada@example.com',
      ]);
    });

    it('filters by role', async () => {
      expect(emailsOf((await asAdmin(`${USERS}?role=ADMIN`)).body)).toEqual(['root@example.com']);
      expect(emailsOf((await asAdmin(`${USERS}?role=USER`)).body)).toEqual([
        'bob@example.com',
        'ada@example.com',
      ]);
      expect(emailsOf((await asAdmin(`${USERS}?role=ADMIN&status=all`)).body)).toEqual([
        'former-admin@example.com',
        'root@example.com',
      ]);
    });

    it.each([
      ['createdAt', ['root@example.com', 'ada@example.com', 'bob@example.com']],
      ['-createdAt', ['bob@example.com', 'ada@example.com', 'root@example.com']],
      ['email', ['ada@example.com', 'bob@example.com', 'root@example.com']],
      ['-email', ['root@example.com', 'bob@example.com', 'ada@example.com']],
    ])('sorts by %s', async (sort, expected) => {
      expect(emailsOf((await asAdmin(`${USERS}?sort=${sort}`)).body)).toEqual(expected);
    });

    it('pages through the list: first, second and final page', async () => {
      const first = await asAdmin(`${USERS}?limit=1`);
      expect(emailsOf(first.body)).toEqual(['bob@example.com']);
      expect(first.body.meta).toEqual({ nextCursor: bob.id, limit: 1, hasMore: true });

      const second = await asAdmin(`${USERS}?limit=1&cursor=${first.body.meta.nextCursor}`);
      expect(emailsOf(second.body)).toEqual(['ada@example.com']);
      expect(second.body.meta).toEqual({ nextCursor: ada.id, limit: 1, hasMore: true });

      const final = await asAdmin(`${USERS}?limit=1&cursor=${second.body.meta.nextCursor}`);
      expect(emailsOf(final.body)).toEqual(['root@example.com']);
      expect(final.body.meta).toEqual({ nextCursor: null, limit: 1, hasMore: false });
    });

    it('accepts the boundary limits 1 and 100', async () => {
      expect((await asAdmin(`${USERS}?limit=1`)).status).toBe(200);
      expect((await asAdmin(`${USERS}?limit=100`)).body.meta.limit).toBe(100);
    });

    it.each([
      ['limit=0', 'limit'],
      ['limit=101', 'limit'],
      ['limit=abc', 'limit'],
      ['cursor=not-a-uuid', 'cursor must be a UUID'],
      ['cursor=123', 'cursor must be a UUID'],
      ['sort=passwordHash', 'sort must be one of'],
      ['sort=-displayName', 'sort must be one of'],
      ['role=SUPERADMIN', 'role'],
      ['role=admin', 'role'],
      ['status=banned', 'status must be one of'],
      ['q=a', 'q must be'],
      [`q=${'a'.repeat(101)}`, 'q must be'],
      ['includeDeleted=true', 'property includeDeleted should not exist'],
      ['select=passwordHash', 'property select should not exist'],
    ])('rejects ?%s with 400, without querying', async (queryString, expectedMessage) => {
      const res = await asAdmin(`${USERS}?${queryString}`);

      expect(res.status).toBe(400);
      expect(res.body).toMatchObject({ statusCode: 400 });
      expect(JSON.stringify(res.body.message)).toContain(expectedMessage);
      expect(ctx.prisma.user.findMany).not.toHaveBeenCalled();
    });

    it('uses one query for the whole page, with no per-user queries (no N+1)', async () => {
      const res = await asAdmin(`${USERS}?status=all`);

      expect(res.body.data).toHaveLength(5);
      expect(ctx.prisma.user.findMany).toHaveBeenCalledTimes(1);
      expect(ctx.prisma.user.findUnique).not.toHaveBeenCalled();
      expect(ctx.prisma.user.count).not.toHaveBeenCalled();
      expect(ctx.prisma.task.groupBy).not.toHaveBeenCalled();
      expect(ctx.prisma.task.count).not.toHaveBeenCalled();
      expect(ctx.prisma.trip.groupBy).not.toHaveBeenCalled();
      expect(ctx.prisma.refreshToken.groupBy).not.toHaveBeenCalled();
      expect(ctx.prisma.refreshToken.aggregate).not.toHaveBeenCalled();
    });
  });

  describe('GET /admin/users/:id', () => {
    it("returns the account with aggregate counts of the user's own records", async () => {
      const res = await asAdmin(`${USERS}/${ada.id}`);

      expect(res.status).toBe(200);
      expect(res.body).toEqual({
        data: {
          id: ada.id,
          email: 'ada@example.com',
          displayName: 'Ada Lovelace',
          role: 'USER',
          timezone: 'Europe/Istanbul',
          language: 'tr',
          createdAt: ada.createdAt.toISOString(),
          updatedAt: ada.updatedAt.toISOString(),
          deletedAt: null,
          isActive: true,
          lastActiveAt: adaLastSession.toISOString(),
          tasks: { total: 4, todo: 2, inProgress: 1, done: 1, overdue: 2, completionRate: 25 },
          trips: { total: 2, planned: 1, ongoing: 0, completed: 1, cancelled: 0 },
        },
      });
    });

    it('returns exactly the detail fields and nothing sensitive', async () => {
      const res = await asAdmin(`${USERS}/${ada.id}`);

      expect(Object.keys(res.body.data).sort()).toEqual(DETAIL_FIELDS);
      expect(JSON.stringify(res.body)).not.toMatch(FORBIDDEN_IN_ANY_RESPONSE);
    });

    it('never selects the password hash or other private columns', async () => {
      await asAdmin(`${USERS}/${ada.id}`);

      const { select } = ctx.prisma.user.findUnique.mock.calls[0][0];
      expect(Object.keys(select ?? {}).sort()).toEqual(
        [
          'createdAt',
          'deletedAt',
          'displayName',
          'email',
          'id',
          'language',
          'role',
          'timezone',
          'updatedAt',
        ].sort(),
      );
    });

    it('returns zeros and a null lastActiveAt for a user with no data', async () => {
      const res = await asAdmin(`${USERS}/${admin.id}`);

      expect(res.body.data).toMatchObject({
        lastActiveAt: null,
        tasks: { total: 0, todo: 0, inProgress: 0, done: 0, overdue: 0, completionRate: 0 },
        trips: { total: 0, planned: 0, ongoing: 0, completed: 0, cancelled: 0 },
      });
    });

    it('returns a soft-deleted user too, marked inactive, with their own counts', async () => {
      const res = await asAdmin(`${USERS}/${gone.id}`);

      expect(res.status).toBe(200);
      expect(res.body.data).toMatchObject({
        email: 'gone@example.com',
        isActive: false,
        deletedAt: gone.deletedAt?.toISOString(),
        tasks: { total: 1, todo: 1, overdue: 1, completionRate: 0 },
        trips: { total: 1, ongoing: 1 },
      });
    });

    it('returns 404 for a user that does not exist', async () => {
      const res = await asAdmin(`${USERS}/${UNKNOWN_ID}`);

      expect(res.status).toBe(404);
      expect(res.body).toMatchObject({ statusCode: 404, message: 'User not found.' });
      expect(ctx.prisma.task.groupBy).not.toHaveBeenCalled();
    });

    it.each(['not-a-uuid', '123', "1'%20OR%20'1'='1"])(
      'returns 400 for the non-UUID id %s, without querying',
      async (id) => {
        const res = await asAdmin(`${USERS}/${id}`);

        expect(res.status).toBe(400);
        expect(ctx.prisma.user.findUnique).not.toHaveBeenCalled();
      },
    );

    it('uses a fixed number of queries', async () => {
      await asAdmin(`${USERS}/${ada.id}`);

      expect(ctx.prisma.user.findUnique).toHaveBeenCalledTimes(1);
      expect(ctx.prisma.task.groupBy).toHaveBeenCalledTimes(1);
      expect(ctx.prisma.task.count).toHaveBeenCalledTimes(1);
      expect(ctx.prisma.trip.groupBy).toHaveBeenCalledTimes(1);
      expect(ctx.prisma.refreshToken.aggregate).toHaveBeenCalledTimes(1);
      expect(ctx.prisma.user.findMany).not.toHaveBeenCalled();
    });
  });

  it('keeps GET /admin/session working alongside the new routes', async () => {
    const res = await asAdmin('/api/v1/admin/session');

    expect(res.status).toBe(200);
    expect(res.body).toEqual({
      data: { id: admin.id, email: 'root@example.com', displayName: 'Root Admin', role: 'ADMIN' },
    });
  });
});
