import { screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { formatDateTime } from '../../shared/format/format';
import { createFakeBackend, fakeUser } from '../../test/fakeBackend';
import type { FakeBackend } from '../../test/fakeBackend';
import {
  SENSITIVE_PATTERN,
  currentLocation,
  renderSignedIn,
  statValue,
  visibleText,
} from '../../test/renderApp';

const UNKNOWN_ID = '00000000-0000-4000-8000-00000000ffff';

function setup() {
  const root = fakeUser({ email: 'root@example.com', displayName: 'Root Admin', role: 'ADMIN' });
  const ada = fakeUser({
    email: 'ada@example.com',
    displayName: 'Ada Lovelace',
    timezone: 'America/New_York',
    language: 'en',
    createdAt: '2026-09-01T10:00:00.000Z',
    updatedAt: '2026-09-15T11:30:00.000Z',
    lastActiveAt: '2026-10-04T08:30:00.000Z',
    tasks: { total: 9, todo: 4, inProgress: 2, done: 3, overdue: 1, completionRate: 33 },
    trips: { total: 7, planned: 3, ongoing: 1, completed: 2, cancelled: 1 },
  });
  const gone = fakeUser({
    email: 'gone@example.com',
    displayName: 'Gone User',
    deletedAt: '2026-10-02T14:00:00.000Z',
    tasks: { total: 1, todo: 1, inProgress: 0, done: 0, overdue: 1, completionRate: 0 },
  });
  return { root, ada, gone, backend: createFakeBackend([root, ada, gone]) };
}

const open = (backend: FakeBackend, id: string) =>
  renderSignedIn(backend, 'root@example.com', `/users/${id}`);

/** The value shown next to `label` in the account details. */
function field(label: string): string {
  const term = within(screen.getByRole('region', { name: 'Hesap bilgileri' })).getByText(label);
  return term.nextElementSibling?.textContent ?? '';
}

describe('UserDetailPage', () => {
  it('shows a loading state until the user arrives', async () => {
    const { backend, ada } = setup();
    const release = backend.hold('GET', `/admin/users/${ada.id}`);

    await open(backend, ada.id);

    expect(await screen.findByRole('status')).toHaveTextContent('Kullanıcı bilgileri yükleniyor…');
    expect(screen.queryByRole('heading', { name: 'Ada Lovelace' })).not.toBeInTheDocument();

    release();
    expect(await screen.findByRole('heading', { name: 'Ada Lovelace' })).toBeInTheDocument();
  });

  it('shows the account fields of the requested user', async () => {
    const { backend, ada } = setup();

    await open(backend, ada.id);
    await screen.findByRole('heading', { name: 'Ada Lovelace' });

    expect(backend.callsTo('GET', `/admin/users/${ada.id}`)).toHaveLength(1);
    expect(field('Görünen ad')).toBe('Ada Lovelace');
    expect(field('E-posta')).toBe('ada@example.com');
    expect(field('Rol')).toBe('Kullanıcı');
    expect(field('Durum')).toBe('Aktif');
    expect(field('Saat dilimi')).toBe('America/New_York');
    expect(field('Dil')).toBe('İngilizce (en)');
    expect(field('Kayıt tarihi')).toBe(formatDateTime('2026-09-01T10:00:00.000Z'));
    expect(field('Son güncelleme')).toBe(formatDateTime('2026-09-15T11:30:00.000Z'));
    expect(field('Silinme tarihi')).toBe('—');
    expect(field('Kullanıcı kimliği')).toBe(ada.id);
  });

  it('shows the last session time and says what it does and does not mean', async () => {
    const { backend, ada } = setup();

    await open(backend, ada.id);
    await screen.findByRole('heading', { name: 'Ada Lovelace' });

    expect(field('Son oturum')).toContain(formatDateTime('2026-10-04T08:30:00.000Z'));
    expect(field('Son oturum')).toContain(
      'Son giriş veya oturum yenileme zamanı. Uygulama içi kullanım ölçüsü değildir.',
    );
  });

  it('says so when the user has never signed in', async () => {
    const { backend, gone } = setup();

    await open(backend, gone.id);
    await screen.findByRole('heading', { name: 'Gone User' });

    expect(field('Son oturum')).toContain('Hiç oturum açmadı');
  });

  it("shows the user's own task counts, with overdue measured in their timezone", async () => {
    const { backend, ada } = setup();

    await open(backend, ada.id);
    await screen.findByRole('heading', { name: 'Ada Lovelace' });

    expect(statValue('Görevler', 'Toplam görev')).toBe('9');
    expect(statValue('Görevler', 'Yapılacak')).toBe('4');
    expect(statValue('Görevler', 'Devam eden')).toBe('2');
    expect(statValue('Görevler', 'Tamamlanan')).toBe('3');
    expect(statValue('Görevler', 'Geciken')).toBe('1');
    expect(statValue('Görevler', 'Tamamlanma oranı')).toBe('%33');
    expect(
      screen.getByText('Kullanıcının saat dilimine göre (America/New_York)'),
    ).toBeInTheDocument();
    expect(screen.queryByText('Bugünün UTC tarihine göre')).not.toBeInTheDocument();
  });

  it("shows the user's own trip counts", async () => {
    const { backend, ada } = setup();

    await open(backend, ada.id);
    await screen.findByRole('heading', { name: 'Ada Lovelace' });

    expect(statValue('Seyahatler', 'Toplam seyahat')).toBe('7');
    expect(statValue('Seyahatler', 'Planlanan')).toBe('3');
    expect(statValue('Seyahatler', 'Devam eden')).toBe('1');
    expect(statValue('Seyahatler', 'Tamamlanan')).toBe('2');
    expect(statValue('Seyahatler', 'İptal edilen')).toBe('1');
  });

  it('does not show the dashboard charts or any system-wide figure', async () => {
    const { backend, ada } = setup();

    await open(backend, ada.id);
    await screen.findByRole('heading', { name: 'Ada Lovelace' });

    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    expect(screen.queryByText('Görev durumu dağılımı')).not.toBeInTheDocument();
    expect(screen.queryByText('Seyahat durumu dağılımı')).not.toBeInTheDocument();
    expect(screen.queryByText('Görev tamamlanma oranı')).not.toBeInTheDocument();
    expect(screen.queryByRole('region', { name: 'Kullanıcılar' })).not.toBeInTheDocument();
    expect(backend.callsTo('GET', '/admin/dashboard')).toHaveLength(0);
  });

  it('shows a soft-deleted account, clearly marked, with its deletion time', async () => {
    const { backend, gone } = setup();

    await open(backend, gone.id);
    await screen.findByRole('heading', { name: 'Gone User' });

    expect(screen.getAllByText('Silinmiş hesap').length).toBeGreaterThan(0);
    expect(field('Durum')).toBe('Silinmiş hesap');
    expect(field('Silinme tarihi')).toBe(formatDateTime('2026-10-02T14:00:00.000Z'));
    expect(screen.queryByText('Aktif')).not.toBeInTheDocument();
    expect(statValue('Görevler', 'Geciken')).toBe('1');
  });

  it('says "Kullanıcı bulunamadı" for an id that does not exist (404)', async () => {
    const { backend } = setup();

    await open(backend, UNKNOWN_ID);

    expect(
      await screen.findByRole('heading', { name: 'Kullanıcı bulunamadı' }),
    ).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Tekrar dene' })).not.toBeInTheDocument();
    expect(screen.queryByRole('region', { name: 'Hesap bilgileri' })).not.toBeInTheDocument();
  });

  it('says "Kullanıcı bulunamadı" for an id that is not a UUID (400)', async () => {
    const { backend } = setup();

    await open(backend, 'not-a-uuid');

    expect(
      await screen.findByRole('heading', { name: 'Kullanıcı bulunamadı' }),
    ).toBeInTheDocument();
    expect(backend.callsTo('GET', '/admin/users/not-a-uuid')).toHaveLength(1);
  });

  it('shows a retryable error for a server failure, not "not found"', async () => {
    const { backend, ada } = setup();
    backend.fail('GET', `/admin/users/${ada.id}`, 500);
    const { user } = await open(backend, ada.id);

    expect(await screen.findByRole('alert')).toHaveTextContent('Sunucuda bir hata oluştu.');
    expect(screen.queryByText('Kullanıcı bulunamadı')).not.toBeInTheDocument();

    backend.clearOverrides();
    await user.click(screen.getByRole('button', { name: 'Tekrar dene' }));

    expect(await screen.findByRole('heading', { name: 'Ada Lovelace' })).toBeInTheDocument();
  });

  it('returns to the user list with "Kullanıcılara dön"', async () => {
    const { backend, ada } = setup();
    const { user } = await open(backend, ada.id);
    await screen.findByRole('heading', { name: 'Ada Lovelace' });

    await user.click(screen.getByRole('link', { name: 'Kullanıcılara dön' }));

    expect(await screen.findByRole('heading', { name: 'Kullanıcılar' })).toBeInTheDocument();
    expect(currentLocation()).toBe('/users');
  });

  it('shows nothing sensitive, even if the backend were to send it', async () => {
    const { backend, ada } = setup();
    backend.override('GET', `/admin/users/${ada.id}`, () =>
      backend.json(200, {
        data: {
          id: ada.id,
          email: ada.email,
          displayName: ada.displayName,
          role: 'USER',
          timezone: ada.timezone,
          language: ada.language,
          createdAt: ada.createdAt,
          updatedAt: ada.updatedAt,
          deletedAt: null,
          isActive: true,
          lastActiveAt: ada.lastActiveAt,
          tasks: {
            ...ada.tasks,
            items: [{ title: 'SECRET-TASK-TITLE', description: 'SECRET-TASK' }],
          },
          trips: {
            ...ada.trips,
            items: [{ title: 'SECRET-TRIP', destination: 'SECRET-DESTINATION' }],
          },
          passwordHash: '$2b$10$SECRET-PASSWORD-HASH',
          refreshToken: 'SECRET-REFRESH',
          tokenHash: 'SECRET-TOKEN-HASH',
          bio: 'SECRET-BIO',
          avatarUrl: 'https://example.com/SECRET-AVATAR.png',
          notificationPreferences: { taskReminders: true, tripReminders: true, aiInsights: true },
        },
      }),
    );

    await open(backend, ada.id);
    await screen.findByRole('heading', { name: 'Ada Lovelace' });

    expect(visibleText()).not.toMatch(SENSITIVE_PATTERN);
    expect(visibleText()).not.toMatch(/\bbio\b|biyografi|bildirim tercih/i);
    expect(document.querySelector('img')).toBeNull();
  });
});
