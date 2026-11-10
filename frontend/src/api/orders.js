import api from './client';

/** Turns the whole cart into an order. */
export async function placeOrderRequest(shippingAddress) {
  const { data } = await api.post('/api/orders', { shippingAddress });
  return data;
}

export async function fetchMyOrders(page = 0, size = 10) {
  const { data } = await api.get('/api/orders', { params: { page, size } });
  return data;
}

export async function fetchMyOrder(id) {
  const { data } = await api.get(`/api/orders/${id}`);
  return data;
}

export async function cancelOrderRequest(id) {
  const { data } = await api.patch(`/api/orders/${id}/cancel`);
  return data;
}
