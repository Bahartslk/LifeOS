import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../../prisma/prisma.service';

/**
 * The per-user daily chat counter (`ai_chat_usage`). Both writes are single
 * SQL statements, so concurrent requests can never push a user past the
 * limit: the row is locked by the upsert, and the `WHERE` makes the update a
 * no-op once the limit is reached.
 */
@Injectable()
export class ChatUsageRepository {
  constructor(private readonly prisma: PrismaService) {}

  /**
   * Takes one message from the user's allowance for [day] (`YYYY-MM-DD`).
   * Returns how many messages are now used that day, or `null` when the
   * limit was already reached (nothing changes then).
   */
  async reserve(userId: string, day: string, dailyLimit: number): Promise<number | null> {
    const rows = await this.prisma.$queryRaw<{ message_count: number }[]>`
      INSERT INTO ai_chat_usage (id, user_id, usage_date, message_count, created_at, updated_at)
      VALUES (gen_random_uuid(), ${userId}::uuid, ${day}::date, 1, now(), now())
      ON CONFLICT (user_id, usage_date) DO UPDATE
        SET message_count = ai_chat_usage.message_count + 1, updated_at = now()
        WHERE ai_chat_usage.message_count < ${dailyLimit}
      RETURNING message_count`;
    return rows.length > 0 ? Number(rows[0].message_count) : null;
  }

  /** Gives a reserved message back, e.g. when the AI call failed and the user got no reply. */
  async release(userId: string, day: string): Promise<void> {
    await this.prisma.$executeRaw`
      UPDATE ai_chat_usage
      SET message_count = message_count - 1, updated_at = now()
      WHERE user_id = ${userId}::uuid AND usage_date = ${day}::date AND message_count > 0`;
  }
}
