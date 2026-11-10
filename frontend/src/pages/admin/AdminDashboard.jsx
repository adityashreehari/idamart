import { useCallback, useEffect, useState } from 'react';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { fetchAdminOrders, fetchAdminStats } from '../../api/admin';
import { fetchProducts } from '../../api/catalog';
import { formatPrice, getErrorMessage, LOW_STOCK_LIMIT } from '../../utils/format';
import { formatDate } from '../../utils/orderStatus';

export default function AdminDashboard({ onNavigate }) {
  const [stats, setStats] = useState(null);
  const [recentOrders, setRecentOrders] = useState([]);
  const [lowStock, setLowStock] = useState([]);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setError('');
    try {
      const [s, orders, products] = await Promise.all([
        fetchAdminStats(),
        fetchAdminOrders({ size: 5 }),
        fetchProducts({ sort: 'name', size: 50 }),
      ]);
      setStats(s);
      setRecentOrders(orders.content);
      setLowStock(products.content.filter((p) => p.stock <= LOW_STOCK_LIMIT).sort((a, b) => a.stock - b.stock));
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  if (error) return <ErrorMessage message={error} onRetry={load} />;

  const cards = [
    { label: 'Revenue', value: stats ? formatPrice(stats.revenue) : '–', icon: 'bi-cash-stack', note: 'excluding cancelled' },
    { label: 'Orders', value: stats?.totalOrders ?? '–', icon: 'bi-receipt', note: `${stats?.ordersToProcess ?? 0} to process`, tab: 'orders' },
    { label: 'Customers', value: stats?.customers ?? '–', icon: 'bi-people', note: 'registered accounts' },
    { label: 'Products', value: stats?.activeProducts ?? '–', icon: 'bi-box-seam',
      note: `${stats?.lowStockProducts ?? 0} low · ${stats?.outOfStockProducts ?? 0} out`, tab: 'products' },
  ];

  return (
    <>
      <div className="row g-3 mb-4">
        {cards.map((c) => (
          <div className="col-6 col-lg-3" key={c.label}>
            <button
              type="button"
              className="stat-card w-100 text-start"
              onClick={() => c.tab && onNavigate(c.tab)}
              disabled={!c.tab}
              data-testid={`stat-${c.label.toLowerCase()}`}
            >
              <i className={`bi ${c.icon}`} />
              <div className="small text-muted mt-2">{c.label}</div>
              <div className="stat-value">{c.value}</div>
              <div className="small text-muted">{c.note}</div>
            </button>
          </div>
        ))}
      </div>

      <div className="row g-4">
        <div className="col-lg-7">
          <div className="cart-panel p-4 h-100">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h2 className="h5 fw-bold mb-0">Recent orders</h2>
              <button className="btn btn-ghost btn-sm" onClick={() => onNavigate('orders')}>All orders</button>
            </div>
            {recentOrders.length === 0 ? (
              <p className="text-muted mb-0">No orders yet.</p>
            ) : (
              <div className="table-responsive">
                <table className="table align-middle mb-0">
                  <tbody>
                    {recentOrders.map((o) => (
                      <tr key={o.id}>
                        <td className="fw-semibold">#{o.id}</td>
                        <td>
                          <div>{o.customerName}</div>
                          <div className="small text-muted">{formatDate(o.createdAt)}</div>
                        </td>
                        <td><StatusBadge status={o.status} /></td>
                        <td className="text-end price">{formatPrice(o.totalAmount)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        <div className="col-lg-5">
          <div className="cart-panel p-4 h-100">
            <h2 className="h5 fw-bold mb-3">
              <i className="bi bi-exclamation-triangle text-warning me-2" />Low stock
            </h2>
            {lowStock.length === 0 ? (
              <p className="text-muted mb-0">Everything is well stocked.</p>
            ) : (
              <ul className="list-group list-group-flush">
                {lowStock.map((p) => (
                  <li key={p.id} className="list-group-item d-flex justify-content-between align-items-center px-0">
                    <span className="text-truncate me-2">{p.name}</span>
                    <span className={`badge rounded-pill ${p.stock === 0 ? 'text-bg-secondary' : 'text-bg-warning'}`}>
                      {p.stock === 0 ? 'Out' : `${p.stock} left`}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
