/**
 * Aggregate counts shared by `GET /admin/dashboard` (system-wide) and
 * `GET /admin/users/:id` (one user), per docs/15-api-design.md#admin.
 */
export interface TaskStats {
  total: number;
  todo: number;
  inProgress: number;
  done: number;
  overdue: number;
  /** done / total as a whole percentage; 0 when there are no tasks. */
  completionRate: number;
}

export interface TripStats {
  total: number;
  planned: number;
  ongoing: number;
  completed: number;
  cancelled: number;
}
