import { screen, waitFor, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { formatDate, formatDateTime } from '../../shared/format/format';
import { createFakeBackend, fakeUser } from '../../test/fakeBackend';
import type { FakeBackend } from '../../test/fakeBackend';
import {
  SENSITIVE_PATTERN,
  currentLocation,
  renderSignedIn,
  visibleText,
} from '../../test/renderApp';

const at = (day: number) => new Date(Date.UTC(2026, 8, day, 10, 0, 0)).toISOString();

/** 5 active accounts (newest first: cem, bob, ada, root, zoe) and 1 soft-deleted. */
function setup() {
  const zoe = fakeUser({ email: 'zoe@example.com', displayName: 'Zoe', createdAt: at(1) });
  const root = fakeUser({
    email: 'root@example.com',
    displayName: 'Root Admin',
    role: 'ADMIN',
    createdAt: at(2),
  });
  const ada = fakeUser({
    email: 'ada@example.com',
    displayName: 'Ada Lovelace',
    timezone: 'America/New_York',
    createdAt: at(3),
    lastActiveAt: '2026-10-04T08:30:00.000Z',
  });
  const bob = fakeUser({ email: 'bob@example.com', displayName: 'Bob', createdAt: at(4) });
  const cem = fakeUser({ email: 'cem@example.com', displayName: 'Cem', createdAt: at(5) });
  const gone = fakeUser({
    email: 'gone@example.com',
    displayName: 'Gone Ada',
    createdAt: at(6),
    deletedAt: at(7),
  });
  return {
    zoe,
    root,
    ada,
    bob,
    cem,
    gone,
    backend: createFakeBackend([zoe, root, ada, bob, cem, gone]),
  };
}

/** 45 more active accounts, so the default view spans three pages of 20. */
function setupManyUsers() {
  const context = setup();
  for (let index = 0; index < 45; index += 1) {
    context.backend.users.push(
      fakeUser({
        email: `bulk${String(index).padStart(2, '0')}@example.com`,
        displayName: `Bulk ${index}`,
        createdAt: new Date(Date.UTC(2026, 7, 1, 0, index)).toISOString(),
      }),
    );
  }
  return context;
}

const open = (backend: FakeBackend, route = '/users') =>
  renderSignedIn(backend, 'root@example.com', route);

const listCalls = (backend: FakeBackend) => backend.callsTo('GET', '/admin/users');
const lastQuery = (backend: FakeBackend) => listCalls(backend).at(-1)?.query;

/** The emails of the rows on screen, in order. */
function shownEmails(): string[] {
  const rows = within(screen.getByRole('table')).getAllByRole('row').slice(1);
  return rows.map((row) => row.querySelector('.user-link__email')?.textContent ?? '');
}

async function waitForEmails(expected: string[]) {
  await waitFor(() => expect(shownEmails()).toEqual(expected));
}

describe('UsersPage', () => {
  describe('loading the list', () => {
    it('shows a loading state until the list arrives', async () => {
      const { backend } = setup();
      const release = backend.hold('GET', '/admin/users');

      await open(backend);

      expect(await screen.findByRole('status')).toHaveTextContent('Kullanıcılar yükleniyor…');
      expect(screen.queryByRole('table')).not.toBeInTheDocument();

      release();
      expect(await screen.findByRole('table')).toBeInTheDocument();
    });

    it('asks for active accounts, newest first, 20 per page by default', async () => {
      const { backend } = setup();

      await open(backend);
      await screen.findByRole('table');

      expect(lastQuery(backend)).toEqual({ status: 'active', sort: '-createdAt', limit: '20' });
      expect(shownEmails()).toEqual([
        'cem@example.com',
        'bob@example.com',
        'ada@example.com',
        'root@example.com',
        'zoe@example.com',
      ]);
    });

    it('shows name, email, role, status, timezone and creation date of each account', async () => {
      const { backend, ada } = setup();

      await open(backend);

      const row = (await screen.findByText('Ada Lovelace')).closest('tr') as HTMLElement;
      expect(within(row).getByText('ada@example.com')).toBeInTheDocument();
      expect(within(row).getByText('Kullanıcı')).toBeInTheDocument();
      expect(within(row).getByText('Aktif')).toBeInTheDocument();
      expect(within(row).getByText('America/New_York')).toBeInTheDocument();
      expect(within(row).getByText(formatDate(ada.createdAt))).toBeInTheDocument();

      const adminRow = within(screen.getByRole('table'))
        .getByText('Root Admin')
        .closest('tr') as HTMLElement;
      expect(within(adminRow).getByText('Yönetici')).toBeInTheDocument();
    });

    it('has no "last active" column: the list contract does not include it', async () => {
      const { backend, ada } = setup();

      await open(backend);
      await screen.findByRole('table');

      const headers = screen.getAllByRole('columnheader').map((header) => header.textContent);
      expect(headers).toEqual(['Kullanıcı', 'Rol', 'Durum', 'Saat dilimi', 'Kayıt tarihi']);
      expect(visibleText()).not.toContain(formatDateTime(ada.lastActiveAt));
    });

    it('shows an error with a retry that loads the list', async () => {
      const { backend } = setup();
      backend.fail('GET', '/admin/users', 500);
      const { user } = await open(backend);

      const alert = await screen.findByRole('alert');
      expect(alert).toHaveTextContent('Veriler yüklenemedi');
      expect(screen.queryByRole('table')).not.toBeInTheDocument();

      backend.clearOverrides();
      await user.click(screen.getByRole('button', { name: 'Tekrar dene' }));

      expect(await screen.findByRole('table')).toBeInTheDocument();
    });

    it('explains a rate limit (429) in Turkish', async () => {
      const { backend } = setup();
      backend.fail('GET', '/admin/users', 429);

      await open(backend);

      expect(await screen.findByRole('alert')).toHaveTextContent('Çok fazla istek gönderildi.');
    });

    it('shows an error instead of breaking on a response that is not the documented shape', async () => {
      const { backend } = setup();
      backend.override('GET', '/admin/users', () => backend.json(200, { data: { not: 'a list' } }));

      await open(backend);

      expect(await screen.findByRole('alert')).toHaveTextContent(
        'Sunucudan beklenmeyen bir yanıt alındı.',
      );
    });
  });

  describe('searching', () => {
    it('searches once typing settles, not on every keystroke', async () => {
      const { backend } = setup();
      const { user } = await open(backend);
      await screen.findByRole('table');

      await user.type(screen.getByLabelText('Ara'), 'lovelace');

      await waitForEmails(['ada@example.com']);
      const searches = listCalls(backend).filter((call) => call.query.q !== undefined);
      expect(searches.map((call) => call.query.q)).toEqual(['lovelace']);
      expect(currentLocation()).toBe('/users?q=lovelace');
    });

    it('matches email and name, case-insensitively', async () => {
      const { backend } = setup();
      const { user } = await open(backend);
      await screen.findByRole('table');

      await user.type(screen.getByLabelText('Ara'), 'BOB@');

      await waitForEmails(['bob@example.com']);
    });

    it('does not search for a single character, and says why', async () => {
      const { backend } = setup();
      const { user } = await open(backend);
      await screen.findByRole('table');
      const before = listCalls(backend).length;

      await user.type(screen.getByLabelText('Ara'), 'a');

      expect(await screen.findByText('Aramak için en az 2 karakter yazın.')).toBeInTheDocument();
      await new Promise((resolve) => setTimeout(resolve, 500));
      expect(listCalls(backend)).toHaveLength(before);
      expect(currentLocation()).toBe('/users');
    });

    it('shows "Kullanıcı bulunamadı." when nothing matches, and can clear the filters', async () => {
      const { backend } = setup();
      const { user } = await open(backend);
      await screen.findByRole('table');

      await user.type(screen.getByLabelText('Ara'), 'nobody-here');

      expect(
        await screen.findByRole('heading', { name: 'Kullanıcı bulunamadı.' }),
      ).toBeInTheDocument();
      expect(screen.queryByRole('table')).not.toBeInTheDocument();

      await user.click(
        within(
          screen.getByRole('heading', { name: 'Kullanıcı bulunamadı.' }).parentElement!,
        ).getByRole('button', { name: 'Filtreleri temizle' }),
      );

      expect(await screen.findByRole('table')).toBeInTheDocument();
      expect(screen.getByLabelText('Ara')).toHaveValue('');
      expect(currentLocation()).toBe('/users');
    });

    it('reads the search text from the URL', async () => {
      const { backend } = setup();

      await open(backend, '/users?q=bob');

      await screen.findByRole('table');
      expect(screen.getByLabelText('Ara')).toHaveValue('bob');
      expect(lastQuery(backend)).toMatchObject({ q: 'bob' });
      expect(shownEmails()).toEqual(['bob@example.com']);
    });

    it('follows the URL when back/forward changes the search', async () => {
      const { backend } = setup();
      const { user, go } = await open(backend);
      await screen.findByRole('table');
      const input = screen.getByLabelText('Ara');

      await user.type(input, 'bob');
      await waitForEmails(['bob@example.com']);
      await user.clear(input);
      await user.type(input, 'cem');
      await waitForEmails(['cem@example.com']);
      expect(currentLocation()).toBe('/users?q=cem');

      await go(-1);
      await waitFor(() => expect(currentLocation()).toBe('/users?q=bob'));
      expect(input).toHaveValue('bob');
      await waitForEmails(['bob@example.com']);

      await go(1);
      await waitFor(() => expect(currentLocation()).toBe('/users?q=cem'));
      expect(input).toHaveValue('cem');
      await waitForEmails(['cem@example.com']);

      // The box stays put afterwards: the URL is not rewritten from stale text.
      await new Promise((resolve) => setTimeout(resolve, 500));
      expect(currentLocation()).toBe('/users?q=cem');
      expect(input).toHaveValue('cem');
    });

    it('does not lose keystrokes typed while an earlier search is being applied', async () => {
      const { backend } = setup();
      const { user } = await open(backend);
      await screen.findByRole('table');
      const input = screen.getByLabelText('Ara');

      await user.type(input, 'ad');
      await waitFor(() => expect(currentLocation()).toBe('/users?q=ad'));
      await user.type(input, 'a lovelace');

      expect(input).toHaveValue('ada lovelace');
      await waitFor(() => expect(currentLocation()).toBe('/users?q=ada+lovelace'));
      await waitForEmails(['ada@example.com']);
    });
  });

  describe('filtering and sorting', () => {
    it('filters by role', async () => {
      const { backend } = setup();
      const { user } = await open(backend);
      await screen.findByRole('table');

      await user.selectOptions(screen.getByLabelText('Rol'), 'Yönetici');

      await waitForEmails(['root@example.com']);
      expect(lastQuery(backend)).toMatchObject({ role: 'ADMIN' });
      expect(currentLocation()).toBe('/users?role=ADMIN');
    });

    it('lists soft-deleted accounts only when asked, marked "Silinmiş hesap"', async () => {
      const { backend } = setup();
      const { user } = await open(backend);
      await screen.findByRole('table');
      expect(shownEmails()).not.toContain('gone@example.com');

      await user.selectOptions(screen.getByLabelText('Durum'), 'Silinmiş hesaplar');

      await waitForEmails(['gone@example.com']);
      expect(lastQuery(backend)).toMatchObject({ status: 'deleted' });
      expect(currentLocation()).toBe('/users?status=deleted');
      expect(screen.getByText('Silinmiş hesap')).toBeInTheDocument();
    });

    it('lists every account for "Tüm hesaplar"', async () => {
      const { backend } = setup();
      const { user } = await open(backend);
      await screen.findByRole('table');

      await user.selectOptions(screen.getByLabelText('Durum'), 'Tüm hesaplar');

      await waitFor(() => expect(shownEmails()).toHaveLength(6));
      expect(shownEmails()[0]).toBe('gone@example.com');
    });

    it('sorts by email in both directions', async () => {
      const { backend } = setup();
      const { user } = await open(backend);
      await screen.findByRole('table');

      await user.selectOptions(screen.getByLabelText('Sıralama'), 'E-posta (A → Z)');
      await waitForEmails([
        'ada@example.com',
        'bob@example.com',
        'cem@example.com',
        'root@example.com',
        'zoe@example.com',
      ]);
      expect(lastQuery(backend)).toMatchObject({ sort: 'email' });
      expect(currentLocation()).toBe('/users?sort=email');

      await user.selectOptions(screen.getByLabelText('Sıralama'), 'E-posta (Z → A)');
      await waitFor(() => expect(shownEmails()[0]).toBe('zoe@example.com'));
      expect(lastQuery(backend)).toMatchObject({ sort: '-email' });
    });

    it('offers only the sorts the backend supports', async () => {
      const { backend } = setup();
      await open(backend);
      await screen.findByRole('table');

      const options = within(screen.getByLabelText('Sıralama'))
        .getAllByRole('option')
        .map((option) => (option as HTMLOptionElement).value);
      expect(options).toEqual(['-createdAt', 'createdAt', 'email', '-email']);
    });

    it('restores every filter from the URL and sends them together', async () => {
      const { backend } = setup();

      await open(backend, '/users?q=ada&status=all&sort=email&role=USER');

      await screen.findByRole('table');
      expect(screen.getByLabelText('Ara')).toHaveValue('ada');
      expect(screen.getByLabelText('Rol')).toHaveValue('USER');
      expect(screen.getByLabelText('Durum')).toHaveValue('all');
      expect(screen.getByLabelText('Sıralama')).toHaveValue('email');
      expect(lastQuery(backend)).toEqual({
        q: 'ada',
        role: 'USER',
        status: 'all',
        sort: 'email',
        limit: '20',
      });
      expect(shownEmails()).toEqual(['ada@example.com', 'gone@example.com']);
    });

    it('keeps the other filters in the URL when one changes', async () => {
      const { backend } = setup();
      const { user } = await open(backend, '/users?q=ada&status=all');
      await screen.findByRole('table');

      await user.selectOptions(screen.getByLabelText('Sıralama'), 'E-posta (A → Z)');

      await waitFor(() => expect(currentLocation()).toBe('/users?q=ada&status=all&sort=email'));
    });

    it('never sends a value from a hand-edited URL that the backend would reject', async () => {
      const { backend } = setup();

      await open(backend, '/users?status=banned&sort=passwordHash&role=ROOT&q=x');

      await screen.findByRole('table');
      expect(lastQuery(backend)).toEqual({ status: 'active', sort: '-createdAt', limit: '20' });
    });
  });

  describe('paging', () => {
    it('moves forward and back with cursors, without a page total', async () => {
      const { backend } = setupManyUsers();
      const { user } = await open(backend);
      await screen.findByRole('table');
      const firstPage = shownEmails();
      const previous = () => screen.getByRole('button', { name: 'Önceki' });
      const next = () => screen.getByRole('button', { name: 'Sonraki' });

      expect(firstPage).toHaveLength(20);
      expect(previous()).toBeDisabled();
      expect(screen.getByText('Sayfa 1 · 20 kayıt')).toBeInTheDocument();
      expect(lastQuery(backend)).not.toHaveProperty('cursor');

      await user.click(next());
      await screen.findByText('Sayfa 2 · 20 kayıt');
      const secondPage = shownEmails();
      expect(secondPage).toHaveLength(20);
      expect(secondPage.filter((email) => firstPage.includes(email))).toEqual([]);
      const lastIdOfFirstPage = backend.users.find((u) => u.email === firstPage[19])?.id;
      expect(lastQuery(backend)).toMatchObject({ cursor: lastIdOfFirstPage });

      await user.click(next());
      await screen.findByText('Sayfa 3 · 10 kayıt');
      expect(next()).toBeDisabled();

      await user.click(previous());
      await screen.findByText('Sayfa 2 · 20 kayıt');
      expect(shownEmails()).toEqual(secondPage);

      await user.click(previous());
      await screen.findByText('Sayfa 1 · 20 kayıt');
      expect(shownEmails()).toEqual(firstPage);
      expect(lastQuery(backend)).not.toHaveProperty('cursor');
      expect(visibleText()).not.toMatch(/Sayfa\s+\d+\s*\/\s*\d+/);
    });

    it('disables "Sonraki" when everything fits on one page', async () => {
      const { backend } = setup();

      await open(backend);
      await screen.findByRole('table');

      expect(screen.getByRole('button', { name: 'Sonraki' })).toBeDisabled();
      expect(screen.getByRole('button', { name: 'Önceki' })).toBeDisabled();
    });

    it('returns to the first page when a filter changes', async () => {
      const { backend } = setupManyUsers();
      const { user } = await open(backend);
      await screen.findByRole('table');
      await user.click(screen.getByRole('button', { name: 'Sonraki' }));
      await screen.findByText('Sayfa 2 · 20 kayıt');

      await user.selectOptions(screen.getByLabelText('Sıralama'), 'E-posta (A → Z)');

      await screen.findByText('Sayfa 1 · 20 kayıt');
      expect(lastQuery(backend)).not.toHaveProperty('cursor');
      expect(shownEmails()[0]).toBe('ada@example.com');
    });
  });

  describe('opening a user', () => {
    it('opens the detail page and returns to the same filtered list', async () => {
      const { backend, ada } = setup();
      const { user } = await open(backend, '/users?status=all&sort=email');
      await screen.findByRole('table');

      await user.click(screen.getByRole('link', { name: /Ada Lovelace/ }));
      expect(await screen.findByRole('heading', { name: 'Ada Lovelace' })).toBeInTheDocument();
      expect(currentLocation()).toBe(`/users/${ada.id}`);

      await user.click(screen.getByRole('link', { name: 'Kullanıcılara dön' }));
      await screen.findByRole('table');
      expect(currentLocation()).toBe('/users?status=all&sort=email');
    });
  });

  describe('privacy', () => {
    it('shows nothing sensitive, even if the backend were to send it', async () => {
      const { backend, ada } = setup();
      backend.override('GET', '/admin/users', () =>
        backend.json(200, {
          data: [
            {
              id: ada.id,
              email: ada.email,
              displayName: ada.displayName,
              role: 'USER',
              timezone: ada.timezone,
              createdAt: ada.createdAt,
              deletedAt: null,
              isActive: true,
              passwordHash: '$2b$10$SECRET-PASSWORD-HASH',
              bio: 'SECRET-BIO',
              avatarUrl: 'https://example.com/SECRET-AVATAR.png',
              notificationPreferences: { taskReminders: true },
              refreshToken: 'SECRET-REFRESH',
              tokenHash: 'SECRET-TOKEN-HASH',
              lastActiveAt: '2026-10-04T08:30:00.000Z',
              tasks: [{ title: 'SECRET-TASK-TITLE' }],
              trips: [{ destination: 'SECRET-DESTINATION' }],
            },
          ],
          meta: { nextCursor: null, limit: 20, hasMore: false },
        }),
      );

      await open(backend);

      await screen.findByText('Ada Lovelace');
      expect(visibleText()).not.toMatch(SENSITIVE_PATTERN);
      expect(visibleText()).not.toContain(formatDateTime('2026-10-04T08:30:00.000Z'));
    });
  });
});
