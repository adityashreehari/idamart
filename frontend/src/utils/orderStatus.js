/** Order statuses, in the order an order moves through them (same rules as the backend enum). */
export const STATUS_FLOW = ['PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED'];

export const STATUS_INFO = {
  PLACED: { label: 'Placed', badge: 'text-bg-primary', icon: 'bi-receipt' },
  CONFIRMED: { label: 'Confirmed', badge: 'text-bg-info', icon: 'bi-check2-circle' },
  SHIPPED: { label: 'Shipped', badge: 'text-bg-warning', icon: 'bi-truck' },
  DELIVERED: { label: 'Delivered', badge: 'text-bg-success', icon: 'bi-house-check' },
  CANCELLED: { label: 'Cancelled', badge: 'text-bg-secondary', icon: 'bi-x-circle' },
};

/** The next step for the admin's "move forward" button (null when the order is finished). */
export function nextStatus(status) {
  const i = STATUS_FLOW.indexOf(status);
  return i >= 0 && i < STATUS_FLOW.length - 1 ? STATUS_FLOW[i + 1] : null;
}

export function canCancel(status) {
  return status === 'PLACED' || status === 'CONFIRMED';
}

export function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString('en-AE', { dateStyle: 'medium', timeStyle: 'short' });
}
