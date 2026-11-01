import { createSign } from 'crypto';
import { Logger } from '@nestjs/common';
import { PushMessage, PushSendResult, PushSender } from './push-sender';
import { ServiceAccount } from './service-account';

export const GOOGLE_TOKEN_URL = 'https://oauth2.googleapis.com/token';
const FCM_SCOPE = 'https://www.googleapis.com/auth/firebase.messaging';
const REQUEST_TIMEOUT_MS = 10_000;
/** Renew the access token this long before Google says it expires. */
const TOKEN_RENEW_MARGIN_MS = 60_000;

const base64Url = (value: string | Buffer): string => Buffer.from(value).toString('base64url');

/**
 * Sends through Firebase Cloud Messaging's HTTP v1 API with plain `fetch`
 * (no Firebase SDK): a short-lived OAuth access token is obtained by signing
 * a JWT with the service account's private key (cached until shortly
 * before it expires), then each device gets one `messages:send` call.
 *
 * A device whose token FCM reports as unregistered or malformed comes back
 * in `invalidTokens`; any other failure is only counted. Logs never contain
 * tokens, keys or the message text.
 */
export class FcmPushSender extends PushSender {
  private readonly logger = new Logger('Push');
  private accessToken: { value: string; expiresAt: number } | null = null;

  constructor(
    private readonly account: ServiceAccount,
    private readonly fetchFn: typeof fetch = fetch,
    private readonly now: () => number = Date.now,
  ) {
    super();
  }

  async send(tokens: string[], message: PushMessage): Promise<PushSendResult> {
    const result: PushSendResult = { sent: 0, invalidTokens: [], failed: 0 };
    if (tokens.length === 0) return result;

    const accessToken = await this.getAccessToken();
    for (const token of tokens) {
      const outcome = await this.sendOne(accessToken, token, message);
      if (outcome === 'sent') result.sent++;
      else if (outcome === 'invalid') result.invalidTokens.push(token);
      else result.failed++;
    }
    if (result.failed > 0) {
      this.logger.warn(`FCM: ${result.failed} of ${tokens.length} push(es) failed.`);
    }
    return result;
  }

  private async sendOne(
    accessToken: string,
    token: string,
    message: PushMessage,
  ): Promise<'sent' | 'invalid' | 'failed'> {
    let response: Response;
    try {
      response = await this.fetchFn(
        `https://fcm.googleapis.com/v1/projects/${this.account.projectId}/messages:send`,
        {
          method: 'POST',
          headers: {
            authorization: `Bearer ${accessToken}`,
            'content-type': 'application/json',
          },
          body: JSON.stringify({
            message: {
              token,
              notification: { title: message.title, body: message.body },
              data: message.data ?? {},
              android: { priority: 'HIGH', notification: { channel_id: message.channelId } },
            },
          }),
          signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
        },
      );
    } catch {
      return 'failed';
    }
    if (response.ok) return 'sent';
    return (await this.isInvalidToken(response)) ? 'invalid' : 'failed';
  }

  /** FCM's answers that mean "this device token is dead": 404 UNREGISTERED, or a 400 about the token. */
  private async isInvalidToken(response: Response): Promise<boolean> {
    if (response.status === 404) return true;
    if (response.status !== 400) return false;
    const body = (await response.json().catch(() => null)) as {
      error?: { message?: string; details?: { errorCode?: string }[] };
    } | null;
    const codes = body?.error?.details?.map((detail) => detail.errorCode) ?? [];
    return codes.includes('UNREGISTERED') || /registration token/i.test(body?.error?.message ?? '');
  }

  private async getAccessToken(): Promise<string> {
    if (this.accessToken && this.accessToken.expiresAt - TOKEN_RENEW_MARGIN_MS > this.now()) {
      return this.accessToken.value;
    }
    const response = await this.fetchFn(GOOGLE_TOKEN_URL, {
      method: 'POST',
      headers: { 'content-type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        assertion: this.signedAssertion(),
      }).toString(),
      signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
    });
    if (!response.ok) {
      throw new Error(
        `Google rejected the FCM access token request with status ${response.status}.`,
      );
    }
    const body = (await response.json()) as { access_token: string; expires_in: number };
    this.accessToken = {
      value: body.access_token,
      expiresAt: this.now() + body.expires_in * 1000,
    };
    return body.access_token;
  }

  /** The service account's signed JWT that Google exchanges for an access token (RS256). */
  private signedAssertion(): string {
    const issuedAt = Math.floor(this.now() / 1000);
    const header = base64Url(JSON.stringify({ alg: 'RS256', typ: 'JWT' }));
    const claims = base64Url(
      JSON.stringify({
        iss: this.account.clientEmail,
        scope: FCM_SCOPE,
        aud: GOOGLE_TOKEN_URL,
        iat: issuedAt,
        exp: issuedAt + 3600,
      }),
    );
    const signature = createSign('RSA-SHA256')
      .update(`${header}.${claims}`)
      .sign(this.account.privateKey);
    return `${header}.${claims}.${base64Url(signature)}`;
  }
}
