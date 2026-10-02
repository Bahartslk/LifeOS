import { Injectable } from '@nestjs/common';
import { Prisma, Task, TaskCategory, TaskPriority, TaskSource, TaskStatus } from '@prisma/client';
import { PrismaService } from '../../../prisma/prisma.service';

export interface CreateTaskData {
  userId: string;
  title: string;
  description: string | null;
  dueDate: Date;
  dueTime: Date | null;
  priority: TaskPriority;
  category: TaskCategory;
  taskListId: string | null;
  /** Explicit, not defaulted here — `PlannerService.createTask` always passes `PLANNER`; `createIntegrationTask` passes whatever the calling feature module needs (e.g. `TRAVEL`). */
  source: TaskSource;
}

export type UpdateTaskData = Partial<
  Pick<
    CreateTaskData,
    'title' | 'description' | 'dueDate' | 'dueTime' | 'priority' | 'category' | 'taskListId'
  >
> & { status?: TaskStatus };

export interface FindManyQuery {
  status?: TaskStatus;
  priority?: TaskPriority;
  taskListId?: string;
  dueAfter?: Date;
  dueBefore?: Date;
  sortField: 'dueDate' | 'createdAt' | 'priority';
  sortDirection: 'asc' | 'desc';
  cursor?: string;
  limit: number;
}

export interface FindManyResult {
  items: Task[];
  hasMore: boolean;
}

/**
 * Sole data-access point for `tasks` — every method scopes by `userId` in
 * its `WHERE` clause (repository-layer ownership filtering, defense in
 * depth alongside `PlannerService`'s own ownership check, per
 * docs/15-api-design.md#authorization's two-layer model). No `create`/
 * `update`/`delete` call bypasses this.
 */
@Injectable()
export class TasksRepository {
  constructor(private readonly prisma: PrismaService) {}

  create(data: CreateTaskData): Promise<Task> {
    return this.prisma.task.create({
      data: {
        userId: data.userId,
        title: data.title,
        description: data.description,
        dueDate: data.dueDate,
        dueTime: data.dueTime,
        priority: data.priority,
        category: data.category,
        taskListId: data.taskListId,
        status: TaskStatus.TODO,
        source: data.source,
      },
    });
  }

  findById(id: string, userId: string): Promise<Task | null> {
    return this.prisma.task.findFirst({ where: { id, userId, deletedAt: null } });
  }

  async update(id: string, userId: string, data: UpdateTaskData): Promise<Task | null> {
    const result = await this.prisma.task.updateMany({
      where: { id, userId, deletedAt: null },
      data,
    });
    if (result.count === 0) return null;
    return this.findById(id, userId);
  }

  async softDelete(id: string, userId: string): Promise<boolean> {
    const result = await this.prisma.task.updateMany({
      where: { id, userId, deletedAt: null },
      data: { deletedAt: new Date() },
    });
    return result.count > 0;
  }

  /** Backs `GET /planner/tasks` — filters/sort per docs/15-api-design.md#filtering-and-sorting, cursor pagination per docs/15-api-design.md#pagination. */
  async findMany(userId: string, query: FindManyQuery): Promise<FindManyResult> {
    const where: Prisma.TaskWhereInput = {
      userId,
      deletedAt: null,
      ...(query.status && { status: query.status }),
      ...(query.priority && { priority: query.priority }),
      ...(query.taskListId && { taskListId: query.taskListId }),
      ...((query.dueAfter || query.dueBefore) && {
        dueDate: {
          ...(query.dueAfter && { gte: query.dueAfter }),
          ...(query.dueBefore && { lte: query.dueBefore }),
        },
      }),
    };

    // Fetch one extra row to detect hasMore without a separate count query.
    // `id` is always appended as a tiebreaker so paging stays stable when
    // the sort field has duplicate values across rows.
    const rows = await this.prisma.task.findMany({
      where,
      orderBy: [{ [query.sortField]: query.sortDirection }, { id: 'asc' }],
      ...(query.cursor && { cursor: { id: query.cursor }, skip: 1 }),
      take: query.limit + 1,
    });

    const hasMore = rows.length > query.limit;
    return { items: hasMore ? rows.slice(0, query.limit) : rows, hasMore };
  }

  /** Every active task due exactly on `today`, any status — Planner Dashboard's "today" bucket, ordered for a same-day checklist view. */
  findDueOn(userId: string, date: Date): Promise<Task[]> {
    return this.prisma.task.findMany({
      where: { userId, deletedAt: null, dueDate: date },
      orderBy: [{ dueTime: 'asc' }, { createdAt: 'asc' }],
    });
  }

  /**
   * Every active, unfinished (TODO/IN_PROGRESS) task due strictly before
   * `today` — Planner Dashboard's "overdue" bucket. Date-based only: a task
   * due today is never overdue here, even once its `dueTime` has passed.
   * Served by the partial `(user_id, due_date) WHERE deleted_at IS NULL`
   * index (see the `add_planner` migration). `createdAt` is the final
   * tie-breaker so the order is stable.
   */
  findOverdue(userId: string, today: Date): Promise<Task[]> {
    return this.prisma.task.findMany({
      where: {
        userId,
        deletedAt: null,
        dueDate: { lt: today },
        status: { in: [TaskStatus.TODO, TaskStatus.IN_PROGRESS] },
      },
      orderBy: [{ dueDate: 'asc' }, { dueTime: 'asc' }, { createdAt: 'asc' }],
    });
  }

  /** Every active task due strictly after `today`, any status — Planner Dashboard's "upcoming" bucket. No cutoff window: all future tasks, per this sprint's scope decision (see `PlannerService`). */
  findDueAfter(userId: string, date: Date): Promise<Task[]> {
    return this.prisma.task.findMany({
      where: { userId, deletedAt: null, dueDate: { gt: date } },
      orderBy: [{ dueDate: 'asc' }, { dueTime: 'asc' }],
    });
  }

  /** Backs the Dashboard's `completedCount`/`pendingCount`/`progressPercentage` — the caller's ENTIRE active task set, not just today/upcoming (matching mobile's `PlannerOverview` semantics; see `PlannerService.getDashboard`). */
  async countTotalAndCompleted(userId: string): Promise<{ total: number; completed: number }> {
    const [total, completed] = await Promise.all([
      this.prisma.task.count({ where: { userId, deletedAt: null } }),
      this.prisma.task.count({ where: { userId, deletedAt: null, status: TaskStatus.DONE } }),
    ]);
    return { total, completed };
  }
}
