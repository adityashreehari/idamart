import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Pagination from '../components/Pagination.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { fetchMyOrders } from '../api/orders';
import { formatPrice, getErrorMessage } from '../utils/format';
import { formatDate } from '../utils/orderStatus';

export default function OrdersPage() {
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setResult(await fetchMyOrders(page, 10));
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  const orders = result?.content ?? [];

  return (
    <div className="container py-4 py-lg-5">
      <div className="mb-4">
        <div className="eyebrow">Account</div>
        <h1 className="h2 fw-bold mb-0">My orders</h1>
      </div>

      {error ? (
        <ErrorMessage message={error} onRetry={load} />
      ) : loading ? (
        <div className="text-center py-5">
          <div className="spinner-border" role="status" style={{ color: 'var(--ida-accent)' }}>
            <span className="visually-hidden">Loading orders…</span>
          </div>
        </div>
      ) : orders.length === 0 ? (
        <div className="empty-state" data-testid="no-orders">
          <i className="bi bi-receipt d-block mb-2" />
          <p className="mb-3">You haven't placed any orders yet.</p>
          <Link to="/products" className="btn btn-ida">Start shopping</Link>
        </div>
      ) : (
        <>
          <div className="d-flex flex-column gap-3">
            {orders.map((order) => {
              const count = order.items.reduce((sum, i) => sum + i.quantity, 0);
              return (
                <Link key={order.id} to={`/orders/${order.id}`} className="order-row" data-testid="order-row">
                  <div className="order-icon"><i className="bi bi-box-seam" /></div>
                  <div className="flex-grow-1 min-w-0">
                    <div className="fw-bold">Order #{order.id}</div>
                    <div className="small text-muted">
                      {formatDate(order.createdAt)} · {count} {count === 1 ? 'item' : 'items'}
                    </div>
                    <div className="small text-muted text-truncate">
                      {order.items.map((i) => i.productName).join(', ')}
                    </div>
                  </div>
                  <div className="text-end">
                    <StatusBadge status={order.status} />
                    <div className="price mt-1">{formatPrice(order.totalAmount)}</div>
                  </div>
                  <i className="bi bi-chevron-right text-muted d-none d-sm-block" />
                </Link>
              );
            })}
          </div>
          <Pagination page={result.page} totalPages={result.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}
