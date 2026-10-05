import { PrismaClient, UserRole } from '@prisma/client';

/**
 * Grants or revokes the ADMIN role for an EXISTING account — the only way
 * a role ever changes; no API request can do it.
 *
 *   npm run admin:promote -- user@example.com              # dry run
 *   npm run admin:promote -- user@example.com --confirm    # make ADMIN
 *   npm run admin:promote -- user@example.com --revoke --confirm
 *
 * Safety properties:
 * - Dry run by default: nothing is written without `--confirm`.
 * - Only changes `role` on an account that already exists and is not
 *   soft-deleted. It never creates an account and never touches a password.
 * - Reads `DATABASE_URL` from the environment and holds no secret itself.
 *   Output shows the database host/name (never credentials) so the
 *   operator can see which database is about to change.
 *
 * Because `RolesGuard` checks the database on every request, the change
 * takes effect immediately, for already-issued tokens too.
 */

export interface PromoteArgs {
  email: string;
  revoke: boolean;
  confirm: boolean;
}

export class UsageError extends Error {}

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const KNOWN_FLAGS = new Set(['--confirm', '--revoke']);

export const USAGE = 'Usage: npm run admin:promote -- <email> [--revoke] [--confirm]';

export function parseArgs(argv: string[]): PromoteArgs {
  const flags = argv.filter((arg) => arg.startsWith('--'));
  const positional = argv.filter((arg) => !arg.startsWith('--'));

  const unknown = flags.find((flag) => !KNOWN_FLAGS.has(flag));
  if (unknown) {
    throw new UsageError(`Unknown option: ${unknown}`);
  }
  if (positional.length !== 1) {
    throw new UsageError('Exactly one email address is required.');
  }
  const email = positional[0].trim();
  if (!EMAIL_PATTERN.test(email)) {
    throw new UsageError(`Not a valid email address: ${email}`);
  }

  return { email, revoke: flags.includes('--revoke'), confirm: flags.includes('--confirm') };
}

export interface AccountSummary {
  id: string;
  email: string;
  role: UserRole;
}

export type PromoteOutcome =
  | { status: 'not-found' }
  | { status: 'unchanged'; account: AccountSummary }
  | { status: 'dry-run'; account: AccountSummary; targetRole: UserRole }
  | { status: 'updated'; account: AccountSummary; targetRole: UserRole };

/** The slice of `PrismaClient` this tool needs — narrow so tests can supply a plain object. */
export type UserStore = Pick<PrismaClient, 'user'>;

export async function setAdminRole(prisma: UserStore, args: PromoteArgs): Promise<PromoteOutcome> {
  const targetRole = args.revoke ? UserRole.USER : UserRole.ADMIN;

  const user = await prisma.user.findFirst({
    where: { email: { equals: args.email, mode: 'insensitive' }, deletedAt: null },
    select: { id: true, email: true, role: true },
  });
  if (!user) {
    return { status: 'not-found' };
  }

  const account: AccountSummary = { id: user.id, email: user.email, role: user.role };
  if (user.role === targetRole) {
    return { status: 'unchanged', account };
  }
  if (!args.confirm) {
    return { status: 'dry-run', account, targetRole };
  }

  await prisma.user.update({ where: { id: user.id }, data: { role: targetRole } });
  return { status: 'updated', account, targetRole };
}

/** "host:port/database" of a connection string, with user and password stripped — safe to print. */
export function describeDatabaseTarget(databaseUrl: string | undefined): string {
  if (!databaseUrl) return '(DATABASE_URL is not set)';
  try {
    const url = new URL(databaseUrl);
    return `${url.host}${url.pathname}`;
  } catch {
    return '(unparseable DATABASE_URL)';
  }
}

export function describeOutcome(outcome: PromoteOutcome, args: PromoteArgs): string {
  switch (outcome.status) {
    case 'not-found':
      return `No active account found for ${args.email}. Nothing was changed.`;
    case 'unchanged':
      return `${outcome.account.email} (${outcome.account.id}) is already ${outcome.account.role}. Nothing was changed.`;
    case 'dry-run':
      return (
        `DRY RUN: ${outcome.account.email} (${outcome.account.id}) is ${outcome.account.role} ` +
        `and would become ${outcome.targetRole}. Re-run with --confirm to apply.`
      );
    case 'updated':
      return `${outcome.account.email} (${outcome.account.id}): ${outcome.account.role} -> ${outcome.targetRole}.`;
  }
}

async function main(): Promise<number> {
  let args: PromoteArgs;
  try {
    args = parseArgs(process.argv.slice(2));
  } catch (error) {
    if (error instanceof UsageError) {
      console.error(`${error.message}\n${USAGE}`);
      return 1;
    }
    throw error;
  }

  // Instantiating the client is what loads `.env`, so the target is read after it.
  const prisma = new PrismaClient();
  try {
    console.log(`Database: ${describeDatabaseTarget(process.env.DATABASE_URL)}`);
    const outcome = await setAdminRole(prisma, args);
    console.log(describeOutcome(outcome, args));
    return outcome.status === 'not-found' ? 1 : 0;
  } finally {
    await prisma.$disconnect();
  }
}

if (require.main === module) {
  main()
    .then((code) => {
      process.exitCode = code;
    })
    .catch((error) => {
      console.error(error instanceof Error ? error.message : error);
      process.exitCode = 1;
    });
}
