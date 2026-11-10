import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

/**
 * Wraps pages that need a login.
 * Not logged in? Go to /login and remember where you were going (?redirect=…),
 * so after logging in you land back on the page you wanted.
 * Optionally pass adminOnly to also require the ADMIN role (used in Session 8).
 */
export default function ProtectedRoute({ adminOnly = false }) {
  const { isLoggedIn, isAdmin, initializing } = useAuth();
  const location = useLocation();

  if (initializing) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border" role="status" style={{ color: 'var(--ida-accent)' }}>
          <span className="visually-hidden">Loading…</span>
        </div>
      </div>
    );
  }

  if (!isLoggedIn) {
    const redirect = encodeURIComponent(location.pathname + location.search);
    return <Navigate to={`/login?redirect=${redirect}`} replace />;
  }

  if (adminOnly && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
