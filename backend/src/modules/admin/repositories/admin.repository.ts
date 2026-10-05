import { Injectable } from '@nestjs/common';
import { Prisma, TaskStatus, TripStatus, UserRole } from '@prisma/client';
import { PrismaService } from '../../../prisma/prisma.service';
import { AdminUserSortField, AdminUserStatusFilter } from '../dto/admin-users-query.dto';

/**
 * The only columns of `users` the admin API ever reads. `passwordHash`,
 * `bio`, `avatarUrl` and `notificationPreferences` are not selected, so
 * they never leave the database for an admin request.
 */
export const ADMIN_USER_SUMMARY_SELECT = {
  id: true,
  email: true,
  displayName: true,
  role: true,
  timezone: true,
  createdAt: true,
  deletedAt: true,
} satisfies Prisma.UserSelect;

export const ADMIN_USER_DETAIL_SELECT = {
  ...ADMIN_USER_SUMMARY_SELECT,
  language: true,
  updatedAt: true,
} satisfies Prisma.UserSelect;

export type AdminUserSummaryRow = Prisma.UserGetPayload<{
  select: typeof ADMIN_USER_SUMMARY_SELECT;
}>;
export type AdminUserDetailRow = Prisma.UserGetPayload<{ select: typeof ADMIN_USER_DETAIL_SELECT }>;

export interface ListUsersQuery {
  q?: string;
  role?: UserRole;
  status: AdminUserStatusFilter;
  sortField: AdminUserSortField;
  sortDirection: 'asc' | 'desc';
  cursor?: string;
  limit: number;
}

export interface ListUsersResult {
  items: AdminUserSummaryRow[];
  hasMore: boolean;
}

export interface UserCounts {
  total: number;
  deleted: number;
  admins: number;
  newSinceWeek: number;
  newSinceMonth: number;
}

export type TaskStatusCounts = Partial<Record<TaskStatus, number>>;
export type TripStatusCounts = Partial<Record<TripStatus, number>>;

const UNFINISHED_TASK_STATUSES = [TaskStatus.TODO, TaskStatus.IN_PROGRESS];

/**
 * Cross-user, READ-ONLY queries for the admin API — the one place that
 * reads across accounts, so "what can an admin see" is auditable in a
 * single file. Every other repository in this codebase is scoped to one
 * user.
 *
 * Two rules hold for every method here:
 * - `users` is read only through `ADMIN_USER_*_SELECT`.
 * - `tasks`, `trips` and `refresh_tokens` are only ever counted or
 *   aggregated (`count`/`groupBy`/`aggregate`). No row of user content is
 *   loaded, so titles, descriptions, destinations and token hashes cannot
 *   reach an admin response.
 *
 * Each method is a fixed number of queries regardless of how many users or
 * records exist (no per-row follow-up queries).
 */
@Injectable()
export class AdminRepository {
  constructor(private readonly prisma: PrismaService) {}

  /** One query: a page of users plus one extra row to detect `hasMore`. `id` is the tie-breaker that keeps paging stable. */
  async listUsers(query: ListUsersQuery): Promise<ListUsersResult> {
    const where: Prisma.UserWhereInput = {
      ...(query.status === 'active' && { deletedAt: null }),
      ...(query.status === 'deleted' && { deletedAt: { not: null } }),
      ...(query.role && { role: query.role }),
      ...(query.q && {
        OR: [
          { email: { contains: query.q, mode: 'insensitive' } },
          { displayName: { contains: query.q, mode: 'insensitive' } },
        ],
      }),
    };

    const rows = await this.prisma.user.findMany({
      where,
      select: ADMIN_USER_SUMMARY_SELECT,
      orderBy: [{ [query.sortField]: query.sortDirection }, { id: 'asc' }],
      ...(query.cursor && { cursor: { id: query.cursor }, skip: 1 }),
      take: query.limit + 1,
    });

    const hasMore = rows.length > query.limit;
    return { items: hasMore ? rows.slice(0, query.limit) : rows, hasMore };
  }

  /** Includes soft-deleted accounts: an admin must be able to inspect one. */
  findUserById(id: string): Promise<AdminUserDetailRow | null> {
    return this.prisma.user.findUnique({ where: { id }, select: ADMIN_USER_DETAIL_SELECT });
  }

  async countUsers(sinceWeek: Date, sinceMonth: Date): Promise<UserCounts> {
    const [total, deleted, admins, newSinceWeek, newSinceMonth] = await Promise.all([
      this.prisma.user.count({ where: { deletedAt: null } }),
      this.prisma.user.count({ where: { deletedAt: { not: null } } }),
      this.prisma.user.count({ where: { deletedAt: null, role: UserRole.ADMIN } }),
      this.prisma.user.count({ where: { deletedAt: null, createdAt: { gte: sinceWeek } } }),
      this.prisma.user.count({ where: { deletedAt: null, createdAt: { gte: sinceMonth } } }),
    ]);
    return { total, deleted, admins, newSinceWeek, newSinceMonth };
  }

  /**
   * Active accounts with at least one refresh token created since `since`
   * — i.e. that signed in or renewed a session. A proxy for activity, not
   * a measure of in-app usage.
   */
  async countUsersWithSessionSince(since: Date): Promise<number> {
    const groups = await this.prisma.refreshToken.groupBy({
      by: ['userId'],
      where: { createdAt: { gte: since }, user: { deletedAt: null } },
    });
    return groups.length;
  }

  /** Active tasks grouped by status — of one user when `userId` is given, otherwise of every active account. */
  async countTasksByStatus(userId?: string): Promise<TaskStatusCounts> {
    const groups = await this.prisma.task.groupBy({
      by: ['status'],
      where: this.activeRecordsOf(userId),
      _count: { _all: true },
    });
    return Object.fromEntries(groups.map((group) => [group.status, group._count._all]));
  }

  /** Unfinished active tasks due strictly before `today` (a `@db.Date` value, see `toDbDate`). */
  countOverdueTasks(today: Date, userId?: string): Promise<number> {
    return this.prisma.task.count({
      where: {
        ...this.activeRecordsOf(userId),
        dueDate: { lt: today },
        status: { in: UNFINISHED_TASK_STATUSES },
      },
    });
  }

  async countTripsByStatus(userId?: string): Promise<TripStatusCounts> {
    const groups = await this.prisma.trip.groupBy({
      by: ['status'],
      where: this.activeRecordsOf(userId),
      _count: { _all: true },
    });
    return Object.fromEntries(groups.map((group) => [group.status, group._count._all]));
  }

  /** Creation time of the user's newest refresh token, or `null` if they have none. */
  async findLastSessionAt(userId: string): Promise<Date | null> {
    const result = await this.prisma.refreshToken.aggregate({
      where: { userId },
      _max: { createdAt: true },
    });
    return result._max.createdAt;
  }

  /**
   * Not-deleted records: one user's (whatever the account's own state, so
   * a deleted account's figures can still be inspected), or — system-wide
   * — those belonging to active accounts only.
   */
  private activeRecordsOf(
    userId?: string,
  ): { deletedAt: null } & ({ userId: string } | { user: { deletedAt: null } }) {
    return userId ? { deletedAt: null, userId } : { deletedAt: null, user: { deletedAt: null } };
  }
}
