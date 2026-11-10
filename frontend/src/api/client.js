import axios from 'axios';

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
export const TOKEN_KEY = 'idamart_token';
export const LOGOUT_EVENT = 'idamart:logout';

/**
 * One shared axios instance for every request to the backend.
 * Any page can do:  api.get('/api/products')
 */
const api = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
  // Generous timeout: a sleeping free-tier server can take up to a minute to wake up
  timeout: 60000,
});

/**
 * REQUEST interceptor: runs before EVERY request.
 * If the user is logged in, attach their JWT
 * (the same thing you did by hand in Postman's Authorization tab).
 */
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * RESPONSE interceptor: runs after EVERY response.
 * If we sent a token but the server says 401, the token has expired or is invalid,
 * so tell the app to log the user out (AuthContext listens for this event).
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const sentToken = Boolean(error.config?.headers?.Authorization);
    const isLoginCall = error.config?.url?.startsWith('/api/auth/login');
    if (error.response?.status === 401 && sentToken && !isLoginCall) {
      localStorage.removeItem(TOKEN_KEY);
      window.dispatchEvent(new Event(LOGOUT_EVENT));
    }
    return Promise.reject(error);
  },
);

export default api;
