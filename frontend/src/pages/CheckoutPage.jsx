import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import ProductImage from '../components/ProductImage.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { placeOrderRequest } from '../api/orders';
import { formatPrice, getErrorMessage } from '../utils/format';

const EMIRATES = ['Abu Dhabi', 'Dubai', 'Sharjah', 'Ajman', 'Umm Al Quwain', 'Ras Al Khaimah', 'Fujairah'];
const PHONE_PATTERN = /^\+?[0-9\s-]{8,15}$/;

function validate(form) {
  const errors = {};
  if (form.fullName.trim().length < 2) errors.fullName = 'Please enter the name for delivery.';
  if (!PHONE_PATTERN.test(form.phone.trim())) errors.phone = 'Please enter a valid phone number.';
  if (form.address.trim().length < 5) errors.address = 'Please enter your building, street and flat number.';
  if (form.area.trim().length < 2) errors.area = 'Please enter your area or neighbourhood.';
  if (!form.emirate) errors.emirate = 'Please choose an emirate.';
  return errors;
}

/** The backend stores one address line, so the form fields are joined into a single string. */
function toShippingAddress(form) {
  return [form.fullName, form.phone, form.address, form.area, form.emirate, 'UAE']
    .map((part) => part.trim())
    .filter(Boolean)
    .join(', ');
}

export default function CheckoutPage() {
  const { user } = useAuth();
  const { cart, ready, refreshCart } = useCart();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: user?.fullName || '',
    phone: '',
    address: '',
    area: '',
    emirate: 'Dubai',
  });
  const [touched, setTouched] = useState({});
  const [placing, setPlacing] = useState(false);
  const [serverError, setServerError] = useState('');

  // Wait for the cart to load (e.g. after a page refresh) before deciding anything
  if (!ready) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border" role="status" style={{ color: 'var(--ida-accent)' }}>
          <span className="visually-hidden">Loading cart…</span>
        </div>
      </div>
    );
  }

  // Nothing to check out? Back to the cart page.
  if (cart.items.length === 0 && !placing) return <Navigate to="/cart" replace />;

  const errors = validate(form);
  const hasStockProblem = cart.items.some((i) => !i.available);

  const fieldClass = (name, base = 'form-control') =>
    `${base} ${touched[name] ? (errors[name] ? 'is-invalid' : 'is-valid') : ''}`;

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
    setServerError('');
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setTouched({ fullName: true, phone: true, address: true, area: true, emirate: true });
    if (Object.keys(errors).length > 0 || hasStockProblem) return;

    setPlacing(true);
    try {
      const order = await placeOrderRequest(toShippingAddress(form));
      await refreshCart(); // the backend emptied the cart
      showToast(`Order #${order.id} placed. Thank you!`);
      navigate(`/orders/${order.id}?placed=1`, { replace: true });
    } catch (err) {
      setServerError(getErrorMessage(err));
      refreshCart(); // stock may have changed: show the latest numbers
      setPlacing(false);
    }
  }

  return (
    <div className="container py-4 py-lg-5">
      <div className="mb-4">
        <div className="eyebrow">Almost there</div>
        <h1 className="h2 fw-bold mb-0">Checkout</h1>
      </div>

      {serverError && (
        <div className="alert alert-danger" role="alert" data-testid="checkout-error">
          <i className="bi bi-exclamation-circle me-2" />{serverError}
        </div>
      )}
      {hasStockProblem && (
        <div className="alert alert-warning" role="alert">
          Some items don't have enough stock any more. <Link to="/cart">Update your cart</Link> first.
        </div>
      )}

      <form className="row g-4" onSubmit={handleSubmit} noValidate>
        {/* ---------- Delivery details ---------- */}
        <div className="col-lg-7">
          <div className="cart-panel p-4">
            <h2 className="h5 fw-bold mb-3"><span className="step-dot">1</span>Delivery details</h2>
            <div className="row g-3">
              <div className="col-md-6">
                <label htmlFor="fullName" className="form-label">Full name</label>
                <input id="fullName" name="fullName" className={fieldClass('fullName')} value={form.fullName}
                  onChange={handleChange} onBlur={() => setTouched({ ...touched, fullName: true })}
                  autoComplete="name" data-testid="checkout-name" />
                {touched.fullName && errors.fullName && <div className="invalid-feedback">{errors.fullName}</div>}
              </div>
              <div className="col-md-6">
                <label htmlFor="phone" className="form-label">Phone</label>
                <input id="phone" name="phone" type="tel" className={fieldClass('phone')} value={form.phone}
                  placeholder="+971 50 123 4567" onChange={handleChange}
                  onBlur={() => setTouched({ ...touched, phone: true })} autoComplete="tel" data-testid="checkout-phone" />
                {touched.phone && errors.phone && <div className="invalid-feedback">{errors.phone}</div>}
              </div>
              <div className="col-12">
                <label htmlFor="address" className="form-label">Building, street, flat</label>
                <input id="address" name="address" className={fieldClass('address')} value={form.address}
                  placeholder="Flat 1204, Marina Tower, Al Marsa Street" onChange={handleChange}
                  onBlur={() => setTouched({ ...touched, address: true })} autoComplete="street-address"
                  data-testid="checkout-address" />
                {touched.address && errors.address && <div className="invalid-feedback">{errors.address}</div>}
              </div>
              <div className="col-md-6">
                <label htmlFor="area" className="form-label">Area</label>
                <input id="area" name="area" className={fieldClass('area')} value={form.area}
                  placeholder="Dubai Marina" onChange={handleChange}
                  onBlur={() => setTouched({ ...touched, area: true })} data-testid="checkout-area" />
                {touched.area && errors.area && <div className="invalid-feedback">{errors.area}</div>}
              </div>
              <div className="col-md-6">
                <label htmlFor="emirate" className="form-label">Emirate</label>
                <select id="emirate" name="emirate" className={fieldClass('emirate', 'form-select')}
                  value={form.emirate} onChange={handleChange} data-testid="checkout-emirate">
                  {EMIRATES.map((em) => <option key={em}>{em}</option>)}
                </select>
              </div>
            </div>

            <h2 className="h5 fw-bold mt-4 mb-3"><span className="step-dot">2</span>Payment</h2>
            <div className="payment-option">
              <i className="bi bi-cash-coin fs-4" />
              <div>
                <div className="fw-semibold">Cash on delivery</div>
                <div className="small text-muted">
                  Demo store: no card is charged and no real payment is taken.
                </div>
              </div>
              <i className="bi bi-check-circle-fill ms-auto" style={{ color: 'var(--ida-accent)' }} />
            </div>
          </div>
        </div>

        {/* ---------- Summary ---------- */}
        <div className="col-lg-5">
          <div className="cart-panel p-4 position-sticky" style={{ top: '6rem' }}>
            <h2 className="h5 fw-bold mb-3">Order summary</h2>
            <ul className="list-unstyled mb-3">
              {cart.items.map((item) => (
                <li key={item.productId} className="d-flex align-items-center gap-3 py-2">
                  <div className="mini-thumb position-relative">
                    <ProductImage product={{ name: item.productName, imageUrl: item.imageUrl, stock: item.availableStock }} />
                    <span className="qty-bubble">{item.quantity}</span>
                  </div>
                  <div className="flex-grow-1 min-w-0 small fw-semibold text-truncate">{item.productName}</div>
                  <div className="small">{formatPrice(item.lineTotal)}</div>
                </li>
              ))}
            </ul>
            <div className="d-flex justify-content-between mb-2">
              <span className="text-muted">Subtotal ({cart.totalItems} items)</span>
              <span>{formatPrice(cart.totalAmount)}</span>
            </div>
            <div className="d-flex justify-content-between mb-2">
              <span className="text-muted">Delivery</span>
              <span className="text-success">Free</span>
            </div>
            <hr />
            <div className="d-flex justify-content-between align-items-baseline mb-4">
              <span className="fw-semibold">Total</span>
              <span className="price fs-4" data-testid="checkout-total">{formatPrice(cart.totalAmount)}</span>
            </div>
            <button type="submit" className="btn btn-ida btn-lg w-100" disabled={placing || hasStockProblem}
              data-testid="place-order">
              {placing && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}
              Place order
            </button>
            <Link to="/cart" className="btn btn-link text-muted w-100 mt-2">Back to cart</Link>
          </div>
        </div>
      </form>
    </div>
  );
}
