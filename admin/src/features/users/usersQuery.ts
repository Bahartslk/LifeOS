/**
 * The filters of the user list, mirrored in the page's URL so a filtered
 * view can be bookmarked and survives back/forward navigation. Values are
 * the ones `GET /admin/users` accepts (docs/15-api-design.md#admin); the
 * backend rejects anything else with 400, so anything else found in a
 * hand-edited URL falls back to the default instead of being sent.
 */
export const ROLE_VALUES = ['USER', 'ADMIN'] as const;
export const STATUS_VALUES = ['active', 'deleted', 'all'] as const;
/** The backend sorts by creation date or email only — there is no display-name sort. */
export const SORT_VALUES = ['-createdAt', 'createdAt', 'email', '-email'] as const;

export type RoleFilter = (typeof ROLE_VALUES)[number] | '';
export type StatusFilter = (typeof STATUS_VALUES)[number];
export type SortOption = (typeof SORT_VALUES)[number];

export interface UsersFilters {
  q: string;
  role: RoleFilter;
  status: StatusFilter;
  sort: SortOption;
}

export const DEFAULT_FILTERS: UsersFilters = {
  q: '',
  role: '',
  status: 'active',
  sort: '-createdAt',
};

export const SEARCH_MIN_LENGTH = 2;
export const SEARCH_MAX_LENGTH = 100;
export const PAGE_SIZE = 20;

function oneOf<T extends string>(values: readonly T[], value: string | null, fallback: T): T {
  return values.includes(value as T) ? (value as T) : fallback;
}

/** The text to search for, or `''` when it is too short for the backend to accept. */
export function normalizeSearch(text: string): string {
  const trimmed = text.trim().slice(0, SEARCH_MAX_LENGTH);
  return trimmed.length >= SEARCH_MIN_LENGTH ? trimmed : '';
}

export function parseFilters(params: URLSearchParams): UsersFilters {
  return {
    q: normalizeSearch(params.get('q') ?? ''),
    role: oneOf<RoleFilter>(['', ...ROLE_VALUES], params.get('role'), DEFAULT_FILTERS.role),
    status: oneOf(STATUS_VALUES, params.get('status'), DEFAULT_FILTERS.status),
    sort: oneOf(SORT_VALUES, params.get('sort'), DEFAULT_FILTERS.sort),
  };
}

/** Only non-default filters appear in the URL, so the plain `/users` is the default view. */
export function toSearchParams(filters: UsersFilters): URLSearchParams {
  const params = new URLSearchParams();
  if (filters.q) params.set('q', filters.q);
  if (filters.role) params.set('role', filters.role);
  if (filters.status !== DEFAULT_FILTERS.status) params.set('status', filters.status);
  if (filters.sort !== DEFAULT_FILTERS.sort) params.set('sort', filters.sort);
  return params;
}

export function hasActiveFilters(filters: UsersFilters): boolean {
  return toSearchParams(filters).toString() !== '';
}
