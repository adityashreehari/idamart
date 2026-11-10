import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { fetchMe, loginRequest, registerRequest } from '../api/auth';
import { LOGOUT_EVENT, TOKEN_KEY } from '../api/client';

/**
 * Who is logged in, shared with the whole app.
 *
 * - The JWT is kept in localStorage so a page refresh doesn't log you out.
 * - On start-up, if a token exists we ask the backend "who am I?" (/api/auth/me)
 *   to check it's still valid and get the user's details.
 */
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);

  // Restore the session on page load
  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      setInitializing(false);
      return;
    }
    fetchMe()
      .then(setUser)
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setInitializing(false));
  }, []);

  // If any request comes back 401 (expired token), the axios interceptor fires this event
  useEffect(() => {
    const handleForcedLogout = () => setUser(null);
    window.addEventListener(LOGOUT_EVENT, handleForcedLogout);
    return () => window.removeEventListener(LOGOUT_EVENT, handleForcedLogout);
  }, []);

  const saveSession = useCallback((authResponse) => {
    localStorage.setItem(TOKEN_KEY, authResponse.token);
    setUser(authResponse.user);
    return authResponse.user;
  }, []);

  const login = useCallback(
    async (email, password) => saveSession(await loginRequest(email, password)),
    [saveSession],
  );

  const register = useCallback(
    async (fullName, email, password) => saveSession(await registerRequest(fullName, email, password)),
    [saveSession],
  );

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      isLoggedIn: Boolean(user),
      isAdmin: user?.role === 'ADMIN',
      initializing,
      login,
      register,
      logout,
    }),
    [user, initializing, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside <AuthProvider>');
  return context;
}
