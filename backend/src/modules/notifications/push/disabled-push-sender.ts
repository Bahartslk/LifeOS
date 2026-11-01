import { Logger } from '@nestjs/common';
import { PushMessage, PushSendResult, PushSender } from './push-sender';

/**
 * Production without a Firebase key: nothing is sent, and the content (a
 * task title, the day's summary) never reaches the log. Only a warning is
 * written; whatever triggered the push carries on.
 */
export class DisabledPushSender extends PushSender {
  private readonly logger = new Logger('Push');

  send(tokens: string[], message: PushMessage): Promise<PushSendResult> {
    this.logger.warn(
      `Push not sent (FIREBASE_SERVICE_ACCOUNT_JSON is not set): ${tokens.length} device(s), channel ${message.channelId}.`,
    );
    return Promise.resolve({ sent: 0, invalidTokens: [], failed: 0 });
  }
}
