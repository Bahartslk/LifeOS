/** One push notification. `data` travels to the app (e.g. which task to open); values must be strings (FCM rule). */
export interface PushMessage {
  title: string;
  body: string;
  /** Android notification channel the app created for this kind of message (e.g. `reminders`). */
  channelId: string;
  data?: Record<string, string>;
}

export interface PushSendResult {
  /** Tokens FCM accepted. */
  sent: number;
  /** Tokens FCM says no longer exist (app uninstalled, token rotated); the caller deletes them. */
  invalidTokens: string[];
  /** Tokens that failed for another reason (network, FCM outage); kept for the next try. */
  failed: number;
}

/**
 * Sends push notifications. An abstract class (not an interface) so it can
 * be the Nest injection token; `NotificationsModule` picks the
 * implementation from the config, like `MailService`: `FcmPushSender` with a
 * Firebase service account key, otherwise `LogPushSender` outside production
 * and `DisabledPushSender` in it. Implementations never log tokens.
 */
export abstract class PushSender {
  abstract send(tokens: string[], message: PushMessage): Promise<PushSendResult>;
}
