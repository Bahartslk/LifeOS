import { Module } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../config/configuration';
import { MailService } from './mail.service';
import { BrevoMailService } from './providers/brevo-mail.service';
import { DisabledMailService } from './providers/disabled-mail.service';
import { LogMailService } from './providers/log-mail.service';

/** Chooses the `MailService` implementation once, at startup (see `MailService`'s doc comment). */
export function createMailService(config: ConfigService<AppConfig, true>): MailService {
  const mail = config.get('mail', { infer: true });
  if (mail.brevoApiKey && mail.fromEmail) {
    return new BrevoMailService({
      apiKey: mail.brevoApiKey,
      fromEmail: mail.fromEmail,
      fromName: mail.fromName,
    });
  }
  return config.get('nodeEnv', { infer: true }) === 'production'
    ? new DisabledMailService()
    : new LogMailService();
}

@Module({
  providers: [{ provide: MailService, useFactory: createMailService, inject: [ConfigService] }],
  exports: [MailService],
})
export class MailModule {}
