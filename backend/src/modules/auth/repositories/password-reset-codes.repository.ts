import { Injectable } from '@nestjs/common';
import { PasswordResetCode } from '@prisma/client';
import { PrismaService } from '../../../prisma/prisma.service';

/** Sole data-access point for `password_reset_codes`; the rules live in `PasswordResetService`. */
@Injectable()
export class PasswordResetCodesRepository {
  constructor(private readonly prisma: PrismaService) {}

  create(data: { userId: string; codeHash: string; expiresAt: Date }): Promise<PasswordResetCode> {
    return this.prisma.passwordResetCode.create({ data });
  }

  /** The newest code that can still be used: not used, not expired, attempts left. */
  findUsable(userId: string, maxAttempts: number, now: Date): Promise<PasswordResetCode | null> {
    return this.prisma.passwordResetCode.findFirst({
      where: { userId, usedAt: null, expiresAt: { gt: now }, attempts: { lt: maxAttempts } },
      orderBy: { createdAt: 'desc' },
    });
  }

  /** Counts one wrong guess; returns the new attempt count. */
  async recordFailedAttempt(id: string): Promise<number> {
    const updated = await this.prisma.passwordResetCode.update({
      where: { id },
      data: { attempts: { increment: 1 } },
    });
    return updated.attempts;
  }

  /** Closes every open code of the user (a new code was issued, or the password was reset). */
  async closeAllForUser(userId: string, now: Date): Promise<void> {
    await this.prisma.passwordResetCode.updateMany({
      where: { userId, usedAt: null },
      data: { usedAt: now },
    });
  }
}
