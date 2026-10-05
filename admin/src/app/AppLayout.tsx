import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../core/auth/AuthContext';
import { roleLabel } from '../shared/format/format';
import { BrandMark } from '../shared/ui/BrandMark';
import { Icon } from '../shared/ui/Icon';
import type { IconName } from '../shared/ui/Icon';

const NAV_ITEMS: { to: string; label: string; icon: IconName }[] = [
  { to: '/dashboard', label: 'Dashboard', icon: 'dashboard' },
  { to: '/users', label: 'Kullanıcılar', icon: 'users' },
];

/**
 * The signed-in shell: a sidebar with navigation and sign-out, a top bar
 * with the panel's title and the admin's identity, and the page.
 */
export function AppLayout() {
  const { admin, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [signingOut, setSigningOut] = useState(false);

  const handleLogout = async () => {
    setSigningOut(true);
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="shell">
      <a className="skip-link" href="#main-content">
        İçeriğe geç
      </a>

      <aside className={menuOpen ? 'sidebar sidebar--open' : 'sidebar'}>
        <div className="sidebar__brand">
          <BrandMark compact />
          <span className="sidebar__tag">Yönetim</span>
        </div>
        <nav className="sidebar__nav" aria-label="Ana menü">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => (isActive ? 'nav-link nav-link--active' : 'nav-link')}
              onClick={() => setMenuOpen(false)}
            >
              <Icon name={item.icon} />
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar__footer">
          <button
            type="button"
            className="nav-link nav-link--button"
            onClick={handleLogout}
            disabled={signingOut}
            aria-busy={signingOut || undefined}
          >
            <Icon name="logout" />
            Çıkış
          </button>
          <p className="sidebar__footnote">Salt okunur panel</p>
        </div>
      </aside>
      {menuOpen && (
        <button
          type="button"
          className="sidebar-backdrop"
          aria-label="Menüyü kapat"
          onClick={() => setMenuOpen(false)}
        />
      )}

      <div className="shell__main">
        <header className="topbar">
          <button
            type="button"
            className="icon-button topbar__menu"
            aria-label="Menüyü aç"
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen((open) => !open)}
          >
            <Icon name="menu" />
          </button>

          <span className="topbar__title">LifeOS Admin</span>
          <div className="topbar__spacer" />

          {admin && (
            <div className="topbar__identity">
              <span className="avatar" aria-hidden="true">
                {admin.displayName.trim().charAt(0).toLocaleUpperCase('tr-TR') || '?'}
              </span>
              <span className="topbar__who">
                <span className="topbar__name">{admin.displayName}</span>
                <span className="topbar__meta">
                  {admin.email} · {roleLabel(admin.role)}
                </span>
              </span>
            </div>
          )}
        </header>

        <main id="main-content" className="content" tabIndex={-1}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
