import { useState } from 'react';
import type { FormEvent } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { describeError, isApiError } from '../../core/api/errors';
import { useAuth } from '../../core/auth/AuthContext';
import { Button } from '../../shared/ui/Button';
import { BrandMark } from '../../shared/ui/BrandMark';

function loginErrorMessage(error: unknown): string {
  if (isApiError(error, 401) || isApiError(error, 400)) {
    return 'E-posta veya şifre hatalı.';
  }
  if (isApiError(error, 429)) {
    return 'Çok fazla giriş denemesi yapıldı. Lütfen bir dakika sonra tekrar deneyin.';
  }
  return describeError(error);
}

export function LoginPage() {
  const { status, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/dashboard';

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (status === 'admin') {
    return <Navigate to={from} replace />;
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    setError(null);
    try {
      const result = await login(email.trim(), password);
      // The password is not kept in memory any longer than the request needs it.
      setPassword('');
      navigate(result === 'admin' ? from : '/access-denied', { replace: true });
    } catch (caught) {
      setPassword('');
      setError(loginErrorMessage(caught));
      setSubmitting(false);
    }
  };

  return (
    <main className="auth-screen">
      <div className="auth-card">
        <BrandMark />
        <h1 className="auth-card__title">Yönetim Paneli</h1>
        <p className="auth-card__subtitle">Devam etmek için yönetici hesabınızla giriş yapın.</p>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="field">
            <label className="field__label" htmlFor="login-email">
              E-posta
            </label>
            <input
              id="login-email"
              className="input"
              type="email"
              autoComplete="username"
              inputMode="email"
              required
              maxLength={254}
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              disabled={submitting}
            />
          </div>

          <div className="field">
            <label className="field__label" htmlFor="login-password">
              Şifre
            </label>
            <input
              id="login-password"
              className="input"
              type="password"
              autoComplete="current-password"
              required
              maxLength={72}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              disabled={submitting}
            />
          </div>

          {error && (
            <p className="form-error" role="alert">
              {error}
            </p>
          )}

          <Button
            type="submit"
            className="button--block"
            busy={submitting}
            disabled={email.trim() === '' || password === ''}
          >
            Giriş yap
          </Button>
        </form>

        <p className="auth-card__note">
          Bu panel yalnızca yetkili yöneticiler içindir. Sayfa yenilendiğinde güvenlik nedeniyle
          yeniden giriş yapmanız gerekir.
        </p>
      </div>
    </main>
  );
}
