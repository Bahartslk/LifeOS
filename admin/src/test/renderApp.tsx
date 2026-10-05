import { act, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { MemoryRouter, useLocation, useNavigate } from 'react-router-dom';
import type { NavigateFunction } from 'react-router-dom';
import { App } from '../app/App';
import { createApiClient } from '../core/api/apiClient';
import { createMemoryTokenStore } from '../core/api/tokenStore';
import type { FakeBackend } from './fakeBackend';

export const API_URL = 'http://api.test/api/v1';

/** Shows the current URL and hands the router's `navigate` to the test (for back/forward). */
function RouterProbe({ onReady }: { onReady: (navigate: NavigateFunction) => void }) {
  const location = useLocation();
  const navigate = useNavigate();
  useEffect(() => onReady(navigate), [navigate, onReady]);
  return <span data-testid="location">{location.pathname + location.search}</span>;
}

/** Renders the whole app — router, auth, real API client — against the fake backend. */
export function renderApp(backend: FakeBackend, route = '/') {
  const tokenStore = createMemoryTokenStore();
  const api = createApiClient({ baseUrl: API_URL, tokenStore, fetch: backend.fetch });
  const user = userEvent.setup();
  let navigate: NavigateFunction | undefined;
  render(
    <MemoryRouter initialEntries={[route]}>
      <App api={api} />
      <RouterProbe
        onReady={(ready) => {
          navigate = ready;
        }}
      />
    </MemoryRouter>,
  );
  /** Moves through the history stack like the browser's back (-1) and forward (1) buttons. */
  const go = (delta: number) => act(() => void navigate?.(delta));
  return { user, api, tokenStore, go };
}

export function currentLocation(): string {
  return screen.getByTestId('location').textContent ?? '';
}

/** Fills in and submits the login form. */
export async function signIn(
  user: ReturnType<typeof userEvent.setup>,
  email: string,
  password = 'correct-horse-battery',
) {
  await user.type(await screen.findByLabelText('E-posta'), email);
  await user.type(screen.getByLabelText('Şifre'), password);
  await user.click(screen.getByRole('button', { name: 'Giriş yap' }));
}

/** Renders the app and signs in; the app then continues to `route`. */
export async function renderSignedIn(backend: FakeBackend, email: string, route = '/dashboard') {
  const context = renderApp(backend, route);
  await signIn(context.user, email);
  await screen.findByText('LifeOS Admin');
  return context;
}

/** The value shown on the stat card labelled `label` inside the section named `section`. */
export function statValue(section: string, label: string): string {
  const region = screen.getByRole('region', { name: section });
  const card = within(region)
    .getAllByText(label)
    .map((element) => element.closest('.stat-card'))
    .find((element) => element !== null);
  if (!card) throw new Error(`No stat card "${label}" in section "${section}".`);
  return card.querySelector('.stat-card__value')?.textContent ?? '';
}

/** Everything the page currently shows, as text — for "this must never appear" checks. */
export function visibleText(): string {
  return document.body.textContent ?? '';
}

/** Names of fields and markers of values that no admin screen may ever show. */
export const SENSITIVE_PATTERN =
  /SECRET-|\$2b\$|passwordHash|password_hash|tokenHash|refreshToken|refresh-\d|access-\d|notificationPreferences|taskReminders|avatarUrl/;
