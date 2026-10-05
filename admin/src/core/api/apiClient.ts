import { ApiError, InvalidResponseError, NetworkError } from './errors';
import type { AuthTokens, TokenStore } from './tokenStore';

export type QueryValue = string | number | undefined | null;
export type Query = Record<string, QueryValue>;

/** `meta` of a cursor-paginated list, per docs/15-api-design.md#pagination. There is no total count. */
export interface PageMeta {
  nextCursor: string | null;
  limit: number;
  hasMore: boolean;
}

export interface Page<T> {
  data: T[];
  meta: PageMeta;
}

/** `user` of the login response. `role` is a hint only — see `AuthProvider`. */
export interface LoginUser {
  id: string;
  email: string;
  displayName: string;
  role: string;
}

export interface ApiClientHandlers {
  /** The session could not be renewed; tokens are already cleared. */
  onSessionExpired?: () => void;
  /** The backend refused an authenticated request with 403. */
  onForbidden?: () => void;
}

export interface ApiClientOptions {
  /** Backend origin plus API prefix, e.g. `http://localhost:3000/api/v1`. */
  baseUrl: string;
  tokenStore: TokenStore;
  fetch?: typeof fetch;
}

export interface ApiClient {
  /** GET a single resource; returns the envelope's `data`. */
  get<T>(path: string, query?: Query): Promise<T>;
  /** GET a paginated list; returns `{ data, meta }` as sent. */
  getPage<T>(path: string, query?: Query): Promise<Page<T>>;
  login(email: string, password: string): Promise<LoginUser>;
  /** Revokes the refresh token on the backend, then forgets both tokens — also when the request fails. */
  logout(): Promise<void>;
  hasSession(): boolean;
  setHandlers(handlers: ApiClientHandlers): void;
}

interface RequestOptions {
  method: 'GET' | 'POST';
  path: string;
  query?: Query;
  /** A function is evaluated on every attempt, so a retry after a refresh sends current values. */
  body?: unknown | (() => unknown);
  /** Send the access token and renew the session on 401. False for login/refresh themselves. */
  authenticated: boolean;
}

interface ErrorBody {
  message?: string | string[];
  error?: string;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function buildUrl(baseUrl: string, path: string, query?: Query): string {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query ?? {})) {
    if (value !== undefined && value !== null && value !== '') {
      params.set(key, String(value));
    }
  }
  const search = params.toString();
  return `${baseUrl}${path}${search ? `?${search}` : ''}`;
}

async function parseJson(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) return undefined;
  try {
    return JSON.parse(text);
  } catch {
    return undefined;
  }
}

function toApiError(status: number, body: unknown): ApiError {
  const { message, error } = (body ?? {}) as ErrorBody;
  const text = Array.isArray(message) ? message.join(' ') : message;
  return new ApiError(status, text || `Request failed with status ${status}`, error);
}

/**
 * The one place this app talks HTTP. Responsibilities:
 * - attach the access token and unwrap the `{ data }` / `{ data, meta }`
 *   response envelope;
 * - on 401, renew the session ONCE and retry the request once;
 * - never retry or renew on 403 — that is an authorization answer, and
 *   only the backend decides it.
 *
 * Refresh tokens rotate and the backend treats a reused one as theft,
 * revoking the whole session (auth.service.ts). So renewals are
 * single-flight: however many requests fail with 401 at the same moment,
 * exactly one refresh call is made and all of them wait for it.
 */
export function createApiClient(options: ApiClientOptions): ApiClient {
  const { baseUrl, tokenStore } = options;
  const doFetch: typeof fetch = options.fetch ?? ((input, init) => fetch(input, init));
  let handlers: ApiClientHandlers = {};
  let refreshInFlight: Promise<boolean> | null = null;

  async function send(request: RequestOptions, accessToken: string | undefined): Promise<Response> {
    const body =
      typeof request.body === 'function' ? (request.body as () => unknown)() : request.body;
    try {
      return await doFetch(buildUrl(baseUrl, request.path, request.query), {
        method: request.method,
        headers: {
          Accept: 'application/json',
          ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
          ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        },
        body: body === undefined ? undefined : JSON.stringify(body),
        // Tokens travel in the Authorization header; no cookies are involved.
        credentials: 'omit',
        cache: 'no-store',
      });
    } catch {
      throw new NetworkError();
    }
  }

  async function renewSession(): Promise<boolean> {
    const current = tokenStore.get();
    if (!current) return false;
    try {
      const response = await send(
        {
          method: 'POST',
          path: '/auth/refresh',
          body: { refreshToken: current.refreshToken },
          authenticated: false,
        },
        undefined,
      );
      if (!response.ok) return false;
      const { data } = (await parseJson(response)) as { data: AuthTokens };
      tokenStore.set({ accessToken: data.accessToken, refreshToken: data.refreshToken });
      return true;
    } catch {
      return false;
    }
  }

  function renewSessionOnce(): Promise<boolean> {
    refreshInFlight ??= renewSession().finally(() => {
      refreshInFlight = null;
    });
    return refreshInFlight;
  }

  function expireSession(): void {
    tokenStore.clear();
    handlers.onSessionExpired?.();
  }

  async function request(options: RequestOptions): Promise<unknown> {
    const usedToken = options.authenticated ? tokenStore.get()?.accessToken : undefined;
    let response = await send(options, usedToken);

    if (response.status === 401 && options.authenticated) {
      // Another request may already have renewed the session while this one
      // was in the air; then the stored token is newer and only a retry is needed.
      const alreadyRenewed =
        tokenStore.get() !== null && tokenStore.get()?.accessToken !== usedToken;
      const renewed = alreadyRenewed || (await renewSessionOnce());
      if (!renewed) {
        expireSession();
        throw toApiError(401, await parseJson(response));
      }
      response = await send(options, tokenStore.get()?.accessToken);
      if (response.status === 401) {
        expireSession();
        throw toApiError(401, await parseJson(response));
      }
    }

    const body = await parseJson(response);
    if (!response.ok) {
      if (response.status === 403 && options.authenticated) {
        handlers.onForbidden?.();
      }
      throw toApiError(response.status, body);
    }
    return body;
  }

  return {
    async get<T>(path: string, query?: Query): Promise<T> {
      const body = await request({ method: 'GET', path, query, authenticated: true });
      if (!isRecord(body) || !isRecord(body.data)) {
        throw new InvalidResponseError();
      }
      return body.data as T;
    },

    async getPage<T>(path: string, query?: Query): Promise<Page<T>> {
      const body = await request({ method: 'GET', path, query, authenticated: true });
      if (!isRecord(body) || !Array.isArray(body.data) || !isRecord(body.meta)) {
        throw new InvalidResponseError();
      }
      return { data: body.data as T[], meta: body.meta as unknown as PageMeta };
    },

    async login(email: string, password: string): Promise<LoginUser> {
      const body = (await request({
        method: 'POST',
        path: '/auth/login',
        body: { email, password },
        authenticated: false,
      })) as { data: AuthTokens & { user: LoginUser } };
      tokenStore.set({ accessToken: body.data.accessToken, refreshToken: body.data.refreshToken });
      const { id, email: userEmail, displayName, role } = body.data.user;
      return { id, email: userEmail, displayName, role };
    },

    async logout(): Promise<void> {
      if (!tokenStore.get()) return;
      // Signing out must never bounce through the "session expired" or
      // "forbidden" flows, whatever the backend answers.
      const active = handlers;
      handlers = {};
      try {
        await request({
          method: 'POST',
          path: '/auth/logout',
          body: () => ({ refreshToken: tokenStore.get()?.refreshToken }),
          authenticated: true,
        });
      } catch {
        // Best effort: the local session ends regardless.
      } finally {
        handlers = active;
        tokenStore.clear();
      }
    },

    hasSession: () => tokenStore.get() !== null,

    setHandlers(next: ApiClientHandlers): void {
      handlers = next;
    },
  };
}
