import type { ApiClient } from '../core/api/apiClient';
import { AuthProvider } from '../core/auth/AuthContext';
import { AppRoutes } from './AppRoutes';

/** The app below the router: `main.tsx` and tests each supply their own router and API client. */
export function App({ api }: { api: ApiClient }) {
  return (
    <AuthProvider api={api}>
      <AppRoutes />
    </AuthProvider>
  );
}
