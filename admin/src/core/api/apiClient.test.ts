import { describe, expect, it, vi } from 'vitest';
import { createFakeBackend, fakeUser } from '../../test/fakeBackend';
import { API_URL } from '../../test/renderApp';
import { createApiClient } from './apiClient';
import { ApiError, NetworkError } from './errors';
import { createMemoryTokenStore } from './tokenStore';

function setup() {
  const admin = fakeUser({ email: 'root@example.com', role: 'ADMIN', displayName: 'Root' });
  const regular = fakeUser({ email: 'user@example.com' });
  const backend = createFakeBackend([admin, regular]);
  const tokenStore = createMemoryTokenStore();
  const api = createApiClient({ baseUrl: API_URL, tokenStore, fetch: backend.fetch });
  const onSessionExpired = vi.fn();
  const onForbidden = vi.fn();
  api.setHandlers({ onSessionExpired, onForbidden });
  return { admin, regular, backend, tokenStore, api, onSessionExpired, onForbidden };
}

describe('apiClient', () => {
  describe('requests and envelopes', () => {
    it('unwraps the { data } envelope of a single resource', async () => {
      const { api, admin } = setup();
      await api.login('root@example.com', 'correct-horse-battery');

      expect(await api.get('/admin/session')).toEqual({
        id: admin.id,
        email: 'root@example.com',
        displayName: 'Root',
        role: 'ADMIN',
      });
    });

    it('returns data and meta of a paginated list', async () => {
      const { api } = setup();
      await api.login('root@example.com', 'correct-horse-battery');

      const page = await api.getPage<{ email: string }>('/admin/users', { limit: 1 });

      expect(page.data).toHaveLength(1);
      expect(page.meta).toEqual({ nextCursor: expect.any(String), limit: 1, hasMore: true });
    });

    it('sends the access token and omits empty query values', async () => {
      const { api, backend, tokenStore } = setup();
      await api.login('root@example.com', 'correct-horse-battery');

      await api.getPage('/admin/users', {
        q: undefined,
        role: '',
        cursor: null,
        status: 'all',
        limit: 20,
      });

      const [call] = backend.callsTo('GET', '/admin/users');
      expect(call?.query).toEqual({ status: 'all', limit: '20' });
      expect(call?.authorization).toBe(`Bearer ${tokenStore.get()?.accessToken}`);
    });

    it('never sends cookies and never caches', async () => {
      const fetchSpy = vi.fn<typeof fetch>(
        async () => new Response('{"data":{}}', { status: 200 }),
      );
      const api = createApiClient({
        baseUrl: API_URL,
        tokenStore: createMemoryTokenStore(),
        fetch: fetchSpy,
      });

      await api.get('/admin/session');

      expect(fetchSpy.mock.calls[0]?.[1]).toMatchObject({ credentials: 'omit', cache: 'no-store' });
    });
  });

  describe('login and logout', () => {
    it('keeps the tokens of a successful login and returns the user', async () => {
      const { api, tokenStore } = setup();

      const user = await api.login('root@example.com', 'correct-horse-battery');

      expect(user).toMatchObject({ email: 'root@example.com', role: 'ADMIN' });
      expect(tokenStore.get()).toEqual({
        accessToken: expect.any(String),
        refreshToken: expect.any(String),
      });
      expect(api.hasSession()).toBe(true);
    });

    it('rejects wrong credentials without a session, and without trying to refresh', async () => {
      const { api, backend, tokenStore, onSessionExpired } = setup();

      await expect(api.login('root@example.com', 'wrong')).rejects.toMatchObject({ status: 401 });

      expect(tokenStore.get()).toBeNull();
      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(0);
      expect(onSessionExpired).not.toHaveBeenCalled();
    });

    it('revokes the refresh token on logout and forgets both tokens', async () => {
      const { api, backend, tokenStore } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      const refreshToken = tokenStore.get()?.refreshToken;

      await api.logout();

      expect(backend.callsTo('POST', '/auth/logout')[0]?.body).toEqual({ refreshToken });
      expect(backend.activeRefreshTokens()).toEqual([]);
      expect(tokenStore.get()).toBeNull();
    });

    it('forgets the tokens even when the logout request fails', async () => {
      const { api, backend, tokenStore, onSessionExpired, onForbidden } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      backend.override('POST', '/auth/logout', () => {
        throw new TypeError('offline');
      });

      await expect(api.logout()).resolves.toBeUndefined();

      expect(tokenStore.get()).toBeNull();
      expect(onSessionExpired).not.toHaveBeenCalled();
      expect(onForbidden).not.toHaveBeenCalled();
    });

    it('renews an expired access token first, then revokes the NEW refresh token', async () => {
      const { api, backend, tokenStore } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      backend.expireAccessTokens();

      await api.logout();

      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(1);
      expect(backend.activeRefreshTokens()).toEqual([]);
      expect(tokenStore.get()).toBeNull();
    });

    it('does nothing on logout without a session', async () => {
      const { api, backend } = setup();

      await api.logout();

      expect(backend.calls).toHaveLength(0);
    });
  });

  describe('session renewal', () => {
    it('refreshes once on 401 and retries the request with the new token', async () => {
      const { api, backend, tokenStore, onSessionExpired } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      const before = tokenStore.get();
      backend.expireAccessTokens();

      const session = await api.get<{ email: string }>('/admin/session');

      expect(session.email).toBe('root@example.com');
      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(1);
      expect(backend.callsTo('POST', '/auth/refresh')[0]?.authorization).toBeNull();
      expect(backend.callsTo('GET', '/admin/session')).toHaveLength(2);
      expect(tokenStore.get()?.accessToken).not.toBe(before?.accessToken);
      expect(tokenStore.get()?.refreshToken).not.toBe(before?.refreshToken);
      expect(onSessionExpired).not.toHaveBeenCalled();
    });

    it('makes a single refresh call for many requests that fail at the same time', async () => {
      const { api, backend, onSessionExpired } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      backend.expireAccessTokens();

      const results = await Promise.all([
        api.get('/admin/session'),
        api.get('/admin/dashboard'),
        api.getPage('/admin/users'),
        api.get('/admin/session'),
      ]);

      expect(results).toHaveLength(4);
      // A second refresh with the same token would be treated as theft and revoke the session.
      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(1);
      expect(backend.activeRefreshTokens()).toHaveLength(1);
      expect(onSessionExpired).not.toHaveBeenCalled();
    });

    it('does not refresh again when another request already renewed the session', async () => {
      const { api, backend, tokenStore } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      const staleToken = tokenStore.get()?.accessToken;
      backend.expireAccessTokens();
      await api.get('/admin/session');
      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(1);

      // A request that was sent with the old token and only now comes back as 401.
      let first = true;
      backend.override('GET', '/admin/dashboard', (call) => {
        if (!first) return undefined;
        first = false;
        expect(call.authorization).not.toBe(`Bearer ${staleToken}`);
        return undefined;
      });
      await api.get('/admin/dashboard');

      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(1);
    });

    it('ends the session when the refresh token is rejected', async () => {
      const { api, backend, tokenStore, onSessionExpired } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      backend.expireAccessTokens();
      backend.revokeRefreshTokens();

      await expect(api.get('/admin/session')).rejects.toMatchObject({ status: 401 });

      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(1);
      expect(tokenStore.get()).toBeNull();
      expect(onSessionExpired).toHaveBeenCalledTimes(1);
    });

    it('ends the session when the refresh request cannot be sent', async () => {
      const { api, backend, tokenStore, onSessionExpired } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      backend.expireAccessTokens();
      backend.override('POST', '/auth/refresh', () => {
        throw new TypeError('offline');
      });

      await expect(api.get('/admin/session')).rejects.toBeInstanceOf(ApiError);

      expect(tokenStore.get()).toBeNull();
      expect(onSessionExpired).toHaveBeenCalledTimes(1);
    });

    it('gives up after one retry instead of looping', async () => {
      const { api, backend, onSessionExpired } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      backend.fail('GET', '/admin/session', 401, 'Unauthorized');

      await expect(api.get('/admin/session')).rejects.toMatchObject({ status: 401 });

      expect(backend.callsTo('GET', '/admin/session')).toHaveLength(2);
      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(1);
      expect(onSessionExpired).toHaveBeenCalledTimes(1);
    });

    it('reports 401 without a refresh call when there is no session at all', async () => {
      const { api, backend, onSessionExpired } = setup();

      await expect(api.get('/admin/session')).rejects.toMatchObject({ status: 401 });

      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(0);
      expect(onSessionExpired).toHaveBeenCalledTimes(1);
    });
  });

  describe('authorization and errors', () => {
    it('never refreshes or retries on 403, and reports it', async () => {
      const { api, backend, tokenStore, onForbidden, onSessionExpired } = setup();
      await api.login('user@example.com', 'correct-horse-battery');

      await expect(api.get('/admin/session')).rejects.toMatchObject({
        status: 403,
        code: 'FORBIDDEN',
      });

      expect(backend.callsTo('GET', '/admin/session')).toHaveLength(1);
      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(0);
      expect(onForbidden).toHaveBeenCalledTimes(1);
      expect(onSessionExpired).not.toHaveBeenCalled();
      // Ending the session is the caller's decision; the client only reports.
      expect(tokenStore.get()).not.toBeNull();
    });

    it('joins the messages of a validation error', async () => {
      const { api } = setup();
      await api.login('root@example.com', 'correct-horse-battery');

      const error = await api
        .getPage('/admin/users', { status: 'banned', sort: 'name' })
        .catch((caught: unknown) => caught);

      expect(error).toBeInstanceOf(ApiError);
      expect((error as ApiError).status).toBe(400);
      expect((error as ApiError).message).toContain('status must be one of');
      expect((error as ApiError).message).toContain('sort must be one of');
    });

    it.each([404, 429, 500])('turns a %i response into an ApiError', async (status) => {
      const { api, backend } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      backend.fail('GET', '/admin/dashboard', status, 'Nope');

      await expect(api.get('/admin/dashboard')).rejects.toMatchObject({ status, message: 'Nope' });
      expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(0);
    });

    it('survives an error response that is not JSON', async () => {
      const { api, backend } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      backend.override(
        'GET',
        '/admin/dashboard',
        () => new Response('<html>502</html>', { status: 502 }),
      );

      await expect(api.get('/admin/dashboard')).rejects.toMatchObject({ status: 502 });
    });

    it('reports a request that never got a response as a NetworkError', async () => {
      const { api, backend } = setup();
      await api.login('root@example.com', 'correct-horse-battery');
      backend.override('GET', '/admin/dashboard', () => {
        throw new TypeError('Failed to fetch');
      });

      await expect(api.get('/admin/dashboard')).rejects.toBeInstanceOf(NetworkError);
    });
  });

  describe('token storage', () => {
    it('never writes anything to browser storage or cookies', async () => {
      const setItem = vi.spyOn(Storage.prototype, 'setItem');
      const { api, backend } = setup();

      await api.login('root@example.com', 'correct-horse-battery');
      backend.expireAccessTokens();
      await api.get('/admin/session');
      await api.logout();

      expect(setItem).not.toHaveBeenCalled();
      // eslint-disable-next-line no-restricted-globals -- asserting that storage stays empty
      expect(localStorage.length + sessionStorage.length).toBe(0);
      expect(document.cookie).toBe('');
    });

    it('keeps each token store separate, so a new page starts signed out', async () => {
      const { api } = setup();
      await api.login('root@example.com', 'correct-horse-battery');

      expect(createMemoryTokenStore().get()).toBeNull();
    });
  });
});
