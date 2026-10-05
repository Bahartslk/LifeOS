import { Navigate, Route, Routes } from 'react-router-dom';
import { RequireAdmin } from '../core/auth/RequireAdmin';
import { AccessDeniedPage } from '../features/auth/AccessDeniedPage';
import { LoginPage } from '../features/auth/LoginPage';
import { DashboardPage } from '../features/dashboard/DashboardPage';
import { UserDetailPage } from '../features/users/UserDetailPage';
import { UsersPage } from '../features/users/UsersPage';
import { AppLayout } from './AppLayout';

/**
 * The panel's single route table. Everything except the login and
 * access-denied screens sits behind `RequireAdmin`. Any unknown path,
 * including `/`, leads to the dashboard (and from there to the login
 * screen when there is no session).
 */
export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/access-denied" element={<AccessDeniedPage />} />
      <Route element={<RequireAdmin />}>
        <Route element={<AppLayout />}>
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="users/:id" element={<UserDetailPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
