import { Injectable, Logger } from '@nestjs/common';
import { MailMessage, MailService } from '../mail.service';

/**
 * Development and test only: writes the email's text (including any reset
 * code) to the log instead of sending it, so the reset flow can be tried
 * without an email provider. `MailModule` never selects it in production.
 */
@Injectable()
export class LogMailService extends MailService {
  private readonly logger = new Logger('Mail');

  send(message: MailMessage): Promise<void> {
    this.logger.log(
      `[not sent: no BREVO_API_KEY] To: ${message.to.email} | ${message.subject}\n${message.text}`,
    );
    return Promise.resolve();
  }
}
