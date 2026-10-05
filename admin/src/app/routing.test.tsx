import { screen, waitFor } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { createFakeBackend, fakeUser } from '../test/fakeBackend';
import { currentLocation, renderApp, renderSignedIn, signIn } from '../test/renderApp';

function setup() {
  const admin = fakeUser({ email: 'root@example.com', role: 'ADMIN', displayName: 'Root Admin' });
  const ada = fakeUser({ email: 'ada@example.com', displayName: 'Ada Lovelace' });
  return { admin, ada, backend: createFakeBackend([admin, ada]) };
}

describe('without a session', () => {
  it.each(['/dashboard', '/users', '/users/00000000-0000-4000-8000-000000000001', '/', '/nope'])(
    'redirects %s to the login screen without calling the admin API',
    async (route) => {
      const { backend } = setup();

      renderApp(backend, route);

      expect(await screen.findByRole('button', { name: 'Giriş yap' })).toBeInTheDocument();
      expect(currentLocation()).toBe('/login');
      expect(backend.calls).toHaveLength(0);
    },
  );

  it('starts every page load signed out, because tokens live in memory only', async () => {
    const { backend } = setup();
    const first = await renderSignedIn(backend, 'root@example.com');
    expect(first.api.hasSession()).toBe(true);

    // A reload is a new page: a new app with a new, empty token store.
    const reloaded = renderApp(backend, '/dashboard');

    expect(reloaded.api.hasSession()).toBe(false);
    expect(reloaded.tokenStore.get()).toBeNull();
  });
});

describe('with an admin session', () => {
  it('opens the dashboard after login from /', async () => {
    const { backend } = setup();

    await renderSignedIn(backend, 'root@example.com', '/');

    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/dashboard');
  });

  it('opens the dashboard for an unknown address', async () => {
    const { backend } = setup();

    await renderSignedIn(backend, 'root@example.com', '/no/such/page');

    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/dashboard');
  });

  it('continues to the page that was asked for before login, filters included', async () => {
    const { backend } = setup();

    await renderSignedIn(backend, 'root@example.com', '/users?role=ADMIN');

    expect(await screen.findByRole('heading', { name: 'Kullanıcılar' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/users?role=ADMIN');
  });

  it('moves between the protected pages through the sidebar', async () => {
    const { backend } = setup();
    const { user } = await renderSignedIn(backend, 'root@example.com');

    await user.click(screen.getByRole('link', { name: 'Kullanıcılar' }));
    expect(await screen.findByRole('heading', { name: 'Kullanıcılar' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/users');

    await user.click(screen.getByRole('link', { name: 'Dashboard' }));
    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/dashboard');
  });

  it('does not show the login form again while signed in', async () => {
    const { backend } = setup();
    const { go } = await renderSignedIn(backend, 'root@example.com');
    await screen.findByRole('heading', { name: 'Dashboard' });

    // Login replaced its own history entry, so "back" has nowhere to return to.
    await go(-1);

    expect(screen.queryByRole('button', { name: 'Giriş yap' })).not.toBeInTheDocument();
    expect(currentLocation()).toBe('/dashboard');
  });
});

describe('when the session changes underneath the page', () => {
  it('renews an expired access token once and stays on the page', async () => {
    const { backend } = setup();
    const { user } = await renderSignedIn(backend, 'root@example.com');
    await screen.findByText('Toplam kullanıcı');
    backend.expireAccessTokens();

    await user.click(screen.getByRole('link', { name: 'Kullanıcılar' }));

    expect(await screen.findByText('ada@example.com')).toBeInTheDocument();
    expect(currentLocation()).toBe('/users');
    expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(1);
  });

  it('returns to the login screen when the session can no longer be renewed (401)', async () => {
    const { backend } = setup();
    const { user, api } = await renderSignedIn(backend, 'root@example.com');
    await screen.findByText('Toplam kullanıcı');
    backend.expireAccessTokens();
    backend.revokeRefreshTokens();

    await user.click(screen.getByRole('link', { name: 'Kullanıcılar' }));

    expect(await screen.findByRole('button', { name: 'Giriş yap' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/login');
    expect(api.hasSession()).toBe(false);
    expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(1);
  });

  it('shows "access denied" when the admin role is revoked mid-session (403)', async () => {
    const { backend, admin } = setup();
    const { user, api } = await renderSignedIn(backend, 'root@example.com');
    await screen.findByText('Toplam kullanıcı');
    admin.role = 'USER';

    await user.click(screen.getByRole('link', { name: 'Kullanıcılar' }));

    expect(await screen.findByRole('heading', { name: 'Yetkisiz erişim' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/access-denied');
    // A 403 is an answer, not an expired token: no refresh is attempted.
    expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(0);
    await waitFor(() => expect(api.hasSession()).toBe(false));
    expect(backend.activeRefreshTokens()).toEqual([]);
  });

  it('keeps a denied account out of every protected page', async () => {
    const { backend } = setup();
    const { user, go } = renderApp(backend, '/users');
    await signIn(user, 'ada@example.com');
    await screen.findByRole('heading', { name: 'Yetkisiz erişim' });

    await go(-1);

    expect(screen.queryByRole('heading', { name: 'Kullanıcılar' })).not.toBeInTheDocument();
    expect(backend.callsTo('GET', '/admin/users')).toHaveLength(0);
  });
});
