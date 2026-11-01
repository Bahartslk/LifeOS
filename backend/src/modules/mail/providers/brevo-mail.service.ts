import { MailMessage, MailService } from '../mail.service';

export const BREVO_SEND_URL = 'https://api.brevo.com/v3/smtp/email';
const REQUEST_TIMEOUT_MS = 10_000;

export interface BrevoSettings {
  apiKey: string;
  fromEmail: string;
  fromName: string;
}

/**
 * Sends through Brevo's transactional email HTTPS API (Railway's plan blocks
 * outbound SMTP). The sender must be verified in the Brevo account. Errors
 * carry only the HTTP status, never the key or the message body. Built by
 * `createMailService`, not by Nest's injector.
 */
export class BrevoMailService extends MailService {
  constructor(
    private readonly settings: BrevoSettings,
    private readonly fetchFn: typeof fetch = fetch,
  ) {
    super();
  }

  async send(message: MailMessage): Promise<void> {
    const response = await this.fetchFn(BREVO_SEND_URL, {
      method: 'POST',
      headers: {
        'api-key': this.settings.apiKey,
        'content-type': 'application/json',
        accept: 'application/json',
      },
      body: JSON.stringify({
        sender: { email: this.settings.fromEmail, name: this.settings.fromName },
        to: [message.to.name ? message.to : { email: message.to.email }],
        subject: message.subject,
        textContent: message.text,
        htmlContent: message.html,
      }),
      signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
    });
    if (!response.ok) {
      throw new Error(`Brevo rejected the email with status ${response.status}.`);
    }
  }
}
