export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface TokenStore {
  get(): AuthTokens | null;
  set(tokens: AuthTokens): void;
  clear(): void;
}

/**
 * Holds the session's tokens in a closure variable — in memory ONLY.
 *
 * Nothing is ever written to `localStorage`, `sessionStorage`, a cookie or
 * IndexedDB, so a token cannot outlive the page and there is no persisted
 * copy for an injected script or another tab to read later. The price is
 * that reloading the page signs the admin out, which is accepted for this
 * panel (docs/12-project-architecture.md#admin-web-client).
 */
export function createMemoryTokenStore(): TokenStore {
  let tokens: AuthTokens | null = null;
  return {
    get: () => tokens,
    set: (next) => {
      tokens = next;
    },
    clear: () => {
      tokens = null;
    },
  };
}
