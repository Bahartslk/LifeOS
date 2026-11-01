import { Module } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../config/configuration';
import { PlannerModule } from '../planner/planner.module';
import { UsersModule } from '../users/users.module';
import { NotificationsController } from './notifications.controller';
import { NotificationsService } from './notifications.service';
import { DisabledPushSender } from './push/disabled-push-sender';
import { FcmPushSender } from './push/fcm-push-sender';
import { LogPushSender } from './push/log-push-sender';
import { PushSender } from './push/push-sender';
import { parseServiceAccount } from './push/service-account';
import { ReminderScheduler } from './reminders/reminder.scheduler';
import { ReminderService } from './reminders/reminder.service';
import { DeviceTokensRepository } from './repositories/device-tokens.repository';
import { NotificationDeliveriesRepository } from './repositories/notification-deliveries.repository';

/**
 * Chooses the `PushSender` once, at startup: FCM with a Firebase key,
 * otherwise log-only outside production and disabled in it. A key that is
 * set but malformed stops the app, so a broken configuration is noticed
 * at deploy time (Railway then keeps the previous deployment running).
 */
export function createPushSender(config: ConfigService<AppConfig, true>): PushSender {
  const raw = config.get('notifications', { infer: true }).firebaseServiceAccountJson;
  if (raw) {
    return new FcmPushSender(parseServiceAccount(raw));
  }
  return config.get('nodeEnv', { infer: true }) === 'production'
    ? new DisabledPushSender()
    : new LogPushSender();
}

/**
 * Push notifications (week 4): device registration, sending through
 * `NotificationsService.sendToUser`, and the minute-by-minute
 * `ReminderScheduler` (task/meeting reminders, the 08:00 summary). Reads
 * tasks through `PlannerService` and time zones/preferences through
 * `UsersService`; neither module depends back on this one.
 */
@Module({
  imports: [PlannerModule, UsersModule],
  controllers: [NotificationsController],
  providers: [
    DeviceTokensRepository,
    NotificationDeliveriesRepository,
    NotificationsService,
    ReminderService,
    ReminderScheduler,
    { provide: PushSender, useFactory: createPushSender, inject: [ConfigService] },
  ],
  exports: [NotificationsService],
})
export class NotificationsModule {}
