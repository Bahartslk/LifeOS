import { Injectable } from '@nestjs/common';
import { RefreshToken } from '@prisma/client';
import { PrismaService } from '../../../prisma/prisma.service';

export interface CreateRefreshTokenData {
  userId: string;
  tokenHash: string;
  familyId: string;
  expiresAt: Date;
}

/**
 * Sole data-access point for `refresh_tokens` — owned by `AuthModule`
 * (unlike `UsersRepository`, no separate service sits in front of this: the
 * rotation/family-revocation *logic* lives in `AuthService`, this class is
 * pure CRUD, so an intermediate `RefreshTokensService` would just forward
 * calls with no behavior of its own — skipped per CLAUDE.md's "avoid
 * premature abstractions" rule).
 */
@Injectable()
export class RefreshTokensRepository {
  constructor(private readonly prisma: PrismaService) {}

  create(data: CreateRefreshTokenData): Promise<RefreshToken> {
    return this.prisma.refreshToken.create({ data });
  }

  findByTokenHash(tokenHash: string): Promise<RefreshToken | null> {
    return this.prisma.refreshToken.findFirst({ where: { tokenHash } });
  }

  async revokeById(id: string): Promise<void> {
    await this.prisma.refreshToken.update({
      where: { id },
      data: { revokedAt: new Date() },
    });
  }

  /** Revokes every still-active token in a family — the response to detecting reuse of an already-rotated-out token (a theft/replay signal). */
  async revokeFamily(familyId: string): Promise<void> {
    await this.prisma.refreshToken.updateMany({
      where: { familyId, revokedAt: null },
      data: { revokedAt: new Date() },
    });
  }
}
