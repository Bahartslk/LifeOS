import { Logger } from '@nestjs/common';
import { PushMessage, PushSendResult, PushSender } from './push-sender';

/**
 * Development and test only: writes the notification to the log instead of
 * sending it, so reminders can be followed without Firebase.
 * `NotificationsModule` never selects it in production.
 */
export class LogPushSender extends PushSender {
  private readonly logger = new Logger('Push');

  send(tokens: string[], message: PushMessage): Promise<PushSendResult> {
    this.logger.log(
      `[not sent: no FIREBASE_SERVICE_ACCOUNT_JSON] to ${tokens.length} device(s) | ${message.title} | ${message.body} | channel ${message.channelId} | data ${JSON.stringify(message.data ?? {})}`,
    );
    return Promise.resolve({ sent: 0, invalidTokens: [], failed: 0 });
  }
}
