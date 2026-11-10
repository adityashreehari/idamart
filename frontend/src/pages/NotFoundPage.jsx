import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="container py-5">
      <div className="empty-state py-5">
        <div className="display-1 fw-bold display-font" style={{ color: 'var(--ida-accent)' }}>404</div>
        <h1 className="h3 mb-2">This page went on a side quest.</h1>
        <p className="text-muted mb-4">The page you're looking for doesn't exist.</p>
        <Link to="/" className="btn btn-ida">Back to home</Link>
      </div>
    </div>
  );
}
