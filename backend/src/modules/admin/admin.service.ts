import { Injectable, NotFoundException, UnauthorizedException } from '@nestjs/common';
import { TaskStatus, TripStatus } from '@prisma/client';
import { toDbDate, todayInTimeZone } from '../../common/utils/date-time.util';
import { UsersService } from '../users/users.service';
import { AdminDashboardResponseDto } from './dto/admin-dashboard-response.dto';
import { AdminSessionResponseDto } from './dto/admin-session-response.dto';
import { AdminTaskStatsDto, AdminTripStatsDto } from './dto/admin-stats.dto';
import {
  AdminUserDetailResponseDto,
  AdminUserSummaryDto,
  PaginatedAdminUsersResponseDto,
} from './dto/admin-user.dto';
import { AdminUserSortField, AdminUsersQueryDto } from './dto/admin-users-query.dto';
import {
  AdminRepository,
  AdminUserSummaryRow,
  TaskStatusCounts,
  TripStatusCounts,
} from './repositories/admin.repository';

const DAY_MS = 24 * 60 * 60 * 1000;

/**
 * Admin-panel business logic. Read-only by design in this first version:
 * nothing here creates, changes or deletes data, and nothing returns the
 * content of a user's tasks or trips — only aggregate counts.
 */
@Injectable()
export class AdminService {
  constructor(
    private readonly usersService: UsersService,
    private readonly adminRepository: AdminRepository,
  ) {}

  /** The calling admin's own identity, read fresh from the database. */
  async getSession(adminId: string): Promise<AdminSessionResponseDto> {
    const user = await this.usersService.findById(adminId);
    if (!user) {
      throw new UnauthorizedException('Account no longer exists.');
    }
    return {
      id: user.id,
      email: user.email,
      displayName: user.displayName,
      role: user.role,
    };
  }

  /**
   * System-wide aggregates, computed with a fixed set of parallel count
   * queries. "Overdue" needs one "today" for all users, so the UTC date is
   * used here (a user detail uses that user's own timezone instead).
   */
  async getDashboard(now: Date = new Date()): Promise<AdminDashboardResponseDto> {
    const sinceWeek = new Date(now.getTime() - 7 * DAY_MS);
    const sinceMonth = new Date(now.getTime() - 30 * DAY_MS);
    const todayUtc = toDbDate(todayInTimeZone('UTC', now));

    const [users, activeLast7Days, taskCounts, overdue, tripCounts] = await Promise.all([
      this.adminRepository.countUsers(sinceWeek, sinceMonth),
      this.adminRepository.countUsersWithSessionSince(sinceWeek),
      this.adminRepository.countTasksByStatus(),
      this.adminRepository.countOverdueTasks(todayUtc),
      this.adminRepository.countTripsByStatus(),
    ]);

    return {
      users: {
        total: users.total,
        deleted: users.deleted,
        admins: users.admins,
        newLast7Days: users.newSinceWeek,
        newLast30Days: users.newSinceMonth,
        activeLast7Days,
      },
      tasks: this.toTaskStats(taskCounts, overdue),
      trips: this.toTripStats(tripCounts),
      generatedAt: now,
    };
  }

  /** One page of users; a single query, with no per-user follow-ups. */
  async listUsers(query: AdminUsersQueryDto): Promise<PaginatedAdminUsersResponseDto> {
    const descending = query.sort.startsWith('-');
    const sortField = (descending ? query.sort.slice(1) : query.sort) as AdminUserSortField;

    const { items, hasMore } = await this.adminRepository.listUsers({
      q: query.q,
      role: query.role,
      status: query.status,
      sortField,
      sortDirection: descending ? 'desc' : 'asc',
      cursor: query.cursor,
      limit: query.limit,
    });

    return {
      data: items.map((user) => this.toUserSummary(user)),
      meta: {
        nextCursor: hasMore ? items[items.length - 1].id : null,
        limit: query.limit,
        hasMore,
      },
    };
  }

  /**
   * One user's account summary and aggregate counts — also for a
   * soft-deleted account. "Overdue" is relative to today in that user's
   * own timezone, matching what the user sees in the app.
   */
  async getUserDetail(id: string, now: Date = new Date()): Promise<AdminUserDetailResponseDto> {
    const user = await this.adminRepository.findUserById(id);
    if (!user) {
      throw new NotFoundException('User not found.');
    }

    const userToday = toDbDate(todayInTimeZone(user.timezone, now));
    const [taskCounts, overdue, tripCounts, lastActiveAt] = await Promise.all([
      this.adminRepository.countTasksByStatus(id),
      this.adminRepository.countOverdueTasks(userToday, id),
      this.adminRepository.countTripsByStatus(id),
      this.adminRepository.findLastSessionAt(id),
    ]);

    return {
      ...this.toUserSummary(user),
      language: user.language,
      updatedAt: user.updatedAt,
      lastActiveAt,
      tasks: this.toTaskStats(taskCounts, overdue),
      trips: this.toTripStats(tripCounts),
    };
  }

  /** Explicit field-by-field mapping: only these fields can ever be returned, whatever the row carries. */
  private toUserSummary(user: AdminUserSummaryRow): AdminUserSummaryDto {
    return {
      id: user.id,
      email: user.email,
      displayName: user.displayName,
      role: user.role,
      timezone: user.timezone,
      createdAt: user.createdAt,
      deletedAt: user.deletedAt,
      isActive: user.deletedAt === null,
    };
  }

  private toTaskStats(counts: TaskStatusCounts, overdue: number): AdminTaskStatsDto {
    const todo = counts[TaskStatus.TODO] ?? 0;
    const inProgress = counts[TaskStatus.IN_PROGRESS] ?? 0;
    const done = counts[TaskStatus.DONE] ?? 0;
    const total = todo + inProgress + done;
    return {
      total,
      todo,
      inProgress,
      done,
      overdue,
      completionRate: total === 0 ? 0 : Math.round((done / total) * 100),
    };
  }

  private toTripStats(counts: TripStatusCounts): AdminTripStatsDto {
    const planned = counts[TripStatus.PLANNED] ?? 0;
    const ongoing = counts[TripStatus.ONGOING] ?? 0;
    const completed = counts[TripStatus.COMPLETED] ?? 0;
    const cancelled = counts[TripStatus.CANCELLED] ?? 0;
    return {
      total: planned + ongoing + completed + cancelled,
      planned,
      ongoing,
      completed,
      cancelled,
    };
  }
}
