import axios from 'axios';

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
export const TOKEN_KEY = 'idamart_token';

/**
 * One shared axios instance for every request to the backend.
 * Any page can do:  api.get('/api/products')
 */
const api = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

/**
 * Interceptor = code that runs before EVERY request.
 * If the user is logged in, attach their JWT so protected endpoints work
 * (the same thing you did by hand in Postman's Authorization tab).
 */
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
