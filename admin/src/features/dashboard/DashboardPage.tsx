import { useApi } from '../../core/auth/AuthContext';
import { formatDateTime } from '../../shared/format/format';
import { useAsync } from '../../shared/hooks/useAsync';
import { TaskStatsSection, TripStatsSection } from '../../shared/stats/StatsSections';
import { Button } from '../../shared/ui/Button';
import { Icon } from '../../shared/ui/Icon';
import { PageHeader } from '../../shared/ui/PageHeader';
import { StatCard } from '../../shared/ui/StatCard';
import { ErrorState, LoadingState } from '../../shared/ui/StateViews';
import { fetchDashboard } from './api';
import { TaskCharts, TripCharts } from './DashboardCharts';

export function DashboardPage() {
  const api = useApi();
  const dashboard = useAsync(() => fetchDashboard(api), 'dashboard');

  return (
    <>
      <PageHeader
        title="Dashboard"
        description={
          dashboard.status === 'success'
            ? `Son güncelleme: ${formatDateTime(dashboard.data.generatedAt)}`
            : 'Sistem genelindeki toplam sayılar'
        }
        actions={
          <Button
            variant="outlined"
            onClick={dashboard.reload}
            disabled={dashboard.status === 'loading'}
          >
            <Icon name="refresh" size={18} />
            Yenile
          </Button>
        }
      />

      {dashboard.status === 'loading' && <LoadingState label="Özet veriler yükleniyor…" />}
      {dashboard.status === 'error' && (
        <ErrorState error={dashboard.error} onRetry={dashboard.reload} />
      )}
      {dashboard.status === 'success' && (
        <>
          <section className="section" aria-labelledby="user-stats-title">
            <h2 id="user-stats-title" className="section__title">
              Kullanıcılar
            </h2>
            <div className="stat-grid stat-grid--six">
              <StatCard
                label="Toplam kullanıcı"
                value={dashboard.data.users.total}
                accent="violet"
                hint="Silinmemiş hesaplar"
              />
              <StatCard
                label="Son 7 günde oturum açan"
                value={dashboard.data.users.activeLast7Days}
                accent="teal"
                hint="Giriş yapan veya oturumu yenilenen hesaplar. Uygulama içi kullanım ölçüsü değildir."
              />
              <StatCard label="Yönetici" value={dashboard.data.users.admins} />
              <StatCard label="Son 7 günde yeni" value={dashboard.data.users.newLast7Days} />
              <StatCard label="Son 30 günde yeni" value={dashboard.data.users.newLast30Days} />
              <StatCard label="Silinmiş hesap" value={dashboard.data.users.deleted} />
            </div>
          </section>

          <TaskStatsSection stats={dashboard.data.tasks} overdueHint="Bugünün UTC tarihine göre">
            <TaskCharts stats={dashboard.data.tasks} />
          </TaskStatsSection>
          <TripStatsSection stats={dashboard.data.trips}>
            <TripCharts stats={dashboard.data.trips} />
          </TripStatsSection>

          <p className="footnote">
            Sayılar yalnızca silinmemiş hesapların silinmemiş kayıtlarını içerir. Görev ve seyahat
            içerikleri yönetim panelinde gösterilmez.
          </p>
        </>
      )}
    </>
  );
}
