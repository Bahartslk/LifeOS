import { UserRole } from '@prisma/client';
import {
  describeDatabaseTarget,
  describeOutcome,
  parseArgs,
  setAdminRole,
  UsageError,
  UserStore,
} from './promote-admin';

interface StoredUser {
  id: string;
  email: string;
  role: UserRole;
  deletedAt: Date | null;
  passwordHash: string;
}

/** An in-memory `prisma.user` honoring the exact `where` this tool sends. */
function storeWith(users: StoredUser[]) {
  const findFirst = jest.fn(
    async ({
      where,
    }: {
      where: { email: { equals: string; mode?: string }; deletedAt: null };
      select?: Record<string, boolean>;
    }) => {
      const found = users.find(
        (u) => u.email.toLowerCase() === where.email.equals.toLowerCase() && u.deletedAt === null,
      );
      return found ? { id: found.id, email: found.email, role: found.role } : null;
    },
  );
  const update = jest.fn(
    async ({ where, data }: { where: { id: string }; data: { role: UserRole } }) => {
      const target = users.find((u) => u.id === where.id)!;
      Object.assign(target, data);
      return target;
    },
  );
  return { store: { user: { findFirst, update } } as unknown as UserStore, findFirst, update };
}

const user = (overrides: Partial<StoredUser> = {}): StoredUser => ({
  id: 'u-1',
  email: 'person@example.com',
  role: UserRole.USER,
  deletedAt: null,
  passwordHash: 'original-hash',
  ...overrides,
});

describe('promote-admin: parseArgs', () => {
  it('defaults to a dry-run promotion', () => {
    expect(parseArgs(['person@example.com'])).toEqual({
      email: 'person@example.com',
      revoke: false,
      confirm: false,
    });
  });

  it('reads --confirm and --revoke in any position', () => {
    expect(parseArgs(['--revoke', 'person@example.com', '--confirm'])).toEqual({
      email: 'person@example.com',
      revoke: true,
      confirm: true,
    });
  });

  it.each([
    [[], 'Exactly one email'],
    [['a@example.com', 'b@example.com'], 'Exactly one email'],
    [['not-an-email'], 'Not a valid email'],
    [['person@example.com', '--force'], 'Unknown option: --force'],
    [['person@example.com', '--role=ADMIN'], 'Unknown option'],
  ])('rejects %j', (argv, message) => {
    expect(() => parseArgs(argv)).toThrow(UsageError);
    expect(() => parseArgs(argv)).toThrow(message);
  });
});

describe('promote-admin: setAdminRole', () => {
  it('promotes an existing user to ADMIN when confirmed', async () => {
    const users = [user()];
    const { store, update } = storeWith(users);

    const outcome = await setAdminRole(store, {
      email: 'person@example.com',
      revoke: false,
      confirm: true,
    });

    expect(outcome).toEqual({
      status: 'updated',
      account: { id: 'u-1', email: 'person@example.com', role: UserRole.USER },
      targetRole: UserRole.ADMIN,
    });
    expect(users[0].role).toBe(UserRole.ADMIN);
    expect(update).toHaveBeenCalledWith({ where: { id: 'u-1' }, data: { role: UserRole.ADMIN } });
  });

  it('changes nothing but the role', async () => {
    const users = [user()];
    const { store } = storeWith(users);

    await setAdminRole(store, { email: 'person@example.com', revoke: false, confirm: true });

    expect(users[0]).toMatchObject({
      email: 'person@example.com',
      passwordHash: 'original-hash',
      deletedAt: null,
    });
  });

  it('writes nothing without --confirm (dry run)', async () => {
    const users = [user()];
    const { store, update } = storeWith(users);

    const outcome = await setAdminRole(store, {
      email: 'person@example.com',
      revoke: false,
      confirm: false,
    });

    expect(outcome.status).toBe('dry-run');
    expect(update).not.toHaveBeenCalled();
    expect(users[0].role).toBe(UserRole.USER);
  });

  it('reports not-found for an unknown email and creates nothing', async () => {
    const users = [user()];
    const { store, update } = storeWith(users);

    const outcome = await setAdminRole(store, {
      email: 'missing@example.com',
      revoke: false,
      confirm: true,
    });

    expect(outcome).toEqual({ status: 'not-found' });
    expect(update).not.toHaveBeenCalled();
    expect(users).toHaveLength(1);
  });

  it('refuses a soft-deleted account', async () => {
    const users = [user({ deletedAt: new Date('2026-10-01T00:00:00Z') })];
    const { store, update, findFirst } = storeWith(users);

    const outcome = await setAdminRole(store, {
      email: 'person@example.com',
      revoke: false,
      confirm: true,
    });

    expect(outcome).toEqual({ status: 'not-found' });
    expect(update).not.toHaveBeenCalled();
    expect(findFirst.mock.calls[0][0].where).toMatchObject({ deletedAt: null });
  });

  it('matches the email case-insensitively', async () => {
    const { store, findFirst } = storeWith([user()]);

    const outcome = await setAdminRole(store, {
      email: 'Person@Example.COM',
      revoke: false,
      confirm: true,
    });

    expect(outcome.status).toBe('updated');
    expect(findFirst.mock.calls[0][0].where.email).toEqual({
      equals: 'Person@Example.COM',
      mode: 'insensitive',
    });
  });

  it('is a no-op for an account that is already ADMIN', async () => {
    const { store, update } = storeWith([user({ role: UserRole.ADMIN })]);

    const outcome = await setAdminRole(store, {
      email: 'person@example.com',
      revoke: false,
      confirm: true,
    });

    expect(outcome.status).toBe('unchanged');
    expect(update).not.toHaveBeenCalled();
  });

  it('revokes ADMIN back to USER with --revoke', async () => {
    const users = [user({ role: UserRole.ADMIN })];
    const { store } = storeWith(users);

    const outcome = await setAdminRole(store, {
      email: 'person@example.com',
      revoke: true,
      confirm: true,
    });

    expect(outcome).toMatchObject({ status: 'updated', targetRole: UserRole.USER });
    expect(users[0].role).toBe(UserRole.USER);
  });

  it('never selects the password hash', async () => {
    const { store, findFirst } = storeWith([user()]);

    await setAdminRole(store, { email: 'person@example.com', revoke: false, confirm: false });

    expect(findFirst.mock.calls[0][0].select).toEqual({ id: true, email: true, role: true });
  });
});

describe('promote-admin: output', () => {
  it('prints the database host and name but never the credentials', () => {
    const target = describeDatabaseTarget(
      'postgresql://lifeos:s3cret-pass@db.internal:5432/lifeos?schema=public',
    );

    expect(target).toBe('db.internal:5432/lifeos');
    expect(target).not.toContain('s3cret-pass');
    expect(target).not.toContain('lifeos:');
  });

  it('handles a missing or malformed DATABASE_URL', () => {
    expect(describeDatabaseTarget(undefined)).toBe('(DATABASE_URL is not set)');
    expect(describeDatabaseTarget('not a url')).toBe('(unparseable DATABASE_URL)');
  });

  it('describes each outcome', () => {
    const args = { email: 'person@example.com', revoke: false, confirm: false };
    const account = { id: 'u-1', email: 'person@example.com', role: UserRole.USER };

    expect(describeOutcome({ status: 'not-found' }, args)).toContain('No active account found');
    expect(describeOutcome({ status: 'unchanged', account }, args)).toContain('already USER');
    expect(
      describeOutcome({ status: 'dry-run', account, targetRole: UserRole.ADMIN }, args),
    ).toContain('DRY RUN');
    expect(
      describeOutcome({ status: 'updated', account, targetRole: UserRole.ADMIN }, args),
    ).toContain('USER -> ADMIN');
  });
});
