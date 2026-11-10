import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import {
  addToCartRequest,
  clearCartRequest,
  fetchCart,
  removeCartItemRequest,
  updateCartItemRequest,
} from '../api/cart';
import { useAuth } from './AuthContext.jsx';

/**
 * The logged-in user's cart, shared with the whole app
 * (the navbar badge, the slide-out cart, the cart page and product pages all read it).
 * The backend is the source of truth: every change returns the full updated cart.
 */
const CartContext = createContext(null);

const EMPTY_CART = { items: [], totalItems: 0, totalAmount: 0 };

export function CartProvider({ children }) {
  const { isLoggedIn } = useAuth();
  const [cart, setCart] = useState(EMPTY_CART);
  const [loading, setLoading] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const refreshCart = useCallback(async () => {
    if (!isLoggedIn) {
      setCart(EMPTY_CART);
      return;
    }
    setLoading(true);
    try {
      setCart(await fetchCart());
    } finally {
      setLoading(false);
    }
  }, [isLoggedIn]);

  // Load the cart when someone logs in; empty it when they log out
  useEffect(() => {
    refreshCart().catch(() => setCart(EMPTY_CART));
    if (!isLoggedIn) setDrawerOpen(false);
  }, [refreshCart, isLoggedIn]);

  const addItem = useCallback(async (productId, quantity = 1) => {
    const updated = await addToCartRequest(productId, quantity);
    setCart(updated);
    return updated;
  }, []);

  const updateQuantity = useCallback(async (productId, quantity) => {
    setCart(await updateCartItemRequest(productId, quantity));
  }, []);

  const removeItem = useCallback(async (productId) => {
    setCart(await removeCartItemRequest(productId));
  }, []);

  const clearCart = useCallback(async () => {
    await clearCartRequest();
    setCart(EMPTY_CART);
  }, []);

  const value = useMemo(
    () => ({
      cart,
      itemCount: cart.totalItems,
      loading,
      refreshCart,
      addItem,
      updateQuantity,
      removeItem,
      clearCart,
      drawerOpen,
      openDrawer: () => setDrawerOpen(true),
      closeDrawer: () => setDrawerOpen(false),
    }),
    [cart, loading, refreshCart, addItem, updateQuantity, removeItem, clearCart, drawerOpen],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) throw new Error('useCart must be used inside <CartProvider>');
  return context;
}
