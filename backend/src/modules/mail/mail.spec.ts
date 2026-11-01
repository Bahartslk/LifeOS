import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../config/configuration';
import { createMailService } from './mail.module';
import { BREVO_SEND_URL, BrevoMailService } from './providers/brevo-mail.service';
import { DisabledMailService } from './providers/disabled-mail.service';
import { LogMailService } from './providers/log-mail.service';
import { buildPasswordResetEmail } from './templates/password-reset.template';

function configWith(
  nodeEnv: string,
  mail: Partial<AppConfig['mail']>,
): ConfigService<AppConfig, true> {
  const values: Record<string, unknown> = { nodeEnv, mail: { fromName: 'LifeOS', ...mail } };
  return { get: (key: string) => values[key] } as unknown as ConfigService<AppConfig, true>;
}

describe('createMailService', () => {
  it('uses Brevo when a key and a sender are configured', () => {
    const service = createMailService(
      configWith('production', { brevoApiKey: 'key', fromEmail: 'noreply@example.com' }),
    );
    expect(service).toBeInstanceOf(BrevoMailService);
  });

  it('only logs outside production without a key', () => {
    expect(createMailService(configWith('development', {}))).toBeInstanceOf(LogMailService);
  });

  it('never logs the content in production without a key', () => {
    expect(createMailService(configWith('production', {}))).toBeInstanceOf(DisabledMailService);
  });
});

describe('BrevoMailService', () => {
  const message = buildPasswordResetEmail({
    email: 'ada@example.com',
    displayName: 'Ada',
    code: '042817',
    validMinutes: 15,
  });

  it('posts the email to Brevo with the key in the api-key header', async () => {
    const fetchFn = jest.fn().mockResolvedValue(new Response('{}', { status: 201 }));
    const service = new BrevoMailService(
      { apiKey: 'test-key', fromEmail: 'noreply@example.com', fromName: 'LifeOS' },
      fetchFn,
    );

    await service.send(message);

    const [url, init] = fetchFn.mock.calls[0];
    expect(url).toBe(BREVO_SEND_URL);
    expect(init.method).toBe('POST');
    expect(init.headers['api-key']).toBe('test-key');
    expect(JSON.parse(init.body)).toEqual({
      sender: { email: 'noreply@example.com', name: 'LifeOS' },
      to: [{ email: 'ada@example.com', name: 'Ada' }],
      subject: message.subject,
      textContent: message.text,
      htmlContent: message.html,
    });
  });

  it('rejects with only the status when Brevo refuses, never the key', async () => {
    const fetchFn = jest
      .fn()
      .mockResolvedValue(new Response('{"message":"bad key"}', { status: 401 }));
    const service = new BrevoMailService(
      { apiKey: 'secret-key', fromEmail: 'noreply@example.com', fromName: 'LifeOS' },
      fetchFn,
    );

    const error = await service.send(message).catch((e: Error) => e);

    expect((error as Error).message).toBe('Brevo rejected the email with status 401.');
    expect((error as Error).message).not.toContain('secret-key');
  });
});

describe('buildPasswordResetEmail', () => {
  it('puts the code and its lifetime in both versions and escapes the name in HTML', () => {
    const message = buildPasswordResetEmail({
      email: 'ada@example.com',
      displayName: '<b>Ada</b>',
      code: '042817',
      validMinutes: 15,
    });

    expect(message.text).toContain('042817');
    expect(message.text).toContain('15 dakika');
    expect(message.html).toContain('042817');
    expect(message.html).toContain('&lt;b&gt;Ada&lt;/b&gt;');
    expect(message.html).not.toContain('<b>Ada</b>');
  });
});
