import { Module } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../config/configuration';
import { NotificationsController } from './notifications.controller';
import { NotificationsService } from './notifications.service';
import { DisabledPushSender } from './push/disabled-push-sender';
import { FcmPushSender } from './push/fcm-push-sender';
import { LogPushSender } from './push/log-push-sender';
import { PushSender } from './push/push-sender';
import { parseServiceAccount } from './push/service-account';
import { DeviceTokensRepository } from './repositories/device-tokens.repository';

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
 * Push notifications (week 4): device registration, and sending through
 * `NotificationsService.sendToUser`, which the reminder scheduler and the
 * auth flows (forgetting devices) use.
 */
@Module({
  controllers: [NotificationsController],
  providers: [
    DeviceTokensRepository,
    NotificationsService,
    { provide: PushSender, useFactory: createPushSender, inject: [ConfigService] },
  ],
  exports: [NotificationsService],
})
export class NotificationsModule {}
