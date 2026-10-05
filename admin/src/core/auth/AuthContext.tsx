import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import type { ApiClient } from '../api/apiClient';
import { isApiError } from '../api/errors';

/** `GET /admin/session`: the calling admin, as the backend sees it right now. */
export interface AdminSession {
  id: string;
  email: string;
  displayName: string;
  role: string;
}

/**
 * - `anonymous`: no session; only the login page is reachable.
 * - `admin`: the backend confirmed admin access for this session.
 * - `denied`: the backend answered 403 — signed in, but not an admin.
 */
export type AuthStatus = 'anonymous' | 'admin' | 'denied';

export type LoginResult = 'admin' | 'denied';

interface AuthContextValue {
  status: AuthStatus;
  admin: AdminSession | null;
  api: ApiClient;
  login(email: string, password: string): Promise<LoginResult>;
  logout(): Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

/**
 * Session state for the panel.
 *
 * The backend is the security boundary: every admin route checks the
 * caller's role in the database on every request. Nothing here grants
 * access — this only decides which screen to show. In particular the
 * `role` in the login response is never trusted: after signing in, admin
 * access is confirmed by calling `GET /admin/session`, and whatever the
 * backend answers (200 or 403) is what the UI follows. If the role is
 * revoked later, the next request's 403 moves the UI to "access denied".
 */
export function AuthProvider({ api, children }: { api: ApiClient; children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>('anonymous');
  const [admin, setAdmin] = useState<AdminSession | null>(null);

  const deny = useCallback(() => {
    setAdmin(null);
    setStatus('denied');
    // The account may not use the panel: revoke its refresh token and drop the session.
    void api.logout();
  }, [api]);

  useEffect(() => {
    api.setHandlers({
      onSessionExpired: () => {
        setAdmin(null);
        setStatus('anonymous');
      },
      onForbidden: deny,
    });
    return () => api.setHandlers({});
  }, [api, deny]);

  const login = useCallback(
    async (email: string, password: string): Promise<LoginResult> => {
      await api.login(email, password);
      try {
        const session = await api.get<AdminSession>('/admin/session');
        setAdmin(session);
        setStatus('admin');
        return 'admin';
      } catch (error) {
        if (isApiError(error, 403)) {
          // `onForbidden` has already switched to "denied" and ended the session.
          return 'denied';
        }
        // Admin access could not be confirmed, so no session is kept.
        await api.logout();
        throw error;
      }
    },
    [api],
  );

  const logout = useCallback(async () => {
    await api.logout();
    setAdmin(null);
    setStatus('anonymous');
  }, [api]);

  const value = useMemo(
    () => ({ status, admin, api, login, logout }),
    [status, admin, api, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error('useAuth must be used inside <AuthProvider>.');
  }
  return value;
}

/** The API client of the current session, for a feature's data functions. */
export function useApi(): ApiClient {
  return useAuth().api;
}
