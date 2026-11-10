import api from './client';

export async function loginRequest(email, password) {
  const { data } = await api.post('/api/auth/login', { email, password });
  return data; // { token, tokenType, expiresInMs, user }
}

export async function registerRequest(fullName, email, password) {
  const { data } = await api.post('/api/auth/register', { fullName, email, password });
  return data;
}

export async function fetchMe() {
  const { data } = await api.get('/api/auth/me');
  return data; // { id, fullName, email, role }
}
