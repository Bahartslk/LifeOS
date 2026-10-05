import { TaskStatus, TripStatus, UserRole } from '@prisma/client';
import { testUser } from '../../../../test/support/http-test-app';
import {
  AdminPrismaDelegates,
  createAdminPrismaDelegates,
  testRefreshToken,
  testTask,
  testTrip,
} from '../../../../test/support/in-memory-admin-prisma';
import { PrismaService } from '../../../prisma/prisma.service';
import {
  ADMIN_USER_DETAIL_SELECT,
  ADMIN_USER_SUMMARY_SELECT,
  AdminRepository,
  ListUsersQuery,
} from './admin.repository';

const day = (iso: string) => new Date(`${iso}T00:00:00.000Z`);
const at = (iso: string) => new Date(iso);

const SENSITIVE_USER_COLUMNS = ['passwordHash', 'bio', 'avatarUrl', 'notificationPreferences'];

describe('AdminRepository', () => {
  // Created in this order; ids are fixed so the id tie-breaker is predictable.
  const ada = testUser({
    id: '00000000-0000-4000-8000-000000000001',
    email: 'ada@example.com',
    displayName: 'Ada Lovelace',
    createdAt: at('2026-09-01T10:00:00.000Z'),
  });
  const bob = testUser({
    id: '00000000-0000-4000-8000-000000000002',
    email: 'bob@example.com',
    displayName: 'Bob',
    createdAt: at('2026-09-20T10:00:00.000Z'),
  });
  const root = testUser({
    id: '00000000-0000-4000-8000-000000000003',
    email: 'root@example.com',
    displayName: 'Root Admin',
    role: UserRole.ADMIN,
    createdAt: at('2026-10-03T10:00:00.000Z'),
  });
  const gone = testUser({
    id: '00000000-0000-4000-8000-000000000004',
    email: 'gone@example.com',
    displayName: 'Gone Ada',
    createdAt: at('2026-10-04T10:00:00.000Z'),
    deletedAt: at('2026-10-04T12:00:00.000Z'),
  });
  const users = [ada, bob, root, gone];

  const tasks = [
    testTask({ userId: ada.id, status: TaskStatus.TODO, dueDate: day('2026-10-01') }),
    testTask({ userId: ada.id, status: TaskStatus.IN_PROGRESS, dueDate: day('2026-10-04') }),
    testTask({ userId: ada.id, status: TaskStatus.TODO, dueDate: day('2026-10-05') }),
    testTask({ userId: ada.id, status: TaskStatus.DONE, dueDate: day('2026-10-01') }),
    testTask({
      userId: ada.id,
      status: TaskStatus.TODO,
      dueDate: day('2026-10-01'),
      deletedAt: at('2026-10-02T00:00:00.000Z'),
    }),
    testTask({ userId: bob.id, status: TaskStatus.DONE, dueDate: day('2026-10-10') }),
    testTask({ userId: gone.id, status: TaskStatus.TODO, dueDate: day('2026-09-01') }),
  ];
  const trips = [
    testTrip({ userId: ada.id, status: TripStatus.PLANNED }),
    testTrip({ userId: ada.id, status: TripStatus.COMPLETED }),
    testTrip({ userId: ada.id, status: TripStatus.PLANNED, deletedAt: at('2026-10-02T00:00:00Z') }),
    testTrip({ userId: bob.id, status: TripStatus.CANCELLED }),
    testTrip({ userId: gone.id, status: TripStatus.ONGOING }),
  ];
  const refreshTokens = [
    testRefreshToken({ userId: ada.id, createdAt: at('2026-09-02T08:00:00.000Z') }),
    testRefreshToken({ userId: ada.id, createdAt: at('2026-10-04T08:00:00.000Z') }),
    testRefreshToken({ userId: ada.id, createdAt: at('2026-10-03T08:00:00.000Z') }),
    testRefreshToken({ userId: bob.id, createdAt: at('2026-09-21T08:00:00.000Z') }),
    testRefreshToken({ userId: gone.id, createdAt: at('2026-10-04T09:00:00.000Z') }),
  ];

  let prisma: AdminPrismaDelegates;
  let repository: AdminRepository;

  const list = (overrides: Partial<ListUsersQuery> = {}) =>
    repository.listUsers({
      status: 'active',
      sortField: 'createdAt',
      sortDirection: 'desc',
      limit: 20,
      ...overrides,
    });
  const emails = async (overrides: Partial<ListUsersQuery> = {}) =>
    (await list(overrides)).items.map((user) => user.email);

  beforeEach(() => {
    prisma = createAdminPrismaDelegates(users, { tasks, trips, refreshTokens });
    repository = new AdminRepository(prisma as unknown as PrismaService);
  });

  afterEach(() => {
    // Whatever a method does, it must never load rows of user content.
    for (const reader of [prisma.task, prisma.trip]) {
      expect(reader.findMany).not.toHaveBeenCalled();
      expect(reader.findFirst).not.toHaveBeenCalled();
      expect(reader.findUnique).not.toHaveBeenCalled();
    }
    expect(prisma.refreshToken.findMany).not.toHaveBeenCalled();
  });

  describe('column selection', () => {
    it.each([
      ['summary', ADMIN_USER_SUMMARY_SELECT],
      ['detail', ADMIN_USER_DETAIL_SELECT],
    ])('never selects a sensitive column in the %s select', (_name, select) => {
      for (const column of SENSITIVE_USER_COLUMNS) {
        expect(select).not.toHaveProperty(column);
      }
    });

    it('lists users through the summary select only', async () => {
      const { items } = await list();

      expect(prisma.user.findMany.mock.calls[0][0].select).toBe(ADMIN_USER_SUMMARY_SELECT);
      expect(Object.keys(items[0]).sort()).toEqual(
        ['createdAt', 'deletedAt', 'displayName', 'email', 'id', 'role', 'timezone'].sort(),
      );
    });

    it('loads one user through the detail select only', async () => {
      const user = await repository.findUserById(ada.id);

      expect(prisma.user.findUnique.mock.calls[0][0].select).toBe(ADMIN_USER_DETAIL_SELECT);
      expect(user).not.toHaveProperty('passwordHash');
      expect(user).toMatchObject({ id: ada.id, language: 'tr' });
    });
  });

  describe('listUsers', () => {
    it('returns only active accounts for status=active', async () => {
      expect(await emails()).toEqual(['root@example.com', 'bob@example.com', 'ada@example.com']);
    });

    it('returns only soft-deleted accounts for status=deleted', async () => {
      expect(await emails({ status: 'deleted' })).toEqual(['gone@example.com']);
    });

    it('returns every account for status=all', async () => {
      expect(await emails({ status: 'all' })).toEqual([
        'gone@example.com',
        'root@example.com',
        'bob@example.com',
        'ada@example.com',
      ]);
    });

    it('filters by role', async () => {
      expect(await emails({ role: UserRole.ADMIN })).toEqual(['root@example.com']);
      expect(await emails({ role: UserRole.USER })).toEqual(['bob@example.com', 'ada@example.com']);
    });

    it('searches email and display name, case-insensitively', async () => {
      expect(await emails({ q: 'BOB@' })).toEqual(['bob@example.com']);
      expect(await emails({ q: 'lovelace' })).toEqual(['ada@example.com']);
      expect(await emails({ q: 'nobody' })).toEqual([]);
    });

    it('combines search with the status filter', async () => {
      expect(await emails({ q: 'ada' })).toEqual(['ada@example.com']);
      expect(await emails({ q: 'ada', status: 'all' })).toEqual([
        'gone@example.com',
        'ada@example.com',
      ]);
    });

    it.each([
      ['createdAt', 'asc', ['ada@example.com', 'bob@example.com', 'root@example.com']],
      ['createdAt', 'desc', ['root@example.com', 'bob@example.com', 'ada@example.com']],
      ['email', 'asc', ['ada@example.com', 'bob@example.com', 'root@example.com']],
      ['email', 'desc', ['root@example.com', 'bob@example.com', 'ada@example.com']],
    ] as const)('sorts by %s %s', async (sortField, sortDirection, expected) => {
      expect(await emails({ sortField, sortDirection })).toEqual(expected);
    });

    it('orders by the sort field with id as tie-breaker', async () => {
      await list({ sortField: 'email', sortDirection: 'asc' });

      expect(prisma.user.findMany.mock.calls[0][0].orderBy).toEqual([
        { email: 'asc' },
        { id: 'asc' },
      ]);
    });

    it('pages through the list with a cursor, fetching one extra row to detect more', async () => {
      const first = await list({ limit: 2 });
      expect(first.items.map((u) => u.email)).toEqual(['root@example.com', 'bob@example.com']);
      expect(first.hasMore).toBe(true);
      expect(prisma.user.findMany.mock.calls[0][0]).toMatchObject({ take: 3 });
      expect(prisma.user.findMany.mock.calls[0][0]).not.toHaveProperty('cursor');

      const second = await list({ limit: 2, cursor: first.items[1].id });
      expect(second.items.map((u) => u.email)).toEqual(['ada@example.com']);
      expect(second.hasMore).toBe(false);
      expect(prisma.user.findMany.mock.calls[1][0]).toMatchObject({
        cursor: { id: bob.id },
        skip: 1,
        take: 3,
      });
    });

    it('reports no further page when the page is exactly full', async () => {
      const page = await list({ limit: 3 });

      expect(page.items).toHaveLength(3);
      expect(page.hasMore).toBe(false);
    });

    it('uses a single query, whatever the number of users', async () => {
      await list();

      expect(prisma.user.findMany).toHaveBeenCalledTimes(1);
      expect(prisma.user.count).not.toHaveBeenCalled();
      expect(prisma.task.groupBy).not.toHaveBeenCalled();
      expect(prisma.task.count).not.toHaveBeenCalled();
      expect(prisma.trip.groupBy).not.toHaveBeenCalled();
      expect(prisma.refreshToken.aggregate).not.toHaveBeenCalled();
    });
  });

  describe('findUserById', () => {
    it('finds a soft-deleted account too', async () => {
      expect(await repository.findUserById(gone.id)).toMatchObject({
        email: 'gone@example.com',
        deletedAt: gone.deletedAt,
      });
    });

    it('returns null for an unknown id', async () => {
      expect(await repository.findUserById('00000000-0000-4000-8000-00000000ffff')).toBeNull();
    });
  });

  describe('countUsers', () => {
    it('counts active, deleted, admin and recently created accounts', async () => {
      const counts = await repository.countUsers(
        at('2026-09-28T12:00:00.000Z'),
        at('2026-09-05T12:00:00.000Z'),
      );

      // `gone` was created inside both windows but is deleted, so it only counts as deleted.
      expect(counts).toEqual({
        total: 3,
        deleted: 1,
        admins: 1,
        newSinceWeek: 1,
        newSinceMonth: 2,
      });
    });
  });

  describe('countUsersWithSessionSince', () => {
    it('counts each active account once, however many sessions it started', async () => {
      // ada has two tokens in the window, bob none, gone one but is deleted.
      expect(await repository.countUsersWithSessionSince(at('2026-09-28T12:00:00.000Z'))).toBe(1);
      expect(await repository.countUsersWithSessionSince(at('2026-09-01T00:00:00.000Z'))).toBe(2);
    });

    it('groups by user only, never reading a token hash', async () => {
      await repository.countUsersWithSessionSince(at('2026-09-28T12:00:00.000Z'));

      expect(prisma.refreshToken.groupBy.mock.calls[0][0]).toMatchObject({ by: ['userId'] });
    });
  });

  describe('task counts', () => {
    it('groups active tasks of active accounts by status, system-wide', async () => {
      // Excludes ada's deleted task and every task of the deleted account.
      expect(await repository.countTasksByStatus()).toEqual({ TODO: 2, IN_PROGRESS: 1, DONE: 2 });
    });

    it("groups one user's active tasks by status", async () => {
      expect(await repository.countTasksByStatus(ada.id)).toEqual({
        TODO: 2,
        IN_PROGRESS: 1,
        DONE: 1,
      });
      expect(await repository.countTasksByStatus(bob.id)).toEqual({ DONE: 1 });
      expect(await repository.countTasksByStatus(root.id)).toEqual({});
    });

    it("still counts a deleted account's own tasks when asked for that account", async () => {
      expect(await repository.countTasksByStatus(gone.id)).toEqual({ TODO: 1 });
    });

    it('counts unfinished tasks due strictly before the given day as overdue', async () => {
      // ada: TODO due 10-01 and IN_PROGRESS due 10-04. Not: due today, DONE, deleted, deleted account.
      expect(await repository.countOverdueTasks(day('2026-10-05'))).toBe(2);
      expect(await repository.countOverdueTasks(day('2026-10-04'))).toBe(1);
      expect(await repository.countOverdueTasks(day('2026-10-01'))).toBe(0);
      expect(await repository.countOverdueTasks(day('2026-10-06'))).toBe(3);
    });

    it("counts one user's overdue tasks", async () => {
      expect(await repository.countOverdueTasks(day('2026-10-05'), ada.id)).toBe(2);
      expect(await repository.countOverdueTasks(day('2026-10-05'), bob.id)).toBe(0);
      expect(await repository.countOverdueTasks(day('2026-10-05'), gone.id)).toBe(1);
    });
  });

  describe('trip counts', () => {
    it('groups active trips of active accounts by status, system-wide', async () => {
      expect(await repository.countTripsByStatus()).toEqual({
        PLANNED: 1,
        COMPLETED: 1,
        CANCELLED: 1,
      });
    });

    it("groups one user's active trips by status", async () => {
      expect(await repository.countTripsByStatus(ada.id)).toEqual({ PLANNED: 1, COMPLETED: 1 });
      expect(await repository.countTripsByStatus(gone.id)).toEqual({ ONGOING: 1 });
    });
  });

  describe('findLastSessionAt', () => {
    it("returns the creation time of the user's newest refresh token", async () => {
      expect(await repository.findLastSessionAt(ada.id)).toEqual(at('2026-10-04T08:00:00.000Z'));
    });

    it('returns null for a user who never signed in', async () => {
      expect(await repository.findLastSessionAt(root.id)).toBeNull();
    });
  });
});
