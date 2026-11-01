import { createPublicKey, createVerify, generateKeyPairSync } from 'crypto';
import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../../config/configuration';
import { createPushSender } from '../notifications.module';
import { DisabledPushSender } from './disabled-push-sender';
import { FcmPushSender, GOOGLE_TOKEN_URL } from './fcm-push-sender';
import { LogPushSender } from './log-push-sender';
import { PushMessage } from './push-sender';
import { parseServiceAccount } from './service-account';

const keys = generateKeyPairSync('rsa', {
  modulusLength: 2048,
  publicKeyEncoding: { type: 'spki', format: 'pem' },
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' },
});

const ACCOUNT_JSON = JSON.stringify({
  type: 'service_account',
  project_id: 'lifeos-test',
  client_email: 'fcm@lifeos-test.iam.gserviceaccount.com',
  private_key: keys.privateKey,
});

const MESSAGE: PushMessage = {
  title: 'Toplantı 10 dakika sonra',
  body: 'Sprint planlama · 10:00',
  channelId: 'reminders',
  data: { taskId: 'task-1' },
};

function configWith(nodeEnv: string, json?: string): ConfigService<AppConfig, true> {
  const values: Record<string, unknown> = {
    nodeEnv,
    notifications: { firebaseServiceAccountJson: json },
  };
  return { get: (key: string) => values[key] } as unknown as ConfigService<AppConfig, true>;
}

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { 'content-type': 'application/json' } });

describe('parseServiceAccount', () => {
  it('reads the key file JSON as-is or base64-encoded', () => {
    const expected = {
      projectId: 'lifeos-test',
      clientEmail: 'fcm@lifeos-test.iam.gserviceaccount.com',
      privateKey: keys.privateKey,
    };
    expect(parseServiceAccount(ACCOUNT_JSON)).toEqual(expected);
    expect(parseServiceAccount(Buffer.from(ACCOUNT_JSON).toString('base64'))).toEqual(expected);
  });

  it('turns an escaped \\n in the private key back into line breaks', () => {
    const flattened = ACCOUNT_JSON.replace(/\\n/g, '\\\\n');

    expect(parseServiceAccount(flattened).privateKey).toBe(keys.privateKey);
  });

  it('names the problem without echoing the value', () => {
    expect(() => parseServiceAccount('not json at all')).toThrow(
      'FIREBASE_SERVICE_ACCOUNT_JSON is not valid JSON (or base64-encoded JSON).',
    );
    expect(() => parseServiceAccount('{"project_id":"x","client_email":"y"}')).toThrow(
      'FIREBASE_SERVICE_ACCOUNT_JSON has no "private_key".',
    );
  });
});

describe('createPushSender', () => {
  it('uses FCM when a key is configured', () => {
    expect(createPushSender(configWith('production', ACCOUNT_JSON))).toBeInstanceOf(FcmPushSender);
  });

  it('only logs outside production without a key', () => {
    expect(createPushSender(configWith('development'))).toBeInstanceOf(LogPushSender);
  });

  it('skips sending in production without a key', () => {
    expect(createPushSender(configWith('production'))).toBeInstanceOf(DisabledPushSender);
  });

  it('stops startup on a malformed key', () => {
    expect(() => createPushSender(configWith('production', '{broken'))).toThrow(
      /FIREBASE_SERVICE_ACCOUNT_JSON/,
    );
  });
});

describe('FcmPushSender', () => {
  let now: number;
  let fetchFn: jest.Mock;
  let sender: FcmPushSender;

  beforeEach(() => {
    now = 1_800_000_000_000;
    fetchFn = jest.fn(async (url: string, init: RequestInit) => {
      if (url === GOOGLE_TOKEN_URL) return json({ access_token: 'access-1', expires_in: 3600 });
      const token = (JSON.parse(init.body as string) as { message: { token: string } }).message
        .token;
      if (token === 'dead') return json({ error: { status: 'NOT_FOUND' } }, 404);
      if (token === 'flaky') return json({ error: { status: 'UNAVAILABLE' } }, 503);
      return json({ name: 'projects/lifeos-test/messages/1' });
    });
    sender = new FcmPushSender(parseServiceAccount(ACCOUNT_JSON), fetchFn, () => now);
  });

  it('gets an access token with a JWT signed by the service account key', async () => {
    await sender.send(['good'], MESSAGE);

    const [url, init] = fetchFn.mock.calls[0];
    expect(url).toBe(GOOGLE_TOKEN_URL);
    const form = new URLSearchParams(init.body as string);
    expect(form.get('grant_type')).toBe('urn:ietf:params:oauth:grant-type:jwt-bearer');
    const [header, claims, signature] = form.get('assertion')!.split('.');
    const verified = createVerify('RSA-SHA256')
      .update(`${header}.${claims}`)
      .verify(createPublicKey(keys.publicKey), Buffer.from(signature, 'base64url'));
    expect(verified).toBe(true);
    expect(JSON.parse(Buffer.from(claims, 'base64url').toString())).toEqual({
      iss: 'fcm@lifeos-test.iam.gserviceaccount.com',
      scope: 'https://www.googleapis.com/auth/firebase.messaging',
      aud: GOOGLE_TOKEN_URL,
      iat: now / 1000,
      exp: now / 1000 + 3600,
    });
  });

  it('sends one FCM v1 message per device with the title, body, channel and data', async () => {
    await sender.send(['good'], MESSAGE);

    const [url, init] = fetchFn.mock.calls[1];
    expect(url).toBe('https://fcm.googleapis.com/v1/projects/lifeos-test/messages:send');
    expect((init.headers as Record<string, string>).authorization).toBe('Bearer access-1');
    expect(JSON.parse(init.body as string)).toEqual({
      message: {
        token: 'good',
        notification: { title: MESSAGE.title, body: MESSAGE.body },
        data: { taskId: 'task-1' },
        android: { priority: 'HIGH', notification: { channel_id: 'reminders' } },
      },
    });
  });

  it('separates delivered, dead and failed devices', async () => {
    const result = await sender.send(['good', 'dead', 'flaky'], MESSAGE);

    expect(result).toEqual({ sent: 1, invalidTokens: ['dead'], failed: 1 });
  });

  it('treats a 400 about the registration token as a dead device, other 400s as failures', async () => {
    fetchFn.mockImplementation(async (url: string, init: RequestInit) => {
      if (url === GOOGLE_TOKEN_URL) return json({ access_token: 'access-1', expires_in: 3600 });
      const token = (JSON.parse(init.body as string) as { message: { token: string } }).message
        .token;
      return token === 'malformed'
        ? json(
            { error: { message: 'The registration token is not a valid FCM registration token' } },
            400,
          )
        : json({ error: { message: 'Invalid JSON payload received.' } }, 400);
    });

    const result = await sender.send(['malformed', 'payload-bug'], MESSAGE);

    expect(result).toEqual({ sent: 0, invalidTokens: ['malformed'], failed: 1 });
  });

  it('reuses the access token until shortly before it expires', async () => {
    await sender.send(['good'], MESSAGE);
    now += 30 * 60_000;
    await sender.send(['good'], MESSAGE);
    expect(fetchFn.mock.calls.filter(([url]) => url === GOOGLE_TOKEN_URL)).toHaveLength(1);

    now += 30 * 60_000;
    await sender.send(['good'], MESSAGE);
    expect(fetchFn.mock.calls.filter(([url]) => url === GOOGLE_TOKEN_URL)).toHaveLength(2);
  });

  it('fails with only the status when Google refuses the key', async () => {
    fetchFn.mockResolvedValueOnce(json({ error: 'invalid_grant' }, 400));

    await expect(sender.send(['good'], MESSAGE)).rejects.toThrow(
      'Google rejected the FCM access token request with status 400.',
    );
  });

  it('makes no call at all for a user without devices', async () => {
    expect(await sender.send([], MESSAGE)).toEqual({ sent: 0, invalidTokens: [], failed: 0 });
    expect(fetchFn).not.toHaveBeenCalled();
  });
});
