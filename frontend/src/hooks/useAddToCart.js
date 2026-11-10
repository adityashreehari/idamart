import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { getErrorMessage } from '../utils/format';

/**
 * "Add to cart" in one place, shared by the product page and the quick-view pop-up:
 * not logged in -> go to login (and come back); otherwise add, show a toast, open the drawer.
 */
export default function useAddToCart() {
  const { isLoggedIn } = useAuth();
  const { addItem, openDrawer } = useCart();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [adding, setAdding] = useState(false);

  async function addToCart(product, quantity, { onAdded } = {}) {
    if (!isLoggedIn) {
      showToast('Please log in to add items to your cart.', 'info');
      navigate(`/login?redirect=${encodeURIComponent(location.pathname + location.search)}`);
      return;
    }
    setAdding(true);
    try {
      await addItem(product.id, quantity);
      showToast(`Added ${quantity} × ${product.name} to your cart`);
      onAdded?.();
      openDrawer();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setAdding(false);
    }
  }

  return { addToCart, adding };
}
