import api from './client';

/** Everything the admin dashboard calls. The backend checks the ADMIN role on each one. */

export async function fetchAdminStats() {
  const { data } = await api.get('/api/admin/stats');
  return data;
}

export async function fetchAdminOrders({ status, page = 0, size = 10 } = {}) {
  const params = { page, size };
  if (status) params.status = status;
  const { data } = await api.get('/api/admin/orders', { params });
  return data;
}

export async function updateOrderStatusRequest(id, status) {
  const { data } = await api.patch(`/api/admin/orders/${id}/status`, { status });
  return data;
}

export async function createProductRequest(product) {
  const { data } = await api.post('/api/products', product);
  return data;
}

export async function updateProductRequest(id, product) {
  const { data } = await api.put(`/api/products/${id}`, product);
  return data;
}

export async function deleteProductRequest(id) {
  await api.delete(`/api/products/${id}`);
}

export async function createCategoryRequest(category) {
  const { data } = await api.post('/api/categories', category);
  return data;
}
