import { useState } from 'react';
import { Link } from 'react-router-dom';
import ProductImage from '../components/ProductImage.jsx';
import QuantityStepper from '../components/QuantityStepper.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { formatPrice, getErrorMessage } from '../utils/format';

const MAX_PER_ITEM = 10;

export default function CartPage() {
  const { cart, loading, updateQuantity, removeItem, clearCart } = useCart();
  const { showToast } = useToast();
  const [busyId, setBusyId] = useState(null); // which line is being updated

  async function run(productId, action, successMessage) {
    setBusyId(productId);
    try {
      await action();
      if (successMessage) showToast(successMessage, 'info');
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setBusyId(null);
    }
  }

  async function handleClear() {
    if (!window.confirm('Remove everything from your cart?')) return;
    await run('all', clearCart, 'Cart cleared');
  }

  if (loading && cart.items.length === 0) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border" role="status" style={{ color: 'var(--ida-accent)' }}>
          <span className="visually-hidden">Loading cart…</span>
        </div>
      </div>
    );
  }

  if (cart.items.length === 0) {
    return (
      <div className="container py-5">
        <div className="empty-state py-5" data-testid="cart-empty">
          <i className="bi bi-bag d-block mb-2" />
          <h1 className="h4">Your cart is empty</h1>
          <p className="text-muted">Find something you love in the shop.</p>
          <Link to="/products" className="btn btn-ida">Start shopping</Link>
        </div>
      </div>
    );
  }

  const hasProblems = cart.items.some((i) => !i.available);

  return (
    <div className="container py-4 py-lg-5">
      <div className="d-flex flex-wrap align-items-end justify-content-between gap-3 mb-4">
        <div>
          <div className="eyebrow">Checkout</div>
          <h1 className="h2 fw-bold mb-0">Your cart</h1>
        </div>
        <button className="btn btn-link text-muted" onClick={handleClear} disabled={busyId !== null} data-testid="clear-cart">
          <i className="bi bi-trash me-1" />Clear cart
        </button>
      </div>

      {hasProblems && (
        <div className="alert alert-warning" role="alert">
          <i className="bi bi-exclamation-triangle me-2" />
          Some items no longer have enough stock. Lower the quantity or remove them before checking out.
        </div>
      )}

      <div className="row g-4">
        {/* ---------- Lines ---------- */}
        <div className="col-lg-8">
          <div className="cart-panel">
            {cart.items.map((item) => {
              const maxQty = Math.max(1, Math.min(MAX_PER_ITEM, item.availableStock));
              const busy = busyId === item.productId;
              return (
                <div
                  key={item.productId}
                  className={`cart-line ${busy ? 'opacity-50' : ''}`}
                  data-testid="cart-line"
                >
                  <Link to={`/products/${item.productId}`} className="cart-thumb">
                    <ProductImage product={{ name: item.productName, imageUrl: item.imageUrl, stock: item.availableStock }} />
                  </Link>

                  <div className="flex-grow-1 min-w-0">
                    <div className="small text-muted">{item.animeSeries}</div>
                    <Link to={`/products/${item.productId}`} className="fw-semibold text-reset text-decoration-none d-block text-truncate">
                      {item.productName}
                    </Link>
                    <div className="small text-muted mb-2">{formatPrice(item.unitPrice)} each</div>
                    {!item.available && (
                      <div className="small text-danger mb-2">
                        {item.availableStock === 0 ? 'Out of stock' : `Only ${item.availableStock} left`}
                      </div>
                    )}
                    <div className="d-flex align-items-center gap-3">
                      <QuantityStepper
                        value={item.quantity}
                        max={maxQty}
                        onChange={(q) => run(item.productId, () => updateQuantity(item.productId, q))}
                        disabled={busy}
                        size="sm"
                        testId="cart-qty"
                      />
                      <button
                        className="btn btn-link btn-sm text-muted p-0"
                        onClick={() => run(item.productId, () => removeItem(item.productId), `Removed ${item.productName}`)}
                        disabled={busy}
                        data-testid="cart-remove"
                      >
                        Remove
                      </button>
                    </div>
                  </div>

                  <div className="price text-end" data-testid="cart-line-total">{formatPrice(item.lineTotal)}</div>
                </div>
              );
            })}
          </div>

          <Link to="/products" className="btn btn-link text-muted mt-3 px-0">
            <i className="bi bi-arrow-left me-1" />Continue shopping
          </Link>
        </div>

        {/* ---------- Summary ---------- */}
        <div className="col-lg-4">
          <div className="cart-panel p-4 position-sticky" style={{ top: '6rem' }}>
            <h2 className="h5 fw-bold mb-3">Order summary</h2>
            <div className="d-flex justify-content-between mb-2">
              <span className="text-muted">Items</span>
              <span data-testid="summary-items">{cart.totalItems}</span>
            </div>
            <div className="d-flex justify-content-between mb-2">
              <span className="text-muted">Delivery</span>
              <span className="text-success">Free</span>
            </div>
            <hr />
            <div className="d-flex justify-content-between align-items-baseline mb-4">
              <span className="fw-semibold">Total</span>
              <span className="price fs-4" data-testid="summary-total">{formatPrice(cart.totalAmount)}</span>
            </div>
            {/* Session 8 adds the checkout page */}
            <button className="btn btn-ida btn-lg w-100" disabled data-testid="checkout-button">
              Checkout
            </button>
            <p className="small text-muted text-center mt-2 mb-0">
              <i className="bi bi-info-circle me-1" />Checkout opens in the next update.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
