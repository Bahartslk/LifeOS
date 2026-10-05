import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/auth/AuthContext';
import { Button } from '../../shared/ui/Button';
import { Icon } from '../../shared/ui/Icon';

/**
 * Shown when the backend answers 403: the account is valid but is not an
 * admin. The session has already been ended by `AuthProvider`.
 */
export function AccessDeniedPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const backToLogin = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <main className="auth-screen">
      <div className="auth-card auth-card--centered">
        <span className="state__icon state__icon--danger">
          <Icon name="lock" size={28} />
        </span>
        <h1 className="auth-card__title">Yetkisiz erişim</h1>
        <p className="auth-card__subtitle">
          Bu hesabın yönetim paneline erişim yetkisi yok. Yönetici yetkisine sahip bir hesapla giriş
          yapmanız gerekir.
        </p>
        <Button className="button--block" onClick={backToLogin}>
          Giriş ekranına dön
        </Button>
      </div>
    </main>
  );
}
