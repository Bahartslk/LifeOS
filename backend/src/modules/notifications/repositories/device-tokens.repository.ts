import { Injectable } from '@nestjs/common';
import { DevicePlatform } from '@prisma/client';
import { PrismaService } from '../../../prisma/prisma.service';

/** Sole data-access point for `device_tokens`; the rules live in `NotificationsService`. */
@Injectable()
export class DeviceTokensRepository {
  constructor(private readonly prisma: PrismaService) {}

  /** Saves the token for `userId`, moving it from another user if it was registered before (one device, one owner). */
  async upsert(userId: string, token: string, platform: DevicePlatform, now: Date): Promise<void> {
    await this.prisma.deviceToken.upsert({
      where: { token },
      create: { userId, token, platform, lastSeenAt: now },
      update: { userId, platform, lastSeenAt: now },
    });
  }

  /** Everyone with at least one registered device: the only users the scheduler needs to look at. */
  async findUserIdsWithDevices(): Promise<string[]> {
    const rows = await this.prisma.deviceToken.findMany({
      distinct: ['userId'],
      select: { userId: true },
    });
    return rows.map((row) => row.userId);
  }

  async findTokensForUser(userId: string): Promise<string[]> {
    const rows = await this.prisma.deviceToken.findMany({
      where: { userId },
      select: { token: true },
    });
    return rows.map((row) => row.token);
  }

  async deleteTokens(tokens: string[]): Promise<number> {
    if (tokens.length === 0) return 0;
    const result = await this.prisma.deviceToken.deleteMany({ where: { token: { in: tokens } } });
    return result.count;
  }

  async deleteAllForUser(userId: string): Promise<number> {
    const result = await this.prisma.deviceToken.deleteMany({ where: { userId } });
    return result.count;
  }
}
