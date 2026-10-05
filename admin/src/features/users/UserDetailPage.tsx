import type { ReactNode } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { isApiError } from '../../core/api/errors';
import { useApi } from '../../core/auth/AuthContext';
import { EMPTY_VALUE, formatDateTime, languageLabel } from '../../shared/format/format';
import { useAsync } from '../../shared/hooks/useAsync';
import { TaskStatsSection, TripStatsSection } from '../../shared/stats/StatsSections';
import { RoleBadge, StatusBadge } from '../../shared/ui/Badge';
import { Icon } from '../../shared/ui/Icon';
import { EmptyState, ErrorState, LoadingState } from '../../shared/ui/StateViews';
import { fetchUser } from './api';

function Field({ label, children, hint }: { label: string; children: ReactNode; hint?: string }) {
  return (
    <div className="detail-field">
      <dt className="detail-field__label">{label}</dt>
      <dd className="detail-field__value">
        {children}
        {hint && <span className="detail-field__hint">{hint}</span>}
      </dd>
    </div>
  );
}

export function UserDetailPage() {
  const api = useApi();
  const { id = '' } = useParams();
  const location = useLocation();
  const backTo = (location.state as { from?: string } | null)?.from ?? '/users';
  const user = useAsync(() => fetchUser(api, id), id);

  const backLink = (
    <Link className="back-link" to={backTo}>
      <Icon name="back" size={18} />
      Kullanıcılara dön
    </Link>
  );

  // 404: no such user. 400: the id in the URL is not a valid identifier.
  const notFound =
    user.status === 'error' && (isApiError(user.error, 404) || isApiError(user.error, 400));

  return (
    <>
      {backLink}

      {user.status === 'loading' && <LoadingState label="Kullanıcı bilgileri yükleniyor…" />}
      {notFound && (
        <EmptyState
          title="Kullanıcı bulunamadı"
          description="Bu adrese karşılık gelen bir hesap yok."
        />
      )}
      {user.status === 'error' && !notFound && (
        <ErrorState error={user.error} onRetry={user.reload} />
      )}
      {user.status === 'success' && (
        <>
          <header className="detail-header">
            <span className="avatar avatar--large" aria-hidden="true">
              {user.data.displayName.trim().charAt(0).toLocaleUpperCase('tr-TR') || '?'}
            </span>
            <div className="detail-header__text">
              <h1 className="page-header__title">{user.data.displayName}</h1>
              <p className="page-header__description">{user.data.email}</p>
            </div>
            <div className="detail-header__badges">
              <RoleBadge role={user.data.role} />
              <StatusBadge isActive={user.data.isActive} />
            </div>
          </header>

          <section className="section" aria-labelledby="account-title">
            <h2 id="account-title" className="section__title">
              Hesap bilgileri
            </h2>
            <dl className="card detail-grid">
              <Field label="Görünen ad">{user.data.displayName}</Field>
              <Field label="E-posta">{user.data.email}</Field>
              <Field label="Rol">
                <RoleBadge role={user.data.role} />
              </Field>
              <Field label="Durum">
                <StatusBadge isActive={user.data.isActive} />
              </Field>
              <Field label="Saat dilimi">{user.data.timezone}</Field>
              <Field label="Dil">{languageLabel(user.data.language)}</Field>
              <Field label="Kayıt tarihi">{formatDateTime(user.data.createdAt)}</Field>
              <Field label="Son güncelleme">{formatDateTime(user.data.updatedAt)}</Field>
              <Field
                label="Son oturum"
                hint="Son giriş veya oturum yenileme zamanı. Uygulama içi kullanım ölçüsü değildir."
              >
                {user.data.lastActiveAt
                  ? formatDateTime(user.data.lastActiveAt)
                  : 'Hiç oturum açmadı'}
              </Field>
              <Field label="Silinme tarihi">
                {user.data.deletedAt ? formatDateTime(user.data.deletedAt) : EMPTY_VALUE}
              </Field>
              <Field label="Kullanıcı kimliği">
                <code className="mono">{user.data.id}</code>
              </Field>
            </dl>
          </section>

          <TaskStatsSection
            stats={user.data.tasks}
            overdueHint={`Kullanıcının saat dilimine göre (${user.data.timezone})`}
          />
          <TripStatsSection stats={user.data.trips} />

          <p className="footnote">
            Yalnızca sayılar gösterilir. Kullanıcının görev ve seyahat içerikleri yönetim panelinde
            görüntülenemez.
          </p>
        </>
      )}
    </>
  );
}
