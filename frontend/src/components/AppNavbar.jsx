import { useEffect, useState } from 'react';
import { Link, NavLink, useNavigate, useSearchParams } from 'react-router-dom';

/**
 * Top bar: logo, links, and a search box that jumps to the shop page.
 * (Session 7 adds Login / Register / Cart here.)
 */
export default function AppNavbar() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [query, setQuery] = useState(searchParams.get('keyword') || '');

  // Keep the box in sync if the URL changes (e.g. clicking "Clear filters")
  useEffect(() => {
    setQuery(searchParams.get('keyword') || '');
  }, [searchParams]);

  function handleSearch(event) {
    event.preventDefault();
    const keyword = query.trim();
    navigate(keyword ? `/products?keyword=${encodeURIComponent(keyword)}` : '/products');
  }

  return (
    <nav className="navbar navbar-expand-lg navbar-dark ida-navbar sticky-top py-3">
      <div className="container">
        <Link to="/" className="navbar-brand brand">
          Ida<span>Mart</span>
        </Link>

        <button
          className="navbar-toggler border-0"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#mainNav"
          aria-controls="mainNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon" />
        </button>

        <div className="collapse navbar-collapse" id="mainNav">
          <ul className="navbar-nav me-auto ms-lg-4 gap-lg-2">
            <li className="nav-item">
              <NavLink to="/" end className="nav-link">Home</NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/products" className="nav-link">Shop</NavLink>
            </li>
          </ul>

          <form className="nav-search position-relative my-3 my-lg-0" role="search" onSubmit={handleSearch}>
            <i className="bi bi-search" aria-hidden="true" />
            <input
              className="form-control"
              type="search"
              placeholder="Search figures, hoodies, manga…"
              aria-label="Search products"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              data-testid="nav-search"
            />
          </form>
        </div>
      </div>
    </nav>
  );
}
