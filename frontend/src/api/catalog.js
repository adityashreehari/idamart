import api from './client';

/**
 * All calls for browsing the shop (public, no login needed).
 * Each function returns the JSON body directly.
 */

export async function fetchProducts({ keyword, categoryId, anime, sort, page = 0, size = 12 } = {}) {
  const params = { page, size };
  if (keyword) params.keyword = keyword;
  if (categoryId) params.categoryId = categoryId;
  if (anime) params.anime = anime;
  if (sort) params.sort = sort;

  const { data } = await api.get('/api/products', { params });
  return data; // { content, page, size, totalElements, totalPages, last }
}

export async function fetchProduct(id) {
  const { data } = await api.get(`/api/products/${id}`);
  return data;
}

export async function fetchCategories() {
  const { data } = await api.get('/api/categories');
  return data;
}

export async function fetchAnimeSeries() {
  const { data } = await api.get('/api/products/anime-series');
  return data;
}
