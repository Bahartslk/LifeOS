import { randomUUID } from 'crypto';
import { TaskStatus, TripStatus, User } from '@prisma/client';

/**
 * An in-memory stand-in for the parts of Prisma that `AdminRepository`
 * uses, evaluating the real `where`/`select`/`orderBy`/`cursor`/`take`
 * arguments it sends. Specs run the real repository against it, so the
 * filters themselves (soft-delete, role, search, overdue...) are under
 * test, not a hand-written mock of their results.
 *
 * `task.findMany`/`trip.findMany` and friends exist only as spies: the
 * admin API must never load a row of user content, and specs assert that
 * these were not called.
 */

export interface TaskRecord {
  id: string;
  userId: string;
  title: string;
  description: string | null;
  status: TaskStatus;
  dueDate: Date;
  deletedAt: Date | null;
}

export interface TripRecord {
  id: string;
  userId: string;
  title: string;
  destination: string;
  country: string;
  status: TripStatus;
  deletedAt: Date | null;
}

export interface RefreshTokenRecord {
  id: string;
  userId: string;
  tokenHash: string;
  createdAt: Date;
}

export interface AdminSeed {
  tasks?: TaskRecord[];
  trips?: TripRecord[];
  refreshTokens?: RefreshTokenRecord[];
}

export function testTask(overrides: Partial<TaskRecord> & { userId: string }): TaskRecord {
  return {
    id: randomUUID(),
    title: 'Task title',
    description: null,
    status: TaskStatus.TODO,
    dueDate: new Date('2026-12-31T00:00:00.000Z'),
    deletedAt: null,
    ...overrides,
  };
}

export function testTrip(overrides: Partial<TripRecord> & { userId: string }): TripRecord {
  return {
    id: randomUUID(),
    title: 'Trip title',
    destination: 'Destination',
    country: 'Country',
    status: TripStatus.PLANNED,
    deletedAt: null,
    ...overrides,
  };
}

export function testRefreshToken(
  overrides: Partial<RefreshTokenRecord> & { userId: string },
): RefreshTokenRecord {
  return {
    id: randomUUID(),
    tokenHash: 'not-a-real-token-hash',
    createdAt: new Date('2026-10-01T10:00:00.000Z'),
    ...overrides,
  };
}

type Row = Record<string, unknown>;
type Where = Record<string, unknown>;

function compare(a: unknown, b: unknown): number {
  const left = a instanceof Date ? a.getTime() : (a as number | string);
  const right = b instanceof Date ? b.getTime() : (b as number | string);
  return left < right ? -1 : left > right ? 1 : 0;
}

function matchesCondition(value: unknown, condition: unknown): boolean {
  if (condition === null) return value === null;
  if (condition instanceof Date || typeof condition !== 'object') {
    return compare(value, condition) === 0;
  }
  const { mode, ...operators } = condition as Record<string, unknown>;
  return Object.entries(operators).every(([operator, operand]) => {
    switch (operator) {
      case 'not':
        return !matchesCondition(value, operand);
      case 'gte':
        return compare(value, operand) >= 0;
      case 'lt':
        return compare(value, operand) < 0;
      case 'in':
        return (operand as unknown[]).includes(value);
      case 'contains': {
        const text = String(value);
        const needle = String(operand);
        return mode === 'insensitive'
          ? text.toLowerCase().includes(needle.toLowerCase())
          : text.includes(needle);
      }
      default:
        throw new Error(`Unsupported operator in test double: ${operator}`);
    }
  });
}

export function createAdminPrismaDelegates(users: User[], seed: AdminSeed = {}) {
  const tasks = seed.tasks ?? [];
  const trips = seed.trips ?? [];
  const refreshTokens = seed.refreshTokens ?? [];

  const matches = (row: Row, where: Where = {}): boolean =>
    Object.entries(where).every(([field, condition]) => {
      if (field === 'OR') {
        return (condition as Where[]).some((branch) => matches(row, branch));
      }
      if (field === 'user') {
        // Relation filter: evaluated against the owning account.
        const owner = users.find((user) => user.id === row.userId);
        return owner !== undefined && matches(owner as unknown as Row, condition as Where);
      }
      return matchesCondition(row[field], condition);
    });

  const pick = (row: Row, select?: Record<string, boolean>): Row =>
    select
      ? Object.fromEntries(Object.keys(select).map((field) => [field, row[field]]))
      : { ...row };

  const groupByStatus = (rows: Row[], where?: Where) => {
    const counts = new Map<unknown, number>();
    for (const row of rows.filter((candidate) => matches(candidate, where))) {
      counts.set(row.status, (counts.get(row.status) ?? 0) + 1);
    }
    return [...counts].map(([status, count]) => ({ status, _count: { _all: count } }));
  };

  /** Spies only — reading content rows is exactly what the admin API must not do. */
  const contentReaders = (rows: Row[]) => ({
    findMany: jest.fn(async () => rows),
    findFirst: jest.fn(async () => rows[0] ?? null),
    findUnique: jest.fn(async () => rows[0] ?? null),
  });

  const userRows = () => users as unknown as Row[];

  return {
    user: {
      findMany: jest.fn(
        async (args: {
          where?: Where;
          select?: Record<string, boolean>;
          orderBy?: Record<string, 'asc' | 'desc'>[];
          cursor?: { id: string };
          skip?: number;
          take?: number;
        }) => {
          const sorted = userRows()
            .filter((row) => matches(row, args.where))
            .sort((a, b) => {
              for (const clause of args.orderBy ?? []) {
                const [field, direction] = Object.entries(clause)[0];
                const result = compare(a[field], b[field]);
                if (result !== 0) return direction === 'desc' ? -result : result;
              }
              return 0;
            });
          const start = args.cursor
            ? sorted.findIndex((row) => row.id === args.cursor?.id) + (args.skip ?? 0)
            : 0;
          return sorted
            .slice(start, args.take === undefined ? undefined : start + args.take)
            .map((row) => pick(row, args.select));
        },
      ),
      findUnique: jest.fn(
        async (args: { where: { id: string }; select?: Record<string, boolean> }) => {
          const row = userRows().find((candidate) => candidate.id === args.where.id);
          return row ? pick(row, args.select) : null;
        },
      ),
      count: jest.fn(
        async (args: { where?: Where } = {}) =>
          userRows().filter((row) => matches(row, args.where)).length,
      ),
    },
    task: {
      ...contentReaders(tasks as unknown as Row[]),
      groupBy: jest.fn(async (args: { where?: Where }) =>
        groupByStatus(tasks as unknown as Row[], args.where),
      ),
      count: jest.fn(
        async (args: { where?: Where } = {}) =>
          (tasks as unknown as Row[]).filter((row) => matches(row, args.where)).length,
      ),
    },
    trip: {
      ...contentReaders(trips as unknown as Row[]),
      groupBy: jest.fn(async (args: { where?: Where }) =>
        groupByStatus(trips as unknown as Row[], args.where),
      ),
    },
    refreshToken: {
      findMany: jest.fn(async () => refreshTokens),
      groupBy: jest.fn(async (args: { where?: Where }) => {
        const userIds = new Set(
          (refreshTokens as unknown as Row[])
            .filter((row) => matches(row, args.where))
            .map((row) => row.userId),
        );
        return [...userIds].map((userId) => ({ userId }));
      }),
      aggregate: jest.fn(async (args: { where?: Where }) => {
        const created = (refreshTokens as unknown as Row[])
          .filter((row) => matches(row, args.where))
          .map((row) => row.createdAt as Date);
        const newest = created.reduce<Date | null>(
          (latest, date) => (latest === null || date > latest ? date : latest),
          null,
        );
        return { _max: { createdAt: newest } };
      }),
    },
  };
}

export type AdminPrismaDelegates = ReturnType<typeof createAdminPrismaDelegates>;
