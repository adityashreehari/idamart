import api from './client';

// Every call returns the whole updated cart: { items, totalItems, totalAmount }

export async function fetchCart() {
  const { data } = await api.get('/api/cart');
  return data;
}

export async function addToCartRequest(productId, quantity) {
  const { data } = await api.post('/api/cart/items', { productId, quantity });
  return data;
}

export async function updateCartItemRequest(productId, quantity) {
  const { data } = await api.put(`/api/cart/items/${productId}`, { quantity });
  return data;
}

export async function removeCartItemRequest(productId) {
  const { data } = await api.delete(`/api/cart/items/${productId}`);
  return data;
}

export async function clearCartRequest() {
  await api.delete('/api/cart');
}
