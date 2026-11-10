import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import ProductImage from './ProductImage.jsx';
import { formatPrice, getErrorMessage } from '../utils/format';

/**
 * Slide-out cart from the right (Bootstrap "offcanvas" styles).
 * React controls when it's open through CartContext, so any page can open it,
 * e.g. right after "Add to cart".
 */
export default function MiniCartDrawer() {
  const { cart, drawerOpen, closeDrawer, removeItem } = useCart();
  const { showToast } = useToast();

  // Close on Escape, and stop the page behind from scrolling while open
  useEffect(() => {
    if (!drawerOpen) return undefined;
    const onKey = (e) => e.key === 'Escape' && closeDrawer();
    document.addEventListener('keydown', onKey);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = '';
    };
  }, [drawerOpen, closeDrawer]);

  async function handleRemove(item) {
    try {
      await removeItem(item.productId);
      showToast(`Removed ${item.productName}`, 'info');
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    }
  }

  return (
    <>
      <div
        className={`offcanvas offcanvas-end mini-cart ${drawerOpen ? 'show' : ''}`}
        tabIndex="-1"
        aria-labelledby="miniCartTitle"
        aria-hidden={!drawerOpen}
        data-testid="mini-cart"
      >
        <div className="offcanvas-header border-bottom">
          <h5 className="offcanvas-title fw-bold" id="miniCartTitle">
            Your cart <span className="text-muted fw-normal">({cart.totalItems})</span>
          </h5>
          <button type="button" className="btn-close" aria-label="Close" onClick={closeDrawer} />
        </div>

        <div className="offcanvas-body d-flex flex-column">
          {cart.items.length === 0 ? (
            <div className="text-center my-auto py-5">
              <i className="bi bi-bag fs-1 text-muted d-block mb-2" />
              <p className="text-muted">Your cart is empty.</p>
              <Link to="/products" className="btn btn-ida" onClick={closeDrawer}>Start shopping</Link>
            </div>
          ) : (
            <>
              <ul className="list-unstyled flex-grow-1 mb-0">
                {cart.items.map((item) => (
                  <li key={item.productId} className="d-flex gap-3 py-3 border-bottom" data-testid="mini-cart-item">
                    <Link to={`/products/${item.productId}`} onClick={closeDrawer} className="mini-thumb">
                      <ProductImage
                        product={{ name: item.productName, imageUrl: item.imageUrl, stock: item.availableStock }}
                      />
                    </Link>
                    <div className="flex-grow-1 min-w-0">
                      <div className="fw-semibold text-truncate">{item.productName}</div>
                      <div className="small text-muted">
                        {item.quantity} × {formatPrice(item.unitPrice)}
                      </div>
                      {!item.available && (
                        <div className="small text-danger">Not enough stock</div>
                      )}
                    </div>
                    <div className="text-end">
                      <div className="price small">{formatPrice(item.lineTotal)}</div>
                      <button
                        className="btn btn-link btn-sm text-muted p-0"
                        onClick={() => handleRemove(item)}
                        aria-label={`Remove ${item.productName}`}
                      >
                        <i className="bi bi-trash" />
                      </button>
                    </div>
                  </li>
                ))}
              </ul>

              <div className="pt-3">
                <div className="d-flex justify-content-between mb-3">
                  <span className="fw-semibold">Subtotal</span>
                  <span className="price fs-5" data-testid="mini-cart-total">{formatPrice(cart.totalAmount)}</span>
                </div>
                <Link to="/cart" className="btn btn-ida w-100 mb-2" onClick={closeDrawer} data-testid="view-cart">
                  View cart & checkout
                </Link>
                <button className="btn btn-ghost w-100" onClick={closeDrawer}>Continue shopping</button>
              </div>
            </>
          )}
        </div>
      </div>

      {drawerOpen && <div className="offcanvas-backdrop fade show" onClick={closeDrawer} />}
    </>
  );
}
