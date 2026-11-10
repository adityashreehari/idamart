import { useCallback, useEffect, useState } from 'react';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import Modal from '../../components/Modal.jsx';
import Pagination from '../../components/Pagination.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { fetchAdminOrders, updateOrderStatusRequest } from '../../api/admin';
import { formatPrice, getErrorMessage } from '../../utils/format';
import { STATUS_INFO, canCancel, formatDate, nextStatus } from '../../utils/orderStatus';

const FILTERS = ['', 'PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

export default function AdminOrders() {
  const { showToast } = useToast();
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [busyId, setBusyId] = useState(null);
  const [viewing, setViewing] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setResult(await fetchAdminOrders({ status, page, size: 10 }));
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [status, page]);

  useEffect(() => {
    load();
  }, [load]);

  async function changeStatus(order, newStatus) {
    if (newStatus === 'CANCELLED' && !window.confirm(`Cancel order #${order.id}? Stock will be restored.`)) return;
    setBusyId(order.id);
    try {
      const updated = await updateOrderStatusRequest(order.id, newStatus);
      showToast(`Order #${order.id} is now ${STATUS_INFO[updated.status].label.toLowerCase()}`);
      setResult((r) => ({ ...r, content: r.content.map((o) => (o.id === updated.id ? updated : o)) }));
      if (viewing?.id === updated.id) setViewing(updated);
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setBusyId(null);
    }
  }

  const orders = result?.content ?? [];

  return (
    <>
      <div className="d-flex flex-wrap gap-2 mb-3" role="group" aria-label="Filter orders by status">
        {FILTERS.map((s) => (
          <button
            key={s || 'all'}
            className={`chip ${status === s ? 'active' : ''}`}
            onClick={() => {
              setStatus(s);
              setPage(0);
            }}
            data-testid={`filter-${s || 'all'}`}
          >
            {s ? STATUS_INFO[s].label : 'All'}
          </button>
        ))}
      </div>

      {error ? (
        <ErrorMessage message={error} onRetry={load} />
      ) : (
        <div className="cart-panel">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0 admin-table">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Customer</th>
                  <th className="text-end">Total</th>
                  <th>Status</th>
                  <th className="text-end">Actions</th>
                </tr>
              </thead>
              <tbody className={loading ? 'opacity-50' : ''}>
                {orders.length === 0 && !loading && (
                  <tr><td colSpan="5" className="text-center text-muted py-4">No orders here.</td></tr>
                )}
                {orders.map((o) => {
                  const next = nextStatus(o.status);
                  const busy = busyId === o.id;
                  return (
                    <tr key={o.id} data-testid="admin-order-row">
                      <td>
                        <button className="btn btn-link p-0 fw-semibold" onClick={() => setViewing(o)}>#{o.id}</button>
                        <div className="small text-muted">{formatDate(o.createdAt)}</div>
                      </td>
                      <td>
                        <div>{o.customerName}</div>
                        <div className="small text-muted">{o.customerEmail}</div>
                      </td>
                      <td className="text-end fw-semibold">{formatPrice(o.totalAmount)}</td>
                      <td><StatusBadge status={o.status} /></td>
                      <td className="text-end text-nowrap">
                        {next && (
                          <button className="btn btn-sm btn-ida me-1" disabled={busy} onClick={() => changeStatus(o, next)}
                            data-testid="advance-order">
                            {busy ? <span className="spinner-border spinner-border-sm" aria-hidden="true" />
                              : <>Mark {STATUS_INFO[next].label.toLowerCase()}</>}
                          </button>
                        )}
                        {canCancel(o.status) && (
                          <button className="btn btn-sm btn-outline-danger rounded-pill" disabled={busy}
                            onClick={() => changeStatus(o, 'CANCELLED')} aria-label={`Cancel order ${o.id}`}>
                            <i className="bi bi-x-lg" />
                          </button>
                        )}
                        {!next && !canCancel(o.status) && <span className="small text-muted">Done</span>}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
      {result && <Pagination page={result.page} totalPages={result.totalPages} onChange={setPage} />}

      <Modal show={Boolean(viewing)} title={viewing ? `Order #${viewing.id}` : ''} onClose={() => setViewing(null)}>
        {viewing && (
          <>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <StatusBadge status={viewing.status} />
              <span className="small text-muted">{formatDate(viewing.createdAt)}</span>
            </div>
            <p className="mb-1 fw-semibold">{viewing.customerName}</p>
            <p className="small text-muted">{viewing.customerEmail}<br />{viewing.shippingAddress}</p>
            <ul className="list-group list-group-flush mb-3">
              {viewing.items.map((i) => (
                <li key={i.productId} className="list-group-item d-flex justify-content-between px-0">
                  <span>{i.quantity} × {i.productName}</span>
                  <span>{formatPrice(i.lineTotal)}</span>
                </li>
              ))}
            </ul>
            <div className="d-flex justify-content-between fw-bold">
              <span>Total</span><span className="price">{formatPrice(viewing.totalAmount)}</span>
            </div>
          </>
        )}
      </Modal>
    </>
  );
}
