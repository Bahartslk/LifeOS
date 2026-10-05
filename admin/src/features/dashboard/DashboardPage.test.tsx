import { screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { formatDateTime } from '../../shared/format/format';
import { createFakeBackend, fakeUser } from '../../test/fakeBackend';
import { SENSITIVE_PATTERN, renderSignedIn, statValue, visibleText } from '../../test/renderApp';

/**
 * 7 accounts: 5 active (1 of them an admin) and 2 soft-deleted. Every
 * figure differs from every other, so a card showing the wrong field
 * cannot pass by coincidence.
 */
function setup() {
  const users = [
    fakeUser({ email: 'root@example.com', role: 'ADMIN', displayName: 'Root Admin' }),
    fakeUser({ email: 'ada@example.com' }),
    fakeUser({ email: 'bob@example.com' }),
    fakeUser({ email: 'cem@example.com' }),
    fakeUser({ email: 'deniz@example.com' }),
    fakeUser({ email: 'gone1@example.com', deletedAt: '2026-10-01T00:00:00.000Z' }),
    fakeUser({ email: 'gone2@example.com', deletedAt: '2026-10-02T00:00:00.000Z' }),
  ];
  const backend = createFakeBackend(users);
  backend.dashboardStats.newLast7Days = 3;
  backend.dashboardStats.newLast30Days = 9;
  backend.dashboardStats.activeLast7Days = 4;
  backend.dashboardStats.tasks = {
    total: 12,
    todo: 6,
    inProgress: 2,
    done: 4,
    overdue: 7,
    completionRate: 33,
  };
  backend.dashboardStats.trips = { total: 8, planned: 4, ongoing: 1, completed: 2, cancelled: 1 };
  return backend;
}

const open = (backend: ReturnType<typeof setup>) => renderSignedIn(backend, 'root@example.com');

describe('DashboardPage', () => {
  it('shows a loading state until the data arrives', async () => {
    const backend = setup();
    const release = backend.hold('GET', '/admin/dashboard');

    await open(backend);

    expect(await screen.findByRole('status')).toHaveTextContent('Özet veriler yükleniyor…');
    expect(screen.queryByText('Toplam kullanıcı')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Yenile' })).toBeDisabled();

    release();
    expect(await screen.findByText('Toplam kullanıcı')).toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });

  it('shows the user figures, each under its own label', async () => {
    await open(setup());
    await screen.findByText('Toplam kullanıcı');

    expect(statValue('Kullanıcılar', 'Toplam kullanıcı')).toBe('5');
    expect(statValue('Kullanıcılar', 'Son 7 günde oturum açan')).toBe('4');
    expect(statValue('Kullanıcılar', 'Yönetici')).toBe('1');
    expect(statValue('Kullanıcılar', 'Son 7 günde yeni')).toBe('3');
    expect(statValue('Kullanıcılar', 'Son 30 günde yeni')).toBe('9');
    expect(statValue('Kullanıcılar', 'Silinmiş hesap')).toBe('2');
  });

  it('presents activeLast7Days as session activity, not as the user total or as app usage', async () => {
    await open(setup());
    await screen.findByText('Toplam kullanıcı');

    const region = screen.getByRole('region', { name: 'Kullanıcılar' });
    expect(within(region).queryByText(/^aktif kullanıcı$/i)).not.toBeInTheDocument();
    expect(
      within(region).getByText(
        'Giriş yapan veya oturumu yenilenen hesaplar. Uygulama içi kullanım ölçüsü değildir.',
      ),
    ).toBeInTheDocument();
    expect(within(region).getByText('Silinmemiş hesaplar')).toBeInTheDocument();
  });

  it('shows the task figures, including overdue and the completion rate', async () => {
    await open(setup());
    await screen.findByText('Toplam görev');

    expect(statValue('Görevler', 'Toplam görev')).toBe('12');
    expect(statValue('Görevler', 'Yapılacak')).toBe('6');
    expect(statValue('Görevler', 'Devam eden')).toBe('2');
    expect(statValue('Görevler', 'Tamamlanan')).toBe('4');
    expect(statValue('Görevler', 'Geciken')).toBe('7');
    expect(statValue('Görevler', 'Tamamlanma oranı')).toBe('%33');
    expect(screen.getByText('Bugünün UTC tarihine göre')).toBeInTheDocument();
  });

  it('shows the trip figures', async () => {
    await open(setup());
    await screen.findByText('Toplam seyahat');

    expect(statValue('Seyahatler', 'Toplam seyahat')).toBe('8');
    expect(statValue('Seyahatler', 'Planlanan')).toBe('4');
    expect(statValue('Seyahatler', 'Devam eden')).toBe('1');
    expect(statValue('Seyahatler', 'Tamamlanan')).toBe('2');
    expect(statValue('Seyahatler', 'İptal edilen')).toBe('1');
  });

  it('shows when the figures were generated', async () => {
    await open(setup());

    expect(
      await screen.findByText(`Son güncelleme: ${formatDateTime('2026-10-05T12:00:00.000Z')}`),
    ).toBeInTheDocument();
  });

  it('draws the completion rate as a progress bar', async () => {
    await open(setup());

    const bar = await screen.findByRole('progressbar', { name: 'Görev tamamlanma oranı' });
    expect(bar).toHaveAttribute('aria-valuenow', '33');
    expect(bar.firstElementChild).toHaveStyle({ width: '33%' });
  });

  it('breaks tasks down by status with counts and shares', async () => {
    await open(setup());
    await screen.findByText('Görev durumu dağılımı');

    const legend = within(screen.getByRole('region', { name: 'Görevler' })).getByRole('list');
    const items = within(legend)
      .getAllByRole('listitem')
      .map((item) => item.textContent);
    expect(items).toEqual(['Yapılacak6 · %50', 'Devam eden2 · %17', 'Tamamlanan4 · %33']);
  });

  it('breaks trips down by status with counts and shares', async () => {
    await open(setup());
    await screen.findByText('Seyahat durumu dağılımı');

    const legend = within(screen.getByRole('region', { name: 'Seyahatler' })).getByRole('list');
    const items = within(legend)
      .getAllByRole('listitem')
      .map((item) => item.textContent);
    expect(items).toEqual([
      'Planlanan4 · %50',
      'Devam eden1 · %13',
      'Tamamlanan2 · %25',
      'İptal edilen1 · %13',
    ]);
  });

  it('says so when there are no tasks or trips, instead of drawing empty shares', async () => {
    const backend = setup();
    backend.dashboardStats.tasks = {
      total: 0,
      todo: 0,
      inProgress: 0,
      done: 0,
      overdue: 0,
      completionRate: 0,
    };
    backend.dashboardStats.trips = { total: 0, planned: 0, ongoing: 0, completed: 0, cancelled: 0 };

    await open(backend);

    expect(await screen.findByText('Henüz görev yok.')).toBeInTheDocument();
    expect(screen.getByText('Henüz seyahat yok.')).toBeInTheDocument();
    expect(statValue('Görevler', 'Tamamlanma oranı')).toBe('%0');
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '0');
    expect(screen.queryByRole('list')).not.toBeInTheDocument();
  });

  it('shows whatever the API returns — no figure is built into the page', async () => {
    const backend = setup();
    backend.dashboardStats.activeLast7Days = 4321;
    backend.dashboardStats.tasks = {
      total: 98765,
      todo: 1,
      inProgress: 2,
      done: 98762,
      overdue: 0,
      completionRate: 100,
    };

    await open(backend);
    await screen.findByText('Toplam görev');

    expect(statValue('Kullanıcılar', 'Son 7 günde oturum açan')).toBe('4.321');
    expect(statValue('Görevler', 'Toplam görev')).toBe('98.765');
    expect(statValue('Görevler', 'Tamamlanma oranı')).toBe('%100');
    expect(backend.callsTo('GET', '/admin/dashboard')).toHaveLength(1);
  });

  it('shows an error with a retry that loads the data', async () => {
    const backend = setup();
    backend.fail('GET', '/admin/dashboard', 500);
    const { user } = await open(backend);

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('Veriler yüklenemedi');
    expect(alert).toHaveTextContent('Sunucuda bir hata oluştu.');
    expect(screen.queryByText('Toplam kullanıcı')).not.toBeInTheDocument();

    backend.clearOverrides();
    await user.click(screen.getByRole('button', { name: 'Tekrar dene' }));

    expect(await screen.findByText('Toplam kullanıcı')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('shows an error instead of breaking on a response that is not the documented shape', async () => {
    const backend = setup();
    backend.override('GET', '/admin/dashboard', () => backend.json(200, { unexpected: true }));

    await open(backend);

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Sunucudan beklenmeyen bir yanıt alındı.',
    );
  });

  it('fetches fresh figures on "Yenile"', async () => {
    const backend = setup();
    const { user } = await open(backend);
    await screen.findByText('Toplam görev');
    backend.dashboardStats.newLast7Days = 77;

    await user.click(screen.getByRole('button', { name: 'Yenile' }));

    await screen.findByText('77');
    expect(statValue('Kullanıcılar', 'Son 7 günde yeni')).toBe('77');
    expect(backend.callsTo('GET', '/admin/dashboard')).toHaveLength(2);
  });

  it('shows aggregates only: no other account and nothing sensitive', async () => {
    await open(setup());
    await screen.findByText('Toplam kullanıcı');

    expect(visibleText()).not.toMatch(SENSITIVE_PATTERN);
    expect(visibleText()).not.toContain('ada@example.com');
    expect(visibleText()).not.toContain('gone1@example.com');
  });
});
