import { Injectable } from '@nestjs/common';
import { Prisma, ThemeMode, User } from '@prisma/client';
import { PrismaService } from '../../../prisma/prisma.service';

export interface CreateUserData {
  email: string;
  passwordHash: string;
  displayName: string;
}

export interface UpdateUserData {
  displayName?: string;
  avatarUrl?: string | null;
  bio?: string | null;
  timezone?: string;
  language?: string;
  themePreference?: ThemeMode;
  notificationPreferences?: Prisma.InputJsonValue;
}

/**
 * Sole data-access point for the `users` table — `UsersService` (and, via
 * it, `AuthService`) never calls `PrismaService` directly, per CLAUDE.md's
 * Repository Pattern rule. No separate `UsersRepository` interface: a
 * TypeScript interface has no runtime representation, so in NestJS's DI
 * this class *is* the abstraction seam (consumers depend on it, not on
 * Prisma) — an interface here would add ceremony without a second
 * implementation to justify it, per CLAUDE.md's "do not over-engineer"
 * rule.
 *
 * `update` is one generic method, not separate `updateProfile`/
 * `updatePreferences` repository methods — both of Sprint 17.0's PATCH
 * routes ultimately write to the same `users` row, just different field
 * subsets; `UsersService` decides which fields are allowed for which
 * route, this class only persists whatever it's given.
 */
@Injectable()
export class UsersRepository {
  constructor(private readonly prisma: PrismaService) {}

  /** Case-insensitive per the `users_email_lower_key` index — matches how uniqueness is enforced at the database level. */
  findByEmail(email: string): Promise<User | null> {
    return this.prisma.user.findFirst({
      where: { email: { equals: email, mode: 'insensitive' }, deletedAt: null },
    });
  }

  findById(id: string): Promise<User | null> {
    return this.prisma.user.findFirst({ where: { id, deletedAt: null } });
  }

  create(data: CreateUserData): Promise<User> {
    return this.prisma.user.create({ data });
  }

  async update(id: string, data: UpdateUserData): Promise<User | null> {
    const result = await this.prisma.user.updateMany({
      where: { id, deletedAt: null },
      data,
    });
    if (result.count === 0) return null;
    return this.findById(id);
  }
}
