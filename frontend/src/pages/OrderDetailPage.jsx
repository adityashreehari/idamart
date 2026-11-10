import { useCallback, useEffect, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Modal from '../components/Modal.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { cancelOrderRequest, fetchMyOrder } from '../api/orders';
import { formatPrice, getErrorMessage } from '../utils/format';
import { STATUS_FLOW, STATUS_INFO, canCancel, formatDate } from '../utils/orderStatus';

export default function OrderDetailPage() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const justPlaced = searchParams.get('placed') === '1';
  const { showToast } = useToast();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notFound, setNotFound] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setOrder(await fetchMyOrder(id));
    } catch (err) {
      if ([400, 404].includes(err.response?.status)) setNotFound(true);
      else setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleCancel() {
    setCancelling(true);
    try {
      setOrder(await cancelOrderRequest(order.id));
      showToast(`Order #${order.id} cancelled. The items are back in stock.`, 'info');
      setConfirmCancel(false);
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setCancelling(false);
    }
  }

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border" role="status" style={{ color: 'var(--ida-accent)' }}>
          <span className="visually-hidden">Loading order…</span>
        </div>
      </div>
    );
  }

  if (notFound) {
    return (
      <div className="container py-5">
        <div className="empty-state">
          <i className="bi bi-question-circle d-block mb-2" />
          <h1 className="h4">Order not found</h1>
          <Link to="/orders" className="btn btn-ida mt-2">My orders</Link>
        </div>
      </div>
    );
  }

  if (error) return <div className="container py-5"><ErrorMessage message={error} onRetry={load} /></div>;

  const cancelled = order.status === 'CANCELLED';
  const reached = STATUS_FLOW.indexOf(order.status);

  return (
    <div className="container py-4 py-lg-5">
      {justPlaced && (
        <div className="order-success mb-4" data-testid="order-success">
          <i className="bi bi-bag-check-fill" />
          <div>
            <h2 className="h4 fw-bold mb-1">Thank you! Your order is placed.</h2>
            <p className="mb-0">We'll let you know as it moves along. You can cancel any time before it ships.</p>
          </div>
        </div>
      )}

      <div className="d-flex flex-wrap align-items-end justify-content-between gap-3 mb-4">
        <div>
          <Link to="/orders" className="small text-muted text-decoration-none">
            <i className="bi bi-arrow-left me-1" />My orders
          </Link>
          <h1 className="h2 fw-bold mb-0 mt-1" data-testid="order-title">Order #{order.id}</h1>
          <div className="text-muted small">Placed {formatDate(order.createdAt)}</div>
        </div>
        <StatusBadge status={order.status} />
      </div>

      {/* ---------- Progress tracker ---------- */}
      {!cancelled ? (
        <div className="cart-panel p-4 mb-4">
          <ol className="tracker" aria-label="Order progress">
            {STATUS_FLOW.map((s, i) => (
              <li key={s} className={i <= reached ? 'done' : ''} aria-current={i === reached ? 'step' : undefined}>
                <span className="dot"><i className={`bi ${STATUS_INFO[s].icon}`} /></span>
                <span className="label">{STATUS_INFO[s].label}</span>
              </li>
            ))}
          </ol>
        </div>
      ) : (
        <div className="alert alert-secondary mb-4">
          <i className="bi bi-x-circle me-2" />This order was cancelled and the items were returned to stock.
        </div>
      )}

      <div className="row g-4">
        <div className="col-lg-8">
          <div className="cart-panel">
            {order.items.map((item) => (
              <div key={item.productId} className="cart-line align-items-center" data-testid="order-item">
                <div className="flex-grow-1">
                  <Link to={`/products/${item.productId}`} className="fw-semibold text-reset text-decoration-none">
                    {item.productName}
                  </Link>
                  <div className="small text-muted">{item.quantity} × {formatPrice(item.unitPrice)}</div>
                </div>
                <div className="price">{formatPrice(item.lineTotal)}</div>
              </div>
            ))}
          </div>
        </div>

        <div className="col-lg-4">
          <div className="cart-panel p-4">
            <h2 className="h6 text-uppercase text-muted fw-semibold">Delivery to</h2>
            <p className="mb-3">{order.shippingAddress}</p>
            <h2 className="h6 text-uppercase text-muted fw-semibold">Payment</h2>
            <p className="mb-3">Cash on delivery</p>
            <hr />
            <div className="d-flex justify-content-between align-items-baseline">
              <span className="fw-semibold">Total</span>
              <span className="price fs-4" data-testid="order-total">{formatPrice(order.totalAmount)}</span>
            </div>
            {canCancel(order.status) && (
              <button className="btn btn-outline-danger w-100 mt-4 rounded-pill" onClick={() => setConfirmCancel(true)}
                data-testid="cancel-order">
                Cancel order
              </button>
            )}
          </div>
        </div>
      </div>

      <Modal
        show={confirmCancel}
        title="Cancel this order?"
        onClose={() => setConfirmCancel(false)}
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setConfirmCancel(false)} disabled={cancelling}>
              Keep order
            </button>
            <button className="btn btn-danger rounded-pill" onClick={handleCancel} disabled={cancelling}
              data-testid="confirm-cancel">
              {cancelling && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}
              Yes, cancel it
            </button>
          </>
        }
      >
        Order #{order.id} for {formatPrice(order.totalAmount)} will be cancelled and the items put back in stock.
        This can't be undone.
      </Modal>
    </div>
  );
}
