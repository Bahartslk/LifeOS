import { screen, waitFor } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { createFakeBackend, fakeUser } from '../test/fakeBackend';
import { currentLocation, renderApp, renderSignedIn, signIn } from '../test/renderApp';

function setup() {
  const admin = fakeUser({ email: 'root@example.com', role: 'ADMIN', displayName: 'Root Admin' });
  const regular = fakeUser({ email: 'user@example.com', displayName: 'Regular User' });
  return { admin, regular, backend: createFakeBackend([admin, regular]) };
}

describe('signing in', () => {
  it('confirms admin access with the backend, then opens the dashboard', async () => {
    const { backend } = setup();
    const { user } = renderApp(backend, '/login');

    await signIn(user, 'root@example.com');

    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/dashboard');
    expect(backend.callsTo('POST', '/auth/login')).toHaveLength(1);
    expect(backend.callsTo('GET', '/admin/session')).toHaveLength(1);
    expect(screen.getByText('Root Admin')).toBeInTheDocument();
    expect(screen.getByText('root@example.com · Yönetici')).toBeInTheDocument();
  });

  it('keeps the button disabled until both fields are filled', async () => {
    const { backend } = setup();
    const { user } = renderApp(backend, '/login');
    const submit = await screen.findByRole('button', { name: 'Giriş yap' });

    expect(submit).toBeDisabled();
    await user.type(screen.getByLabelText('E-posta'), 'root@example.com');
    expect(submit).toBeDisabled();
    await user.type(screen.getByLabelText('Şifre'), 'x');
    expect(submit).toBeEnabled();
  });

  it('shows progress and locks the form while the request is running', async () => {
    const { backend } = setup();
    const release = backend.hold('POST', '/auth/login');
    const { user } = renderApp(backend, '/login');

    await signIn(user, 'root@example.com');

    const submit = screen.getByRole('button', { name: 'Giriş yap' });
    expect(submit).toBeDisabled();
    expect(submit).toHaveAttribute('aria-busy', 'true');
    expect(screen.getByLabelText('E-posta')).toBeDisabled();
    expect(screen.getByLabelText('Şifre')).toBeDisabled();

    release();
    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
  });

  it('reports wrong credentials (401) without starting a session', async () => {
    const { backend } = setup();
    const { user, api } = renderApp(backend, '/login');

    await signIn(user, 'root@example.com', 'wrong-password');

    expect(await screen.findByRole('alert')).toHaveTextContent('E-posta veya şifre hatalı.');
    expect(currentLocation()).toBe('/login');
    expect(api.hasSession()).toBe(false);
    expect(backend.callsTo('GET', '/admin/session')).toHaveLength(0);
    expect(backend.callsTo('POST', '/auth/refresh')).toHaveLength(0);
    // The password is cleared; the email stays so it can be corrected.
    expect(screen.getByLabelText('Şifre')).toHaveValue('');
    expect(screen.getByLabelText('E-posta')).toHaveValue('root@example.com');
  });

  it('lets the admin try again after a failed attempt', async () => {
    const { backend } = setup();
    const { user } = renderApp(backend, '/login');
    await signIn(user, 'root@example.com', 'wrong-password');
    await screen.findByRole('alert');

    await user.type(screen.getByLabelText('Şifre'), 'correct-horse-battery');
    await user.click(screen.getByRole('button', { name: 'Giriş yap' }));

    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
  });

  it('explains a rate limit (429) in Turkish', async () => {
    const { backend } = setup();
    backend.fail('POST', '/auth/login', 429, 'ThrottlerException: Too Many Requests');
    const { user } = renderApp(backend, '/login');

    await signIn(user, 'root@example.com');

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Çok fazla giriş denemesi yapıldı. Lütfen bir dakika sonra tekrar deneyin.',
    );
    expect(screen.getByRole('alert')).not.toHaveTextContent('Throttler');
  });

  it('explains a network failure in Turkish', async () => {
    const { backend } = setup();
    backend.override('POST', '/auth/login', () => {
      throw new TypeError('Failed to fetch');
    });
    const { user, api } = renderApp(backend, '/login');

    await signIn(user, 'root@example.com');

    expect(await screen.findByRole('alert')).toHaveTextContent('Sunucuya ulaşılamadı.');
    expect(api.hasSession()).toBe(false);
  });

  it('keeps no session when admin access cannot be confirmed', async () => {
    const { backend } = setup();
    backend.fail('GET', '/admin/session', 500);
    const { user, api } = renderApp(backend, '/login');

    await signIn(user, 'root@example.com');

    expect(await screen.findByRole('alert')).toHaveTextContent('Sunucuda bir hata oluştu.');
    expect(currentLocation()).toBe('/login');
    expect(api.hasSession()).toBe(false);
    expect(backend.activeRefreshTokens()).toEqual([]);
  });
});

describe('an account that is not an admin', () => {
  it('is sent to "access denied" when the backend answers 403', async () => {
    const { backend } = setup();
    const { user } = renderApp(backend, '/login');

    await signIn(user, 'user@example.com');

    expect(await screen.findByRole('heading', { name: 'Yetkisiz erişim' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/access-denied');
    expect(backend.callsTo('GET', '/admin/session')).toHaveLength(1);
    // Nothing else of the admin API is ever requested for this account.
    expect(backend.callsTo('GET', '/admin/dashboard')).toHaveLength(0);
    expect(backend.callsTo('GET', '/admin/users')).toHaveLength(0);
  });

  it('has its session ended: refresh token revoked, tokens forgotten', async () => {
    const { backend } = setup();
    const { user, api } = renderApp(backend, '/login');

    await signIn(user, 'user@example.com');
    await screen.findByRole('heading', { name: 'Yetkisiz erişim' });

    await waitFor(() => expect(api.hasSession()).toBe(false));
    expect(backend.callsTo('POST', '/auth/logout')).toHaveLength(1);
    expect(backend.activeRefreshTokens()).toEqual([]);
  });

  it('is denied even when the login response claims the ADMIN role', async () => {
    // The role in the login response is a hint; only GET /admin/session decides.
    const { backend } = setup();
    backend.override('GET', '/admin/session', (call) =>
      backend.failure(403, 'FORBIDDEN', 'Forbidden resource', call.path),
    );
    const { user } = renderApp(backend, '/login');

    await signIn(user, 'root@example.com');

    expect(await screen.findByRole('heading', { name: 'Yetkisiz erişim' })).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Dashboard' })).not.toBeInTheDocument();
  });

  it('can return to the login screen and sign in with another account', async () => {
    const { backend } = setup();
    const { user } = renderApp(backend, '/login');
    await signIn(user, 'user@example.com');
    await screen.findByRole('heading', { name: 'Yetkisiz erişim' });

    await user.click(screen.getByRole('button', { name: 'Giriş ekranına dön' }));
    expect(currentLocation()).toBe('/login');
    await signIn(user, 'root@example.com');

    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
  });
});

describe('signing out', () => {
  it('revokes the refresh token, forgets the session and returns to the login screen', async () => {
    const { backend } = setup();
    const { user, api, tokenStore } = await renderSignedIn(backend, 'root@example.com');
    const refreshToken = tokenStore.get()?.refreshToken;

    await user.click(screen.getByRole('button', { name: 'Çıkış' }));

    expect(await screen.findByRole('button', { name: 'Giriş yap' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/login');
    expect(backend.callsTo('POST', '/auth/logout')[0]?.body).toEqual({ refreshToken });
    expect(backend.activeRefreshTokens()).toEqual([]);
    expect(api.hasSession()).toBe(false);
  });

  it('offers exactly one sign-out control', async () => {
    const { backend } = setup();
    await renderSignedIn(backend, 'root@example.com');

    expect(screen.getAllByRole('button', { name: /çıkış/i })).toHaveLength(1);
  });

  it('still signs out locally when the backend cannot be reached', async () => {
    const { backend } = setup();
    const { user, api } = await renderSignedIn(backend, 'root@example.com');
    backend.override('POST', '/auth/logout', () => {
      throw new TypeError('Failed to fetch');
    });

    await user.click(screen.getByRole('button', { name: 'Çıkış' }));

    expect(await screen.findByRole('button', { name: 'Giriş yap' })).toBeInTheDocument();
    expect(api.hasSession()).toBe(false);
  });
});
