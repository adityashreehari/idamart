import { STATUS_INFO } from '../utils/orderStatus';

export default function StatusBadge({ status }) {
  const info = STATUS_INFO[status] || { label: status, badge: 'text-bg-light', icon: 'bi-circle' };
  return (
    <span className={`badge rounded-pill ${info.badge} status-badge`} data-testid="order-status">
      <i className={`bi ${info.icon} me-1`} />
      {info.label}
    </span>
  );
}
