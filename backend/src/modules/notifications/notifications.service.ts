import { Injectable, Logger } from '@nestjs/common';
import { DevicePlatform } from '@prisma/client';
import { PushMessage, PushSendResult, PushSender } from './push/push-sender';
import { DeviceTokensRepository } from './repositories/device-tokens.repository';

/**
 * Push notification devices and sending (week 4).
 *
 * - A device registers its FCM token after sign-in and whenever the token
 *   changes; registering again just refreshes it (or moves it to the user
 *   now signed in on that device).
 * - Signing out unregisters the token. So do a password change and a
 *   password reset, for every device of the user: those sign every session
 *   out, and a signed-out device must not keep receiving the user's
 *   reminders. The device that changed the password registers again.
 * - Sending goes to all of a user's devices; tokens FCM reports as dead are
 *   deleted on the spot.
 */
@Injectable()
export class NotificationsService {
  private readonly logger = new Logger(NotificationsService.name);

  constructor(
    private readonly deviceTokens: DeviceTokensRepository,
    private readonly pushSender: PushSender,
  ) {}

  registerDevice(userId: string, token: string, platform: DevicePlatform): Promise<void> {
    return this.deviceTokens.upsert(userId, token, platform, new Date());
  }

  /** Forgets a device token, whoever it belongs to: the app may be signed out already (an expired session). */
  async unregisterDevice(token: string): Promise<void> {
    await this.deviceTokens.deleteTokens([token]);
  }

  async forgetAllDevices(userId: string): Promise<void> {
    const removed = await this.deviceTokens.deleteAllForUser(userId);
    if (removed > 0) {
      this.logger.log(`Forgot ${removed} push device(s) (user ${userId}).`);
    }
  }

  /** Sends to every device of the user. Never throws: a failed push must not break whatever sent it. */
  async sendToUser(userId: string, message: PushMessage): Promise<PushSendResult> {
    const tokens = await this.deviceTokens.findTokensForUser(userId);
    if (tokens.length === 0) return { sent: 0, invalidTokens: [], failed: 0 };

    try {
      const result = await this.pushSender.send(tokens, message);
      if (result.invalidTokens.length > 0) {
        await this.deviceTokens.deleteTokens(result.invalidTokens);
        this.logger.log(
          `Removed ${result.invalidTokens.length} expired push device(s) (user ${userId}).`,
        );
      }
      return result;
    } catch (error) {
      this.logger.error(
        `Push failed (user ${userId}): ${error instanceof Error ? error.message : 'unknown error'}`,
      );
      return { sent: 0, invalidTokens: [], failed: tokens.length };
    }
  }
}
