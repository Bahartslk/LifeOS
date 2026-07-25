import { Injectable, NotFoundException } from '@nestjs/common';
import { Task, TaskPriority, TaskSource, TaskStatus } from '@prisma/client';
import { TaskListsRepository } from './repositories/task-lists.repository';
import { TasksRepository } from './repositories/tasks.repository';
import { CreateTaskDto } from './dto/create-task.dto';
import { UpdateTaskDto } from './dto/update-task.dto';
import { TaskQueryDto } from './dto/task-query.dto';
import { TaskResponseDto } from './dto/task-response.dto';
import { PaginatedTasksResponseDto } from './dto/paginated-tasks-response.dto';
import { PlannerDashboardResponseDto } from './dto/dashboard-response.dto';
import { fromDbDate, fromDbTime, toDbDate, toDbTime } from '../../common/utils/date-time.util';

type SortField = 'dueDate' | 'createdAt' | 'priority';

export interface CreateIntegrationTaskData {
  title: string;
  description?: string | null;
  dueDate: string;
  dueTime?: string | null;
  priority: TaskPriority;
  source: TaskSource;
}

/**
 * All Planner business logic — controllers stay thin, per CLAUDE.md's
 * "business logic stays out of controllers" rule (same split Sprint 14.0's
 * `AuthService` established). Every method that takes a task `id` enforces
 * ownership by requiring `userId` and treating "exists but not the
 * caller's" identically to "doesn't exist" — a 404, never a 403, per
 * docs/15-api-design.md#authorization ("avoids confirming to a caller that
 * a given resource ID exists at all when it isn't theirs").
 */
@Injectable()
export class PlannerService {
  constructor(
    private readonly tasksRepository: TasksRepository,
    private readonly taskListsRepository: TaskListsRepository,
  ) {}

  async createTask(userId: string, dto: CreateTaskDto): Promise<TaskResponseDto> {
    await this.assertTaskListOwnership(userId, dto.taskListId);

    const task = await this.tasksRepository.create({
      userId,
      title: dto.title,
      description: dto.description ?? null,
      dueDate: toDbDate(dto.dueDate),
      dueTime: dto.dueTime ? toDbTime(dto.dueTime) : null,
      priority: dto.priority,
      taskListId: dto.taskListId ?? null,
      source: TaskSource.PLANNER,
    });

    return this.toResponse(task);
  }

  /**
   * Sprint 16.0's Planner integration point — NOT exposed via
   * `PlannerController` (no HTTP route calls this); other feature modules
   * inject `PlannerService` directly (`PlannerModule` exports it) and call
   * this to create a task with a source other than `PLANNER`, which
   * `createTask`/`CreateTaskDto` deliberately never allow an HTTP caller to
   * set. Currently used by `ItineraryService` (`source: TaskSource.TRAVEL`)
   * per "When an itinerary item requires a task: create/update Planner
   * task, source = TRAVEL. Travel never duplicates Planner task data." No
   * `taskListId` support — not requested by any current caller, kept out
   * per "avoid premature abstraction". Updating/deleting an
   * integration-created task reuses the existing public `updateTask`/
   * `deleteTask` (both already source-agnostic), so no
   * `updateIntegrationTask` counterpart is needed.
   */
  async createIntegrationTask(
    userId: string,
    data: CreateIntegrationTaskData,
  ): Promise<TaskResponseDto> {
    const task = await this.tasksRepository.create({
      userId,
      title: data.title,
      description: data.description ?? null,
      dueDate: toDbDate(data.dueDate),
      dueTime: data.dueTime ? toDbTime(data.dueTime) : null,
      priority: data.priority,
      taskListId: null,
      source: data.source,
    });

    return this.toResponse(task);
  }

  async updateTask(userId: string, id: string, dto: UpdateTaskDto): Promise<TaskResponseDto> {
    await this.assertTaskListOwnership(userId, dto.taskListId);

    const task = await this.tasksRepository.update(id, userId, {
      ...(dto.title !== undefined && { title: dto.title }),
      ...(dto.description !== undefined && { description: dto.description }),
      ...(dto.dueDate !== undefined && { dueDate: toDbDate(dto.dueDate) }),
      ...(dto.dueTime !== undefined && { dueTime: toDbTime(dto.dueTime) }),
      ...(dto.priority !== undefined && { priority: dto.priority }),
      ...(dto.status !== undefined && { status: dto.status }),
      ...(dto.taskListId !== undefined && { taskListId: dto.taskListId }),
    });

    if (!task) {
      throw new NotFoundException('Task not found.');
    }
    return this.toResponse(task);
  }

  async deleteTask(userId: string, id: string): Promise<void> {
    const deleted = await this.tasksRepository.softDelete(id, userId);
    if (!deleted) {
      throw new NotFoundException('Task not found.');
    }
  }

  async getTask(userId: string, id: string): Promise<TaskResponseDto> {
    const task = await this.tasksRepository.findById(id, userId);
    if (!task) {
      throw new NotFoundException('Task not found.');
    }
    return this.toResponse(task);
  }

  async getTasks(userId: string, query: TaskQueryDto): Promise<PaginatedTasksResponseDto> {
    const { field, direction } = this.parseSort(query.sort);

    const { items, hasMore } = await this.tasksRepository.findMany(userId, {
      status: query.status,
      priority: query.priority,
      taskListId: query.taskListId,
      dueAfter: query.dueAfter ? toDbDate(query.dueAfter) : undefined,
      dueBefore: query.dueBefore ? toDbDate(query.dueBefore) : undefined,
      sortField: field,
      sortDirection: direction,
      cursor: query.cursor,
      limit: query.limit,
    });

    return {
      data: items.map((task) => this.toResponse(task)),
      meta: {
        nextCursor: hasMore ? items[items.length - 1].id : null,
        limit: query.limit,
        hasMore,
      },
    };
  }

  async markComplete(userId: string, id: string): Promise<TaskResponseDto> {
    return this.setStatus(userId, id, TaskStatus.DONE);
  }

  async markIncomplete(userId: string, id: string): Promise<TaskResponseDto> {
    return this.setStatus(userId, id, TaskStatus.TODO);
  }

  /**
   * `GET /planner/dashboard` — a new, backend-computed synthesis of
   * mobile's `PlannerDashboard` and AI Assistant's `AiTaskContext` (see
   * `PlannerDashboardResponseDto`'s doc comment). Field-by-field derivation
   * matches `BuildAiTaskContextUseCase`'s classification exactly:
   *
   * - `todayTasks`/`upcomingTasks`: real DB queries (dueDate = today /
   *   dueDate > today), any status.
   * - `completedCount`/`pendingCount`/`progressPercentage`: over the
   *   caller's ENTIRE active task set, not just today/upcoming — matching
   *   how mobile's `PlannerOverview` fake data deliberately counts more
   *   tasks than `todayTasks`/`upcomingTasks` show (a real backlog total,
   *   not a sum of the two visible lists).
   * - `highPriorityTasks`: incomplete (status != DONE), priority HIGH,
   *   drawn from `todayTasks + upcomingTasks` (not the full backlog).
   * - `travelTasks`: source = TRAVEL, drawn from `todayTasks +
   *   upcomingTasks`, any status.
   */
  async getDashboard(userId: string): Promise<PlannerDashboardResponseDto> {
    const today = this.todayUtcMidnight();

    const [todayTasks, upcomingTasks, counts] = await Promise.all([
      this.tasksRepository.findDueOn(userId, today),
      this.tasksRepository.findDueAfter(userId, today),
      this.tasksRepository.countTotalAndCompleted(userId),
    ]);

    const combined = [...todayTasks, ...upcomingTasks];
    const incomplete = combined.filter((task) => task.status !== TaskStatus.DONE);

    return {
      todayTasks: todayTasks.map((task) => this.toResponse(task)),
      upcomingTasks: upcomingTasks.map((task) => this.toResponse(task)),
      completedCount: counts.completed,
      pendingCount: counts.total - counts.completed,
      progressPercentage:
        counts.total === 0 ? 0 : Math.round((counts.completed / counts.total) * 100),
      highPriorityTasks: incomplete
        .filter((task) => task.priority === TaskPriority.HIGH)
        .map((task) => this.toResponse(task)),
      travelTasks: combined
        .filter((task) => task.source === TaskSource.TRAVEL)
        .map((task) => this.toResponse(task)),
    };
  }

  private async setStatus(
    userId: string,
    id: string,
    status: TaskStatus,
  ): Promise<TaskResponseDto> {
    const task = await this.tasksRepository.update(id, userId, { status });
    if (!task) {
      throw new NotFoundException('Task not found.');
    }
    return this.toResponse(task);
  }

  /**
   * `undefined` (field omitted from the request body) is a no-op — nothing
   * to check. `null` is also skipped here despite `class-validator`'s
   * `@IsOptional()` allowing a client to send it explicitly (it treats both
   * `undefined` and `null` as "empty" and skips `@IsUUID()`) — `null` means
   * "detach from any list", which needs no ownership check either, only a
   * real UUID does.
   */
  private async assertTaskListOwnership(
    userId: string,
    taskListId: string | null | undefined,
  ): Promise<void> {
    if (!taskListId) return;
    const taskList = await this.taskListsRepository.findById(taskListId, userId);
    if (!taskList) {
      throw new NotFoundException('Task list not found.');
    }
  }

  private parseSort(sort: string | undefined): { field: SortField; direction: 'asc' | 'desc' } {
    if (!sort) return { field: 'dueDate', direction: 'asc' };
    const direction = sort.startsWith('-') ? 'desc' : 'asc';
    const field = (sort.startsWith('-') ? sort.slice(1) : sort) as SortField;
    return { field, direction };
  }

  private todayUtcMidnight(): Date {
    return new Date(`${new Date().toISOString().slice(0, 10)}T00:00:00.000Z`);
  }

  private toResponse(task: Task): TaskResponseDto {
    return {
      id: task.id,
      title: task.title,
      description: task.description,
      dueDate: fromDbDate(task.dueDate),
      dueTime: fromDbTime(task.dueTime),
      priority: task.priority,
      status: task.status,
      source: task.source,
      taskListId: task.taskListId,
      createdAt: task.createdAt,
      updatedAt: task.updatedAt,
    };
  }
}
