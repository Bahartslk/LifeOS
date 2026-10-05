import { Button } from '../../shared/ui/Button';
import { Icon } from '../../shared/ui/Icon';
import { SEARCH_MAX_LENGTH, SEARCH_MIN_LENGTH } from './usersQuery';
import type { RoleFilter, SortOption, StatusFilter, UsersFilters } from './usersQuery';

const ROLE_OPTIONS: { value: RoleFilter; label: string }[] = [
  { value: '', label: 'Tüm roller' },
  { value: 'USER', label: 'Kullanıcı' },
  { value: 'ADMIN', label: 'Yönetici' },
];

const STATUS_OPTIONS: { value: StatusFilter; label: string }[] = [
  { value: 'active', label: 'Aktif hesaplar' },
  { value: 'deleted', label: 'Silinmiş hesaplar' },
  { value: 'all', label: 'Tüm hesaplar' },
];

const SORT_OPTIONS: { value: SortOption; label: string }[] = [
  { value: '-createdAt', label: 'Kayıt tarihi (yeni → eski)' },
  { value: 'createdAt', label: 'Kayıt tarihi (eski → yeni)' },
  { value: 'email', label: 'E-posta (A → Z)' },
  { value: '-email', label: 'E-posta (Z → A)' },
];

interface UsersFilterBarProps {
  searchText: string;
  onSearchTextChange(text: string): void;
  filters: UsersFilters;
  onFilterChange(change: Partial<UsersFilters>): void;
  canReset: boolean;
  onReset(): void;
}

export function UsersFilterBar({
  searchText,
  onSearchTextChange,
  filters,
  onFilterChange,
  canReset,
  onReset,
}: UsersFilterBarProps) {
  const trimmedLength = searchText.trim().length;
  const tooShort = trimmedLength > 0 && trimmedLength < SEARCH_MIN_LENGTH;

  return (
    <form className="filter-bar" role="search" onSubmit={(event) => event.preventDefault()}>
      <div className="field field--grow">
        <label className="field__label" htmlFor="users-search">
          Ara
        </label>
        <div className="input-with-icon">
          <Icon name="search" size={18} />
          <input
            id="users-search"
            className="input"
            type="search"
            placeholder="E-posta veya ad"
            autoComplete="off"
            maxLength={SEARCH_MAX_LENGTH}
            value={searchText}
            onChange={(event) => onSearchTextChange(event.target.value)}
            aria-describedby="users-search-hint"
          />
        </div>
        <span id="users-search-hint" className="field__hint" aria-live="polite">
          {tooShort ? `Aramak için en az ${SEARCH_MIN_LENGTH} karakter yazın.` : ' '}
        </span>
      </div>

      <div className="field">
        <label className="field__label" htmlFor="users-role">
          Rol
        </label>
        <select
          id="users-role"
          className="input"
          value={filters.role}
          onChange={(event) => onFilterChange({ role: event.target.value as RoleFilter })}
        >
          {ROLE_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <span className="field__hint">{' '}</span>
      </div>

      <div className="field">
        <label className="field__label" htmlFor="users-status">
          Durum
        </label>
        <select
          id="users-status"
          className="input"
          value={filters.status}
          onChange={(event) => onFilterChange({ status: event.target.value as StatusFilter })}
        >
          {STATUS_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <span className="field__hint">{' '}</span>
      </div>

      <div className="field">
        <label className="field__label" htmlFor="users-sort">
          Sıralama
        </label>
        <select
          id="users-sort"
          className="input"
          value={filters.sort}
          onChange={(event) => onFilterChange({ sort: event.target.value as SortOption })}
        >
          {SORT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <span className="field__hint">{' '}</span>
      </div>

      <div className="field field--action">
        <Button variant="text" onClick={onReset} disabled={!canReset}>
          Filtreleri temizle
        </Button>
        <span className="field__hint">{' '}</span>
      </div>
    </form>
  );
}
