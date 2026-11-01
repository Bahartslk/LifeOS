import { Injectable, Logger } from '@nestjs/common';
import { NotificationKind } from '@prisma/client';
import { dateInTimeZone, zonedDateTimeToUtc } from '../../../common/utils/date-time.util';
import { PlannerService } from '../../planner/planner.service';
import { NotificationTarget, UsersService } from '../../users/users.service';
import { NotificationsService } from '../notifications.service';
import { DeviceTokensRepository } from '../repositories/device-tokens.repository';
import { NotificationDeliveriesRepository } from '../repositories/notification-deliveries.repository';
import { buildDailySummary, buildTaskReminder } from './reminder-messages';

/** A notification still goes out if its moment passed less than this long ago (restarts, deploys, a slow tick). Older ones are skipped, never sent late. */
export const CATCH_UP_WINDOW_MS = 10 * 60_000;
/** The morning summary's local time (product decision: 08:00 for everyone, in their own time zone). */
export const DAILY_SUMMARY_TIME = '08:00';
const DAY_MS = 24 * 60 * 60_000;

/**
 * Decides which pushes are due at `now` and sends them, once per minute
 * (see `ReminderScheduler`). Only users with a registered device are looked
 * at. Every push is claimed in `notification_deliveries` before it is sent,
 * so it goes out at most once, even across restarts or two instances.
 *
 * - Task and meeting reminders: unfinished tasks with a time and
 *   `reminderMinutesBefore`; fire at (due time in the user's time zone)
 *   minus the lead. Needs `taskReminders` on.
 * - Morning summary: 08:00 in the user's time zone, if there is at least one
 *   unfinished task that day. Needs `dailySummary` on.
 */
@Injectable()
export class ReminderService {
  private readonly logger = new Logger(ReminderService.name);

  constructor(
    private readonly deviceTokens: DeviceTokensRepository,
    private readonly deliveries: NotificationDeliveriesRepository,
    private readonly users: UsersService,
    private readonly planner: PlannerService,
    private readonly notifications: NotificationsService,
  ) {}

  async runOnce(now: Date): Promise<{ reminders: number; summaries: number }> {
    const userIds = await this.deviceTokens.findUserIdsWithDevices();
    if (userIds.length === 0) return { reminders: 0, summaries: 0 };
    const targets = await this.users.findNotificationTargets(userIds);

    const reminders = await this.sendDueTaskReminders(
      targets.filter((target) => target.taskReminders),
      now,
    );
    const summaries = await this.sendDailySummaries(
      targets.filter((target) => target.dailySummary),
      now,
    );
    if (reminders + summaries > 0) {
      this.logger.log(`Sent ${reminders} reminder(s) and ${summaries} morning summary(ies).`);
    }
    return { reminders, summaries };
  }

  private async sendDueTaskReminders(targets: NotificationTarget[], now: Date): Promise<number> {
    if (targets.length === 0) return 0;
    const zoneByUser = new Map(targets.map((target) => [target.userId, target.timezone]));
    // Wide enough for every time zone (UTC-12…+14) and a lead of up to a day.
    const candidates = await this.planner.findReminderCandidates(
      [...zoneByUser.keys()],
      new Date(now.getTime() - 2 * DAY_MS).toISOString().slice(0, 10),
      new Date(now.getTime() + 3 * DAY_MS).toISOString().slice(0, 10),
    );

    let sent = 0;
    for (const task of candidates) {
      const timezone = zoneByUser.get(task.userId);
      if (!timezone || !task.dueTime || task.reminderMinutesBefore === null) continue;
      const startsAt = zonedDateTimeToUtc(task.dueDate, task.dueTime, timezone);
      const fireAt = new Date(startsAt.getTime() - task.reminderMinutesBefore * 60_000);
      if (!isDue(fireAt, now)) continue;

      const claimed = await this.deliveries.claim({
        userId: task.userId,
        kind: NotificationKind.TASK_REMINDER,
        dedupeKey: `TASK_REMINDER:${task.id}:${fireAt.toISOString()}`,
        scheduledFor: fireAt,
      });
      if (!claimed) continue;
      await this.notifications.sendToUser(task.userId, buildTaskReminder(task));
      sent++;
    }
    return sent;
  }

  private async sendDailySummaries(targets: NotificationTarget[], now: Date): Promise<number> {
    let sent = 0;
    for (const target of targets) {
      const localDate = dateInTimeZone(now, target.timezone);
      const fireAt = zonedDateTimeToUtc(localDate, DAILY_SUMMARY_TIME, target.timezone);
      if (!isDue(fireAt, now)) continue;

      const tasks = await this.planner.findUnfinishedDueOn(target.userId, localDate);
      if (tasks.length === 0) continue;
      const claimed = await this.deliveries.claim({
        userId: target.userId,
        kind: NotificationKind.DAILY_SUMMARY,
        dedupeKey: `DAILY_SUMMARY:${target.userId}:${localDate}`,
        scheduledFor: fireAt,
      });
      if (!claimed) continue;
      await this.notifications.sendToUser(target.userId, buildDailySummary(tasks, localDate));
      sent++;
    }
    return sent;
  }
}

/** Its moment has come, and not so long ago that a push would be stale. */
function isDue(fireAt: Date, now: Date): boolean {
  const age = now.getTime() - fireAt.getTime();
  return age >= 0 && age < CATCH_UP_WINDOW_MS;
}
