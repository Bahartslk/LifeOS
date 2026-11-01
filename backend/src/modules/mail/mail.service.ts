/** One email: plain text for every client, HTML for the ones that show it. */
export interface MailMessage {
  to: { email: string; name?: string };
  subject: string;
  text: string;
  html: string;
}

/**
 * Sends email. An abstract class (not an interface) so it can be the Nest
 * injection token; `MailModule` picks the implementation from the config:
 * `BrevoMailService` when `BREVO_API_KEY` is set, otherwise
 * `LogMailService` outside production and `DisabledMailService` in it.
 * Callers never know which one they got.
 */
export abstract class MailService {
  /** Rejects if the message could not be handed to the provider. */
  abstract send(message: MailMessage): Promise<void>;
}
