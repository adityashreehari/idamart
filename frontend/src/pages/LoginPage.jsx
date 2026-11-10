import { useState } from 'react';
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { getErrorMessage } from '../utils/format';

/** Only allow redirects inside our own site (never to another website). */
export function safeRedirect(value) {
  return value && value.startsWith('/') && !value.startsWith('//') ? value : '/';
}

export default function LoginPage() {
  const { login, isLoggedIn } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const redirectTo = safeRedirect(searchParams.get('redirect'));

  const [form, setForm] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  if (isLoggedIn) return <Navigate to={redirectTo} replace />;

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError('');
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!form.email.trim() || !form.password) {
      setError('Please enter your email and password.');
      return;
    }
    setSubmitting(true);
    try {
      const user = await login(form.email.trim(), form.password);
      showToast(`Welcome back, ${user.fullName.split(' ')[0]}!`);
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="container py-5">
      <div className="auth-card mx-auto">
        <div className="text-center mb-4">
          <div className="eyebrow mb-2">Welcome back</div>
          <h1 className="h3 fw-bold">Log in to IdaMart</h1>
          {redirectTo !== '/' && (
            <p className="text-muted small mb-0">Please log in to continue.</p>
          )}
        </div>

        {error && (
          <div className="alert alert-danger py-2" role="alert" data-testid="login-error">
            <i className="bi bi-exclamation-circle me-2" />{error}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <div className="mb-3">
            <label htmlFor="email" className="form-label">Email</label>
            <input
              id="email"
              name="email"
              type="email"
              className="form-control form-control-lg"
              autoComplete="email"
              value={form.email}
              onChange={handleChange}
              data-testid="login-email"
            />
          </div>

          <div className="mb-4">
            <label htmlFor="password" className="form-label">Password</label>
            <div className="input-group input-group-lg">
              <input
                id="password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                className="form-control"
                autoComplete="current-password"
                value={form.password}
                onChange={handleChange}
                data-testid="login-password"
              />
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`} />
              </button>
            </div>
          </div>

          <button type="submit" className="btn btn-ida btn-lg w-100" disabled={submitting} data-testid="login-submit">
            {submitting && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}
            Log in
          </button>
        </form>

        <p className="text-center text-muted mt-4 mb-0">
          New to IdaMart?{' '}
          <Link to={`/register${redirectTo !== '/' ? `?redirect=${encodeURIComponent(redirectTo)}` : ''}`}>
            Create an account
          </Link>
        </p>
      </div>
    </div>
  );
}
