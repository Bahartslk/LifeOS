import { Injectable, Logger } from '@nestjs/common';
import { MailMessage, MailService } from '../mail.service';

/**
 * Production without `BREVO_API_KEY`: nothing can be sent, and the content
 * (a reset code) must never reach the log. Only a warning is written, and
 * the request that triggered it still succeeds.
 */
@Injectable()
export class DisabledMailService extends MailService {
  private readonly logger = new Logger('Mail');

  send(message: MailMessage): Promise<void> {
    this.logger.warn(`Email not sent (BREVO_API_KEY is not set): "${message.subject}".`);
    return Promise.resolve();
  }
}
