import { NotFoundException } from '@nestjs/common';
import { UserRole } from '@prisma/client';
import { UsersService } from '../users/users.service';
import { AdminService } from './admin.service';
import { AdminUsersQueryDto } from './dto/admin-users-query.dto';
import { AdminRepository, AdminUserDetailRow } from './repositories/admin.repository';

const USER_ID = '00000000-0000-4000-8000-000000000001';

function userRow(overrides: Partial<AdminUserDetailRow> = {}): AdminUserDetailRow {
  return {
    id: USER_ID,
    email: 'ada@example.com',
    displayName: 'Ada',
    role: UserRole.USER,
    timezone: 'Europe/Istanbul',
    language: 'tr',
    createdAt: new Date('2026-09-01T10:00:00.000Z'),
    updatedAt: new Date('2026-09-02T10:00:00.000Z'),
    deletedAt: null,
    ...overrides,
  };
}

function query(overrides: Partial<AdminUsersQueryDto> = {}): AdminUsersQueryDto {
  return Object.assign(new AdminUsersQueryDto(), overrides);
}

describe('AdminService', () => {
  let repository: jest.Mocked<AdminRepository>;
  let service: AdminService;

  beforeEach(() => {
    repository = {
      listUsers: jest.fn().mockResolvedValue({ items: [], hasMore: false }),
      findUserById: jest.fn().mockResolvedValue(userRow()),
      countUsers: jest.fn().mockResolvedValue({
        total: 0,
        deleted: 0,
        admins: 0,
        newSinceWeek: 0,
        newSinceMonth: 0,
      }),
      countUsersWithSessionSince: jest.fn().mockResolvedValue(0),
      countTasksByStatus: jest.fn().mockResolvedValue({}),
      countOverdueTasks: jest.fn().mockResolvedValue(0),
      countTripsByStatus: jest.fn().mockResolvedValue({}),
      findLastSessionAt: jest.fn().mockResolvedValue(null),
    } as unknown as jest.Mocked<AdminRepository>;
    service = new AdminService({} as UsersService, repository);
  });

  describe('getDashboard', () => {
    const NOW = new Date('2026-10-05T12:00:00.000Z');

    it('maps the aggregate counts into the dashboard', async () => {
      repository.countUsers.mockResolvedValue({
        total: 40,
        deleted: 3,
        admins: 2,
        newSinceWeek: 5,
        newSinceMonth: 12,
      });
      repository.countUsersWithSessionSince.mockResolvedValue(9);
      repository.countTasksByStatus.mockResolvedValue({ TODO: 6, IN_PROGRESS: 2, DONE: 4 });
      repository.countOverdueTasks.mockResolvedValue(3);
      repository.countTripsByStatus.mockResolvedValue({
        PLANNED: 4,
        ONGOING: 1,
        COMPLETED: 2,
        CANCELLED: 1,
      });

      expect(await service.getDashboard(NOW)).toEqual({
        users: {
          total: 40,
          deleted: 3,
          admins: 2,
          newLast7Days: 5,
          newLast30Days: 12,
          activeLast7Days: 9,
        },
        tasks: { total: 12, todo: 6, inProgress: 2, done: 4, overdue: 3, completionRate: 33 },
        trips: { total: 8, planned: 4, ongoing: 1, completed: 2, cancelled: 1 },
        generatedAt: NOW,
      });
    });

    it('reports zeros, and a 0 completion rate, for an empty system', async () => {
      const dashboard = await service.getDashboard(NOW);

      expect(dashboard.tasks).toEqual({
        total: 0,
        todo: 0,
        inProgress: 0,
        done: 0,
        overdue: 0,
        completionRate: 0,
      });
      expect(dashboard.trips).toEqual({
        total: 0,
        planned: 0,
        ongoing: 0,
        completed: 0,
        cancelled: 0,
      });
    });

    it.each([
      [{ DONE: 5 }, 100],
      [{ TODO: 5 }, 0],
      [{ TODO: 2, DONE: 1 }, 33],
      [{ TODO: 1, DONE: 2 }, 67],
      [{ TODO: 1, IN_PROGRESS: 1, DONE: 2 }, 50],
    ])('computes the completion rate of %j as %i', async (counts, expected) => {
      repository.countTasksByStatus.mockResolvedValue(counts);

      const { tasks } = await service.getDashboard(NOW);

      expect(tasks.completionRate).toBe(expected);
      expect(Number.isFinite(tasks.completionRate)).toBe(true);
    });

    it('uses 7- and 30-day windows ending now', async () => {
      await service.getDashboard(NOW);

      expect(repository.countUsers).toHaveBeenCalledWith(
        new Date('2026-09-28T12:00:00.000Z'),
        new Date('2026-09-05T12:00:00.000Z'),
      );
      expect(repository.countUsersWithSessionSince).toHaveBeenCalledWith(
        new Date('2026-09-28T12:00:00.000Z'),
      );
    });

    it.each([
      ['2026-10-05T00:00:00.000Z', '2026-10-05'],
      ['2026-10-05T23:59:59.999Z', '2026-10-05'],
      // Already 6 October in Istanbul, but the system-wide cutoff is the UTC date.
      ['2026-10-05T21:30:00.000Z', '2026-10-05'],
    ])('measures overdue against the UTC date (now=%s)', async (now, expectedDay) => {
      await service.getDashboard(new Date(now));

      expect(repository.countOverdueTasks).toHaveBeenCalledWith(
        new Date(`${expectedDay}T00:00:00.000Z`),
      );
    });

    it('asks for system-wide counts, never per user', async () => {
      await service.getDashboard(NOW);

      expect(repository.countTasksByStatus).toHaveBeenCalledWith();
      expect(repository.countTripsByStatus).toHaveBeenCalledWith();
      expect(repository.listUsers).not.toHaveBeenCalled();
      expect(repository.findUserById).not.toHaveBeenCalled();
    });
  });

  describe('listUsers', () => {
    it('applies the defaults: active accounts, newest first, 20 per page', async () => {
      await service.listUsers(query());

      expect(repository.listUsers).toHaveBeenCalledWith({
        q: undefined,
        role: undefined,
        status: 'active',
        sortField: 'createdAt',
        sortDirection: 'desc',
        cursor: undefined,
        limit: 20,
      });
    });

    it.each([
      ['createdAt', 'createdAt', 'asc'],
      ['-createdAt', 'createdAt', 'desc'],
      ['email', 'email', 'asc'],
      ['-email', 'email', 'desc'],
    ])('parses sort=%s', async (sort, sortField, sortDirection) => {
      await service.listUsers(query({ sort }));

      expect(repository.listUsers).toHaveBeenCalledWith(
        expect.objectContaining({ sortField, sortDirection }),
      );
    });

    it('passes search, filters and cursor through', async () => {
      await service.listUsers(
        query({ q: 'ada', role: UserRole.ADMIN, status: 'all', cursor: USER_ID, limit: 5 }),
      );

      expect(repository.listUsers).toHaveBeenCalledWith(
        expect.objectContaining({
          q: 'ada',
          role: UserRole.ADMIN,
          status: 'all',
          cursor: USER_ID,
          limit: 5,
        }),
      );
    });

    it('maps rows to summaries and points nextCursor at the last row when there is more', async () => {
      const deletedAt = new Date('2026-10-04T12:00:00.000Z');
      const last = userRow({ id: '00000000-0000-4000-8000-000000000002', deletedAt });
      repository.listUsers.mockResolvedValue({ items: [userRow(), last], hasMore: true });

      const page = await service.listUsers(query({ limit: 2 }));

      expect(page.meta).toEqual({ nextCursor: last.id, limit: 2, hasMore: true });
      expect(page.data).toEqual([
        {
          id: USER_ID,
          email: 'ada@example.com',
          displayName: 'Ada',
          role: 'USER',
          timezone: 'Europe/Istanbul',
          createdAt: new Date('2026-09-01T10:00:00.000Z'),
          deletedAt: null,
          isActive: true,
        },
        expect.objectContaining({ id: last.id, deletedAt, isActive: false }),
      ]);
    });

    it('returns a null cursor on the last page and on an empty result', async () => {
      repository.listUsers.mockResolvedValue({ items: [userRow()], hasMore: false });
      expect((await service.listUsers(query())).meta).toEqual({
        nextCursor: null,
        limit: 20,
        hasMore: false,
      });

      repository.listUsers.mockResolvedValue({ items: [], hasMore: false });
      expect(await service.listUsers(query())).toEqual({
        data: [],
        meta: { nextCursor: null, limit: 20, hasMore: false },
      });
    });

    it('never returns a field the row carries beyond the summary', async () => {
      const leaky = { ...userRow(), passwordHash: '$2b$10$hash', bio: 'private', avatarUrl: 'x' };
      repository.listUsers.mockResolvedValue({ items: [leaky], hasMore: false });

      const [summary] = (await service.listUsers(query())).data;

      expect(Object.keys(summary).sort()).toEqual(
        [
          'createdAt',
          'deletedAt',
          'displayName',
          'email',
          'id',
          'isActive',
          'role',
          'timezone',
        ].sort(),
      );
    });

    it('issues exactly one repository call, with no per-user lookups', async () => {
      repository.listUsers.mockResolvedValue({
        items: Array.from({ length: 20 }, () => userRow()),
        hasMore: false,
      });

      await service.listUsers(query());

      expect(repository.listUsers).toHaveBeenCalledTimes(1);
      expect(repository.countTasksByStatus).not.toHaveBeenCalled();
      expect(repository.countOverdueTasks).not.toHaveBeenCalled();
      expect(repository.countTripsByStatus).not.toHaveBeenCalled();
      expect(repository.findLastSessionAt).not.toHaveBeenCalled();
    });
  });

  describe('getUserDetail', () => {
    const NOW = new Date('2026-10-05T12:00:00.000Z');

    it('returns the account with its aggregate counts and last session time', async () => {
      const lastSession = new Date('2026-10-04T08:00:00.000Z');
      repository.countTasksByStatus.mockResolvedValue({ TODO: 2, IN_PROGRESS: 1, DONE: 1 });
      repository.countOverdueTasks.mockResolvedValue(2);
      repository.countTripsByStatus.mockResolvedValue({ PLANNED: 1, COMPLETED: 1 });
      repository.findLastSessionAt.mockResolvedValue(lastSession);

      expect(await service.getUserDetail(USER_ID, NOW)).toEqual({
        id: USER_ID,
        email: 'ada@example.com',
        displayName: 'Ada',
        role: 'USER',
        timezone: 'Europe/Istanbul',
        language: 'tr',
        createdAt: new Date('2026-09-01T10:00:00.000Z'),
        updatedAt: new Date('2026-09-02T10:00:00.000Z'),
        deletedAt: null,
        isActive: true,
        lastActiveAt: lastSession,
        tasks: { total: 4, todo: 2, inProgress: 1, done: 1, overdue: 2, completionRate: 25 },
        trips: { total: 2, planned: 1, ongoing: 0, completed: 1, cancelled: 0 },
      });
    });

    it('scopes every count to that user', async () => {
      await service.getUserDetail(USER_ID, NOW);

      expect(repository.countTasksByStatus).toHaveBeenCalledWith(USER_ID);
      expect(repository.countTripsByStatus).toHaveBeenCalledWith(USER_ID);
      expect(repository.findLastSessionAt).toHaveBeenCalledWith(USER_ID);
      expect(repository.countOverdueTasks).toHaveBeenCalledWith(expect.any(Date), USER_ID);
    });

    it.each([
      // 21:30 UTC on 5 Oct is 00:30 on 6 Oct in Istanbul, and still 5 Oct in New York.
      ['Europe/Istanbul', '2026-10-05T21:30:00.000Z', '2026-10-06'],
      ['America/New_York', '2026-10-05T21:30:00.000Z', '2026-10-05'],
      ['UTC', '2026-10-05T21:30:00.000Z', '2026-10-05'],
      // 02:00 UTC on 5 Oct is still 4 Oct in New York.
      ['America/New_York', '2026-10-05T02:00:00.000Z', '2026-10-04'],
      ['Asia/Tokyo', '2026-10-05T16:00:00.000Z', '2026-10-06'],
    ])(
      "measures overdue against today in the user's timezone (%s at %s)",
      async (timezone, now, expectedDay) => {
        repository.findUserById.mockResolvedValue(userRow({ timezone }));

        await service.getUserDetail(USER_ID, new Date(now));

        expect(repository.countOverdueTasks).toHaveBeenCalledWith(
          new Date(`${expectedDay}T00:00:00.000Z`),
          USER_ID,
        );
      },
    );

    it('returns a soft-deleted account as inactive', async () => {
      const deletedAt = new Date('2026-10-04T12:00:00.000Z');
      repository.findUserById.mockResolvedValue(userRow({ deletedAt }));

      expect(await service.getUserDetail(USER_ID, NOW)).toMatchObject({
        deletedAt,
        isActive: false,
      });
    });

    it('returns zeros and a null last session for a user with no data', async () => {
      const detail = await service.getUserDetail(USER_ID, NOW);

      expect(detail.lastActiveAt).toBeNull();
      expect(detail.tasks.completionRate).toBe(0);
      expect(detail.tasks.total).toBe(0);
      expect(detail.trips.total).toBe(0);
    });

    it('throws NotFound for an unknown user, without counting anything', async () => {
      repository.findUserById.mockResolvedValue(null);

      await expect(service.getUserDetail(USER_ID, NOW)).rejects.toThrow(NotFoundException);
      expect(repository.countTasksByStatus).not.toHaveBeenCalled();
      expect(repository.findLastSessionAt).not.toHaveBeenCalled();
    });

    it('uses a fixed number of repository calls', async () => {
      await service.getUserDetail(USER_ID, NOW);

      expect(repository.findUserById).toHaveBeenCalledTimes(1);
      expect(repository.countTasksByStatus).toHaveBeenCalledTimes(1);
      expect(repository.countOverdueTasks).toHaveBeenCalledTimes(1);
      expect(repository.countTripsByStatus).toHaveBeenCalledTimes(1);
      expect(repository.findLastSessionAt).toHaveBeenCalledTimes(1);
    });
  });
});
