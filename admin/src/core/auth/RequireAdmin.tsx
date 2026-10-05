import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

/**
 * Keeps the panel's screens behind a backend-confirmed admin session.
 * This is navigation, not security: a visitor who bypasses it still gets
 * nothing, because every admin request is authorized by the backend.
 */
export function RequireAdmin() {
  const { status } = useAuth();
  const location = useLocation();

  if (status === 'denied') {
    return <Navigate to="/access-denied" replace />;
  }
  if (status !== 'admin') {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  }
  return <Outlet />;
}
