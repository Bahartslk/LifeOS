import type { ApiClient } from '../../core/api/apiClient';
import type { TaskStats, TripStats } from '../../shared/stats/types';

/** `GET /admin/dashboard`, per docs/15-api-design.md#admin. */
export interface AdminDashboard {
  users: {
    /** Accounts that are not soft-deleted. */
    total: number;
    deleted: number;
    admins: number;
    newLast7Days: number;
    newLast30Days: number;
    /** Accounts that signed in or renewed a session in 7 days — NOT in-app usage. */
    activeLast7Days: number;
  };
  tasks: TaskStats;
  trips: TripStats;
  generatedAt: string;
}

export function fetchDashboard(api: ApiClient): Promise<AdminDashboard> {
  return api.get<AdminDashboard>('/admin/dashboard');
}
