import { useSearchParams } from 'react-router-dom';
import AdminDashboard from './AdminDashboard.jsx';
import AdminProducts from './AdminProducts.jsx';
import AdminOrders from './AdminOrders.jsx';

const TABS = [
  { id: 'dashboard', label: 'Dashboard', icon: 'bi-speedometer2' },
  { id: 'products', label: 'Products', icon: 'bi-box-seam' },
  { id: 'orders', label: 'Orders', icon: 'bi-receipt' },
];

/**
 * The manager's office. Only ADMIN users can open it
 * (ProtectedRoute adminOnly in the browser, and the backend checks the role on every call).
 * The active tab lives in the URL (?tab=products) so a refresh keeps you on the same tab.
 */
export default function AdminPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const tab = TABS.some((t) => t.id === searchParams.get('tab')) ? searchParams.get('tab') : 'dashboard';

  return (
    <div className="container py-4 py-lg-5">
      <div className="d-flex flex-wrap align-items-end justify-content-between gap-3 mb-4">
        <div>
          <div className="eyebrow">Admin</div>
          <h1 className="h2 fw-bold mb-0">Store manager</h1>
        </div>
      </div>

      <ul className="nav nav-pills admin-tabs mb-4" role="tablist">
        {TABS.map((t) => (
          <li className="nav-item" key={t.id} role="presentation">
            <button
              type="button"
              role="tab"
              aria-selected={tab === t.id}
              className={`nav-link ${tab === t.id ? 'active' : ''}`}
              onClick={() => setSearchParams(t.id === 'dashboard' ? {} : { tab: t.id })}
              data-testid={`admin-tab-${t.id}`}
            >
              <i className={`bi ${t.icon} me-2`} />{t.label}
            </button>
          </li>
        ))}
      </ul>

      {tab === 'dashboard' && <AdminDashboard onNavigate={(id) => setSearchParams({ tab: id })} />}
      {tab === 'products' && <AdminProducts />}
      {tab === 'orders' && <AdminOrders />}
    </div>
  );
}
