import { useEffect, useState } from 'react';
import { Link, NavLink, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';

/**
 * Top bar: logo, links, search, cart button with a count badge,
 * and either Login / Sign up buttons or the user's menu.
 */
export default function AppNavbar() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [query, setQuery] = useState(searchParams.get('keyword') || '');
  const { user, isLoggedIn, logout } = useAuth();
  const { itemCount, openDrawer } = useCart();
  const { showToast } = useToast();

  // Keep the box in sync if the URL changes (e.g. clicking "Clear filters")
  useEffect(() => {
    setQuery(searchParams.get('keyword') || '');
  }, [searchParams]);

  function handleSearch(event) {
    event.preventDefault();
    const keyword = query.trim();
    navigate(keyword ? `/products?keyword=${encodeURIComponent(keyword)}` : '/products');
  }

  function handleCartClick() {
    if (isLoggedIn) openDrawer();
    else navigate('/login?redirect=%2Fcart');
  }

  function handleLogout() {
    logout();
    showToast('You have been logged out.', 'info');
    navigate('/');
  }

  const firstName = user?.fullName?.split(' ')[0];

  return (
    <nav className="navbar navbar-expand-lg navbar-dark ida-navbar sticky-top py-3">
      <div className="container">
        <Link to="/" className="navbar-brand brand">
          Ida<span>Mart</span>
        </Link>

        {/* Cart button stays visible on phones, next to the menu button */}
        <div className="d-flex align-items-center gap-2 order-lg-last ms-auto ms-lg-3">
          <button
            type="button"
            className="btn btn-cart position-relative"
            onClick={handleCartClick}
            aria-label={`Open cart, ${itemCount} items`}
            data-testid="nav-cart"
          >
            <i className="bi bi-bag" />
            {itemCount > 0 && (
              <span className="cart-badge" data-testid="cart-count">{itemCount}</span>
            )}
          </button>

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
        </div>

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

          <div className="d-flex align-items-center gap-2 ms-lg-3 mb-2 mb-lg-0">
            {isLoggedIn ? (
              <div className="dropdown">
                <button
                  className="btn btn-user dropdown-toggle"
                  type="button"
                  data-bs-toggle="dropdown"
                  aria-expanded="false"
                  data-testid="user-menu"
                >
                  <span className="avatar">{firstName?.[0]?.toUpperCase()}</span>
                  <span data-testid="user-name">{firstName}</span>
                </button>
                <ul className="dropdown-menu dropdown-menu-end shadow-sm">
                  <li className="dropdown-header">
                    <div className="fw-semibold text-dark">{user.fullName}</div>
                    <div className="small">{user.email}</div>
                  </li>
                  <li><hr className="dropdown-divider" /></li>
                  <li>
                    <Link className="dropdown-item" to="/cart">
                      <i className="bi bi-bag me-2" />My cart
                    </Link>
                  </li>
                  <li><hr className="dropdown-divider" /></li>
                  <li>
                    <button className="dropdown-item text-danger" onClick={handleLogout} data-testid="logout">
                      <i className="bi bi-box-arrow-right me-2" />Log out
                    </button>
                  </li>
                </ul>
              </div>
            ) : (
              <>
                <Link to="/login" className="btn btn-link nav-link px-2" data-testid="nav-login">Log in</Link>
                <Link to="/register" className="btn btn-ida btn-sm" data-testid="nav-register">Sign up</Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
