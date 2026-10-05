import { useEffect, useState } from 'react';
import { Link, useLocation, useSearchParams } from 'react-router-dom';
import { useApi } from '../../core/auth/AuthContext';
import { formatDate } from '../../shared/format/format';
import { useAsync } from '../../shared/hooks/useAsync';
import { RoleBadge, StatusBadge } from '../../shared/ui/Badge';
import { Button } from '../../shared/ui/Button';
import { PageHeader } from '../../shared/ui/PageHeader';
import { EmptyState, ErrorState, LoadingState } from '../../shared/ui/StateViews';
import { fetchUsers } from './api';
import { UsersFilterBar } from './UsersFilterBar';
import {
  DEFAULT_FILTERS,
  hasActiveFilters,
  normalizeSearch,
  parseFilters,
  toSearchParams,
} from './usersQuery';
import type { UsersFilters } from './usersQuery';

const SEARCH_DEBOUNCE_MS = 350;

export function UsersPage() {
  const api = useApi();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const filters = parseFilters(searchParams);
  const filterKey = toSearchParams(filters).toString();

  // The list is cursor-paginated and the backend returns no total, so there
  // is no "page X of Y". Going back is possible because the cursors that
  // led to the current page are remembered here, per set of filters.
  const [paging, setPaging] = useState<{ key: string; cursors: string[] }>({
    key: filterKey,
    cursors: [],
  });
  const cursors = paging.key === filterKey ? paging.cursors : [];
  const cursor = cursors[cursors.length - 1];

  // What is in the search box, and the search it settled on once typing
  // paused (debounce) — the value this page writes to the URL.
  const [searchText, setSearchText] = useState(filters.q);
  const [settledSearch, setSettledSearch] = useState(filters.q);

  useEffect(() => {
    const timer = setTimeout(
      () => setSettledSearch(normalizeSearch(searchText)),
      SEARCH_DEBOUNCE_MS,
    );
    return () => clearTimeout(timer);
  }, [searchText]);

  // Keeps the search box in step with the URL. `q` changes in two ways:
  // this page writes it — then it equals the settled search — or the
  // browser's back/forward buttons change it. Only the second kind may
  // overwrite the box; otherwise a write landing while the admin is still
  // typing would eat their latest keystrokes.
  const [urlQ, setUrlQ] = useState(filters.q);
  if (filters.q !== urlQ) {
    setUrlQ(filters.q);
    if (filters.q !== settledSearch) {
      setSearchText(filters.q);
      setSettledSearch(filters.q);
    }
  }

  const applyFilters = (next: UsersFilters) => setSearchParams(toSearchParams(next));

  useEffect(() => {
    if (settledSearch !== filters.q) {
      setSearchParams(toSearchParams({ ...filters, q: settledSearch }));
    }
    // Only a settled search may change the URL; `filters` is read as it is at that moment.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [settledSearch]);

  const users = useAsync(() => fetchUsers(api, filters, cursor), `${filterKey}|${cursor ?? ''}`);

  const resetFilters = () => {
    setSearchText('');
    applyFilters(DEFAULT_FILTERS);
  };
  const goNext = (nextCursor: string) =>
    setPaging({ key: filterKey, cursors: [...cursors, nextCursor] });
  const goPrevious = () => setPaging({ key: filterKey, cursors: cursors.slice(0, -1) });

  const pageNumber = cursors.length + 1;
  const filtered = hasActiveFilters(filters);

  return (
    <>
      <PageHeader title="Kullanıcılar" description="Hesapları arayın, filtreleyin ve inceleyin" />

      <UsersFilterBar
        searchText={searchText}
        onSearchTextChange={setSearchText}
        filters={filters}
        onFilterChange={(change) => applyFilters({ ...filters, ...change })}
        canReset={filtered || searchText !== ''}
        onReset={resetFilters}
      />

      <div className="card card--flush">
        {users.status === 'loading' && <LoadingState label="Kullanıcılar yükleniyor…" />}
        {users.status === 'error' && <ErrorState error={users.error} onRetry={users.reload} />}
        {users.status === 'success' && users.data.data.length === 0 && (
          <EmptyState
            title="Kullanıcı bulunamadı."
            description={
              filtered ? 'Arama metnini veya filtreleri değiştirmeyi deneyin.' : undefined
            }
            action={
              filtered && (
                <Button variant="outlined" onClick={resetFilters}>
                  Filtreleri temizle
                </Button>
              )
            }
          />
        )}
        {users.status === 'success' && users.data.data.length > 0 && (
          <>
            <div className="table-scroll">
              <table className="table">
                <caption className="visually-hidden">Kullanıcı listesi</caption>
                <thead>
                  <tr>
                    <th scope="col">Kullanıcı</th>
                    <th scope="col">Rol</th>
                    <th scope="col">Durum</th>
                    <th scope="col">Saat dilimi</th>
                    <th scope="col">Kayıt tarihi</th>
                  </tr>
                </thead>
                <tbody>
                  {users.data.data.map((user) => (
                    <tr key={user.id}>
                      <td>
                        <Link
                          className="user-link"
                          to={`/users/${user.id}`}
                          state={{ from: location.pathname + location.search }}
                        >
                          <span className="user-link__name">{user.displayName}</span>
                          <span className="user-link__email">{user.email}</span>
                        </Link>
                      </td>
                      <td>
                        <RoleBadge role={user.role} />
                      </td>
                      <td>
                        <StatusBadge isActive={user.isActive} />
                      </td>
                      <td>{user.timezone}</td>
                      <td>{formatDate(user.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <nav className="pagination" aria-label="Sayfalama">
              <span className="pagination__info">
                Sayfa {pageNumber} · {users.data.data.length} kayıt
              </span>
              <div className="pagination__actions">
                <Button variant="outlined" onClick={goPrevious} disabled={cursors.length === 0}>
                  Önceki
                </Button>
                <Button
                  variant="outlined"
                  onClick={() => users.data.meta.nextCursor && goNext(users.data.meta.nextCursor)}
                  disabled={!users.data.meta.hasMore || !users.data.meta.nextCursor}
                >
                  Sonraki
                </Button>
              </div>
            </nav>
          </>
        )}
      </div>
    </>
  );
}
