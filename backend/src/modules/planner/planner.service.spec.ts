import { Task, TaskCategory, TaskPriority, TaskSource, TaskStatus } from '@prisma/client';
import { PrismaService } from '../../prisma/prisma.service';
import { UsersService } from '../users/users.service';
import { PlannerService } from './planner.service';
import { TaskListsRepository } from './repositories/task-lists.repository';
import { TasksRepository } from './repositories/tasks.repository';

const USER_ID = 'user-1';
const OTHER_USER_ID = 'user-2';

type Where = Record<string, unknown>;
type OrderBy = Record<string, 'asc' | 'desc'>[];

/**
 * A minimal in-memory stand-in for `prisma.task`, evaluating exactly the
 * `where`/`orderBy` shapes `TasksRepository` uses (equality, `lt`/`gt`/
 * `gte`/`lte`, `in`, `null`). Lets these tests run the real
 * `TasksRepository` queries — so the overdue filter itself (status,
 * soft-delete, owner, date) is under test, not a hand-written mock of it.
 */
function inMemoryPrisma(tasks: Task[]): PrismaService {
  const matches = (task: Task, where: Where) =>
    Object.entries(where).every(([field, condition]) => {
      const value = (task as unknown as Record<string, unknown>)[field];
      if (condition === null || condition instanceof Date || typeof condition !== 'object') {
        return compare(value, condition) === 0;
      }
      const ops = condition as Record<string, unknown>;
      return Object.entries(ops).every(([op, operand]) => {
        switch (op) {
          case 'lt':
            return compare(value, operand) < 0;
          case 'gt':
            return compare(value, operand) > 0;
          case 'gte':
            return compare(value, operand) >= 0;
          case 'lte':
            return compare(value, operand) <= 0;
          case 'in':
            return (operand as unknown[]).includes(value);
          default:
            throw new Error(`Unsupported operator in test double: ${op}`);
        }
      });
    });

  const sort = (rows: Task[], orderBy: OrderBy = []) =>
    [...rows].sort((a, b) => {
      for (const clause of orderBy) {
        const [field, direction] = Object.entries(clause)[0];
        const av = (a as unknown as Record<string, unknown>)[field];
        const bv = (b as unknown as Record<string, unknown>)[field];
        // PostgreSQL puts NULLs last in ascending order.
        if (av === null && bv !== null) return 1;
        if (bv === null && av !== null) return -1;
        const result = compare(av, bv) * (direction === 'desc' ? -1 : 1);
        if (result !== 0) return result;
      }
      return 0;
    });

  return {
    task: {
      findMany: jest.fn(async ({ where, orderBy }: { where: Where; orderBy?: OrderBy }) =>
        sort(
          tasks.filter((task) => matches(task, where)),
          orderBy,
        ),
      ),
      count: jest.fn(
        async ({ where }: { where: Where }) => tasks.filter((task) => matches(task, where)).length,
      ),
    },
  } as unknown as PrismaService;
}

function compare(a: unknown, b: unknown): number {
  const norm = (v: unknown) => (v instanceof Date ? v.getTime() : v);
  const x = norm(a) as number | string | null;
  const y = norm(b) as number | string | null;
  if (x === y) return 0;
  if (x === null || x === undefined) return -1;
  if (y === null || y === undefined) return 1;
  return x < y ? -1 : 1;
}

function task(
  id: string,
  dueDate: string,
  overrides: Partial<Omit<Task, 'dueDate' | 'dueTime'>> & { dueTime?: string | null } = {},
): Task {
  const { dueTime, ...rest } = overrides;
  return {
    id,
    userId: USER_ID,
    taskListId: null,
    title: id,
    description: null,
    dueDate: new Date(`${dueDate}T00:00:00.000Z`),
    dueTime: dueTime ? new Date(`1970-01-01T${dueTime}:00.000Z`) : null,
    priority: TaskPriority.MEDIUM,
    category: TaskCategory.PERSONAL,
    status: TaskStatus.TODO,
    source: TaskSource.PLANNER,
    createdAt: new Date('2026-09-01T10:00:00.000Z'),
    updatedAt: new Date('2026-09-01T10:00:00.000Z'),
    deletedAt: null,
    ...rest,
  };
}

describe('PlannerService.getDashboard', () => {
  let getProfile: jest.Mock;

  function serviceWith(tasks: Task[], timezone = 'Europe/Istanbul'): PlannerService {
    getProfile = jest.fn().mockResolvedValue({ timezone });
    const usersService = { getProfile } as unknown as UsersService;
    return new PlannerService(
      new TasksRepository(inMemoryPrisma(tasks)),
      {} as TaskListsRepository,
      usersService,
    );
  }

  const ids = (list: { id: string }[]) => list.map((t) => t.id);

  afterEach(() => jest.useRealTimers());

  it('uses an explicit date as "today" without reading the profile timezone', async () => {
    const service = serviceWith([
      task('yesterday', '2026-10-01'),
      task('today', '2026-10-02'),
      task('tomorrow', '2026-10-03'),
    ]);

    const dashboard = await service.getDashboard(USER_ID, '2026-10-02');

    expect(ids(dashboard.overdueTasks)).toEqual(['yesterday']);
    expect(ids(dashboard.todayTasks)).toEqual(['today']);
    expect(ids(dashboard.upcomingTasks)).toEqual(['tomorrow']);
    expect(getProfile).not.toHaveBeenCalled();
  });

  it.each([
    ['2026-10-01T22:30:00Z', '01:30 Istanbul, while UTC is still Oct 1'],
    ['2026-10-02T00:30:00Z', '03:30 Istanbul'],
  ])('without a date, resolves today in Europe/Istanbul at %s (%s)', async (now) => {
    jest.useFakeTimers({ now: new Date(now) });
    const service = serviceWith([task('oct-1', '2026-10-01'), task('oct-2', '2026-10-02')]);

    const dashboard = await service.getDashboard(USER_ID);

    expect(getProfile).toHaveBeenCalledWith(USER_ID);
    expect(ids(dashboard.todayTasks)).toEqual(['oct-2']);
    expect(ids(dashboard.overdueTasks)).toEqual(['oct-1']);
  });

  it("without a date, follows the user's own profile timezone", async () => {
    // 03:00 UTC on Oct 2 is still Oct 1 (23:00) in New York.
    jest.useFakeTimers({ now: new Date('2026-10-02T03:00:00Z') });
    const service = serviceWith(
      [task('oct-1', '2026-10-01'), task('oct-2', '2026-10-02')],
      'America/New_York',
    );

    const dashboard = await service.getDashboard(USER_ID);

    expect(ids(dashboard.todayTasks)).toEqual(['oct-1']);
    expect(ids(dashboard.upcomingTasks)).toEqual(['oct-2']);
    expect(dashboard.overdueTasks).toEqual([]);
  });

  it('classifies overdue strictly by date and unfinished status', async () => {
    const service = serviceWith([
      task('past-todo', '2026-09-30', { status: TaskStatus.TODO }),
      task('past-in-progress', '2026-09-29', { status: TaskStatus.IN_PROGRESS }),
      task('past-done', '2026-09-28', { status: TaskStatus.DONE }),
      task('today-todo', '2026-10-02', { status: TaskStatus.TODO, dueTime: '00:15' }),
      task('future-todo', '2026-10-09', { status: TaskStatus.TODO }),
    ]);

    const dashboard = await service.getDashboard(USER_ID, '2026-10-02');

    expect(ids(dashboard.overdueTasks).sort()).toEqual(['past-in-progress', 'past-todo']);
    // A finished past task is in no list at all.
    const allListed = [
      ...dashboard.overdueTasks,
      ...dashboard.todayTasks,
      ...dashboard.upcomingTasks,
    ];
    expect(ids(allListed)).not.toContain('past-done');
    // Today's task is never overdue, even with its due time long gone.
    expect(ids(dashboard.todayTasks)).toEqual(['today-todo']);
    expect(ids(dashboard.upcomingTasks)).toEqual(['future-todo']);
  });

  it('keeps overdue, today and upcoming disjoint', async () => {
    const service = serviceWith([
      task('a', '2026-09-15'),
      task('b', '2026-10-01', { status: TaskStatus.IN_PROGRESS }),
      task('c', '2026-10-02'),
      task('d', '2026-10-02', { status: TaskStatus.DONE }),
      task('e', '2026-10-03'),
      task('f', '2026-11-20'),
    ]);

    const dashboard = await service.getDashboard(USER_ID, '2026-10-02');

    const overdue = ids(dashboard.overdueTasks);
    const today = ids(dashboard.todayTasks);
    const upcoming = ids(dashboard.upcomingTasks);
    expect(overdue).toEqual(['a', 'b']);
    expect(today.sort()).toEqual(['c', 'd']);
    expect(upcoming).toEqual(['e', 'f']);
    const all = [...overdue, ...today, ...upcoming];
    expect(new Set(all).size).toBe(all.length);
  });

  it('orders overdue tasks by due date, then due time (untimed last), then creation', async () => {
    const service = serviceWith([
      task('sep-30-untimed', '2026-09-30'),
      task('sep-30-late', '2026-09-30', { dueTime: '18:00' }),
      task('sep-30-early-second', '2026-09-30', {
        dueTime: '09:00',
        createdAt: new Date('2026-09-02T00:00:00Z'),
      }),
      task('sep-30-early-first', '2026-09-30', {
        dueTime: '09:00',
        createdAt: new Date('2026-09-01T00:00:00Z'),
      }),
      task('sep-01', '2026-09-01'),
    ]);

    const dashboard = await service.getDashboard(USER_ID, '2026-10-02');

    expect(ids(dashboard.overdueTasks)).toEqual([
      'sep-01',
      'sep-30-early-first',
      'sep-30-early-second',
      'sep-30-late',
      'sep-30-untimed',
    ]);
  });

  it('includes overdue high-priority tasks in highPriorityTasks exactly once, unfinished only', async () => {
    const service = serviceWith([
      task('overdue-high', '2026-09-30', { priority: TaskPriority.HIGH }),
      task('overdue-high-done', '2026-09-30', {
        priority: TaskPriority.HIGH,
        status: TaskStatus.DONE,
      }),
      task('overdue-medium', '2026-09-30'),
      task('today-high', '2026-10-02', { priority: TaskPriority.HIGH }),
      task('today-high-done', '2026-10-02', {
        priority: TaskPriority.HIGH,
        status: TaskStatus.DONE,
      }),
      task('upcoming-high', '2026-10-05', { priority: TaskPriority.HIGH }),
    ]);

    const dashboard = await service.getDashboard(USER_ID, '2026-10-02');

    expect(ids(dashboard.highPriorityTasks)).toEqual([
      'overdue-high',
      'today-high',
      'upcoming-high',
    ]);
  });

  it('returns an empty overdue list when nothing is overdue', async () => {
    const service = serviceWith([
      task('past-done', '2026-09-30', { status: TaskStatus.DONE }),
      task('today', '2026-10-02'),
    ]);

    const dashboard = await service.getDashboard(USER_ID, '2026-10-02');

    expect(dashboard.overdueTasks).toEqual([]);
  });

  it("never lists another user's or soft-deleted tasks as overdue", async () => {
    const service = serviceWith([
      task('mine', '2026-09-30'),
      task('theirs', '2026-09-30', { userId: OTHER_USER_ID }),
      task('deleted', '2026-09-30', { deletedAt: new Date('2026-09-30T12:00:00Z') }),
    ]);

    const dashboard = await service.getDashboard(USER_ID, '2026-10-02');

    expect(ids(dashboard.overdueTasks)).toEqual(['mine']);
  });

  it('leaves counts and travelTasks semantics unchanged', async () => {
    const service = serviceWith([
      task('overdue-travel', '2026-09-30', { source: TaskSource.TRAVEL }),
      task('today-travel', '2026-10-02', { source: TaskSource.TRAVEL }),
      task('done', '2026-09-20', { status: TaskStatus.DONE }),
      task('upcoming', '2026-10-10'),
    ]);

    const dashboard = await service.getDashboard(USER_ID, '2026-10-02');

    // Counts stay all-time over the active task set (overdue included).
    expect(dashboard.completedCount).toBe(1);
    expect(dashboard.pendingCount).toBe(3);
    expect(dashboard.progressPercentage).toBe(25);
    // travelTasks is still drawn from today + upcoming only.
    expect(ids(dashboard.travelTasks)).toEqual(['today-travel']);
  });

  it('maps overdue tasks to the same response shape as the other lists', async () => {
    const service = serviceWith([task('late', '2026-09-30', { dueTime: '14:30' })]);

    const [overdue] = (await service.getDashboard(USER_ID, '2026-10-02')).overdueTasks;

    expect(overdue).toMatchObject({
      id: 'late',
      dueDate: '2026-09-30',
      dueTime: '14:30',
      status: TaskStatus.TODO,
    });
  });
});
