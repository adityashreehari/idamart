import { useState } from 'react';
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { getErrorMessage } from '../utils/format';
import { safeRedirect } from './LoginPage.jsx';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Same rules as the backend's RegisterRequest, checked in the browser first
 * so people see mistakes instantly. The backend still checks again
 * (never trust the browser alone).
 */
function validate(form) {
  const errors = {};
  if (form.fullName.trim().length < 2) errors.fullName = 'Please enter your full name.';
  if (!EMAIL_PATTERN.test(form.email.trim())) errors.email = 'Please enter a valid email address.';
  if (form.password.length < 8) errors.password = 'Password must be at least 8 characters.';
  else if (!/[A-Za-z]/.test(form.password) || !/\d/.test(form.password)) {
    errors.password = 'Password must contain at least one letter and one number.';
  }
  if (form.confirmPassword !== form.password) errors.confirmPassword = 'Passwords do not match.';
  return errors;
}

export default function RegisterPage() {
  const { register, isLoggedIn } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const redirectTo = safeRedirect(searchParams.get('redirect'));

  const [form, setForm] = useState({ fullName: '', email: '', password: '', confirmPassword: '' });
  const [touched, setTouched] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  if (isLoggedIn) return <Navigate to={redirectTo} replace />;

  const errors = validate(form);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
    setServerError('');
  }

  function handleBlur(e) {
    setTouched({ ...touched, [e.target.name]: true });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setTouched({ fullName: true, email: true, password: true, confirmPassword: true });
    if (Object.keys(errors).length > 0) return;

    setSubmitting(true);
    try {
      const user = await register(form.fullName.trim(), form.email.trim(), form.password);
      showToast(`Welcome to IdaMart, ${user.fullName.split(' ')[0]}!`);
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setServerError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  /** Bootstrap shows a red border with "is-invalid" and a green one with "is-valid". */
  const fieldClass = (name) =>
    `form-control ${touched[name] ? (errors[name] ? 'is-invalid' : 'is-valid') : ''}`;

  const fields = [
    { name: 'fullName', label: 'Full name', type: 'text', autoComplete: 'name' },
    { name: 'email', label: 'Email', type: 'email', autoComplete: 'email' },
    { name: 'password', label: 'Password', type: 'password', autoComplete: 'new-password',
      help: 'At least 8 characters, with a letter and a number.' },
    { name: 'confirmPassword', label: 'Confirm password', type: 'password', autoComplete: 'new-password' },
  ];

  return (
    <div className="container py-5">
      <div className="auth-card mx-auto">
        <div className="text-center mb-4">
          <div className="eyebrow mb-2">Join the club</div>
          <h1 className="h3 fw-bold">Create your account</h1>
        </div>

        {serverError && (
          <div className="alert alert-danger py-2" role="alert" data-testid="register-error">
            <i className="bi bi-exclamation-circle me-2" />{serverError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          {fields.map((f) => (
            <div className="mb-3" key={f.name}>
              <label htmlFor={f.name} className="form-label">{f.label}</label>
              <input
                id={f.name}
                name={f.name}
                type={f.type}
                className={fieldClass(f.name)}
                autoComplete={f.autoComplete}
                value={form[f.name]}
                onChange={handleChange}
                onBlur={handleBlur}
                data-testid={`register-${f.name}`}
              />
              {touched[f.name] && errors[f.name] ? (
                <div className="invalid-feedback">{errors[f.name]}</div>
              ) : (
                f.help && <div className="form-text">{f.help}</div>
              )}
            </div>
          ))}

          <button type="submit" className="btn btn-ida btn-lg w-100 mt-2" disabled={submitting} data-testid="register-submit">
            {submitting && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}
            Create account
          </button>
        </form>

        <p className="text-center text-muted mt-4 mb-0">
          Already have an account?{' '}
          <Link to={`/login${redirectTo !== '/' ? `?redirect=${encodeURIComponent(redirectTo)}` : ''}`}>
            Log in
          </Link>
        </p>
      </div>
    </div>
  );
}
