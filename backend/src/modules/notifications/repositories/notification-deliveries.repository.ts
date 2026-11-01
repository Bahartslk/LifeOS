import { Injectable } from '@nestjs/common';
import { NotificationKind, Prisma } from '@prisma/client';
import { PrismaService } from '../../../prisma/prisma.service';

/** Sole data-access point for `notification_deliveries`: the scheduler's "sent at most once" record. */
@Injectable()
export class NotificationDeliveriesRepository {
  constructor(private readonly prisma: PrismaService) {}

  /**
   * Claims one notification occurrence. `true` if this call inserted the
   * row (so the caller sends), `false` if it already existed (another tick
   * or instance sent it). Relies on the unique `dedupe_key`.
   */
  async claim(data: {
    userId: string;
    kind: NotificationKind;
    dedupeKey: string;
    scheduledFor: Date;
  }): Promise<boolean> {
    try {
      await this.prisma.notificationDelivery.create({ data });
      return true;
    } catch (error) {
      if (error instanceof Prisma.PrismaClientKnownRequestError && error.code === 'P2002') {
        return false;
      }
      throw error;
    }
  }
}
