import type { ApiClient, Page } from '../../core/api/apiClient';
import type { TaskStats, TripStats } from '../../shared/stats/types';
import { PAGE_SIZE } from './usersQuery';
import type { UsersFilters } from './usersQuery';

/**
 * One row of `GET /admin/users`. These are all the fields the backend
 * exposes for a list item: no password hash, tokens, bio, avatar or
 * notification preferences exist in the contract, and none is shown.
 */
export interface AdminUserSummary {
  id: string;
  email: string;
  displayName: string;
  role: string;
  timezone: string;
  createdAt: string;
  /** Set when the account is soft-deleted. */
  deletedAt: string | null;
  isActive: boolean;
}

/** `GET /admin/users/:id`: the summary plus counts — never task or trip content. */
export interface AdminUserDetail extends AdminUserSummary {
  language: string;
  updatedAt: string;
  /** Creation time of the account's newest session, or null. Not a measure of in-app usage. */
  lastActiveAt: string | null;
  tasks: TaskStats;
  trips: TripStats;
}

export function fetchUsers(
  api: ApiClient,
  filters: UsersFilters,
  cursor: string | undefined,
): Promise<Page<AdminUserSummary>> {
  return api.getPage<AdminUserSummary>('/admin/users', {
    q: filters.q || undefined,
    role: filters.role || undefined,
    status: filters.status,
    sort: filters.sort,
    cursor,
    limit: PAGE_SIZE,
  });
}

export function fetchUser(api: ApiClient, id: string): Promise<AdminUserDetail> {
  return api.get<AdminUserDetail>(`/admin/users/${encodeURIComponent(id)}`);
}
