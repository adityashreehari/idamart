import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import ProductImage from '../components/ProductImage.jsx';
import ProductGrid from '../components/ProductGrid.jsx';
import StockBadge from '../components/StockBadge.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import QuantityStepper from '../components/QuantityStepper.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { fetchProduct, fetchProducts } from '../api/catalog';
import { formatPrice, getErrorMessage } from '../utils/format';

const MAX_PER_ITEM = 10;

export default function ProductDetailPage() {
  const { id } = useParams(); // the :id from /products/:id

  const [product, setProduct] = useState(null);
  const [related, setRelated] = useState([]);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notFound, setNotFound] = useState(false);
  const [adding, setAdding] = useState(false);

  const { isLoggedIn } = useAuth();
  const { cart, addItem, openDrawer } = useCart();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    setNotFound(false);
    setQuantity(1);
    try {
      const data = await fetchProduct(id);
      setProduct(data);

      // "More from this series": same anime, excluding this product
      const more = await fetchProducts({ anime: data.animeSeries, size: 5 });
      setRelated(more.content.filter((p) => p.id !== data.id).slice(0, 4));
    } catch (err) {
      if (err.response?.status === 404 || err.response?.status === 400) setNotFound(true);
      else setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  if (loading) {
    return (
      <div className="container py-5">
        <div className="row g-5">
          <div className="col-md-6"><div className="skeleton" style={{ aspectRatio: '1 / 1', borderRadius: 24 }} /></div>
          <div className="col-md-6">
            <div className="skeleton mb-3" style={{ height: 14, width: '40%' }} />
            <div className="skeleton mb-3" style={{ height: 36 }} />
            <div className="skeleton mb-4" style={{ height: 28, width: '30%' }} />
            <div className="skeleton" style={{ height: 80 }} />
          </div>
        </div>
      </div>
    );
  }

  if (notFound) {
    return (
      <div className="container py-5">
        <div className="empty-state" data-testid="product-not-found">
          <i className="bi bi-question-circle d-block mb-2" />
          <h2 className="h4">Product not found</h2>
          <p className="text-muted">It may have been removed from the shop.</p>
          <Link to="/products" className="btn btn-ida">Back to shop</Link>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container py-5">
        <ErrorMessage message={error} onRetry={load} />
      </div>
    );
  }

  const outOfStock = product.stock <= 0;
  const maxQty = Math.min(product.stock, MAX_PER_ITEM);
  const inCart = cart.items.find((i) => i.productId === product.id)?.quantity ?? 0;

  async function handleAddToCart() {
    if (!isLoggedIn) {
      showToast('Please log in to add items to your cart.', 'info');
      navigate(`/login?redirect=${encodeURIComponent(location.pathname)}`);
      return;
    }
    setAdding(true);
    try {
      await addItem(product.id, quantity);
      showToast(`Added ${quantity} × ${product.name} to your cart`);
      openDrawer();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setAdding(false);
    }
  }

  return (
    <div className="container py-4 py-lg-5">
      <nav aria-label="breadcrumb" className="mb-4">
        <ol className="breadcrumb small">
          <li className="breadcrumb-item"><Link to="/">Home</Link></li>
          <li className="breadcrumb-item"><Link to="/products">Shop</Link></li>
          <li className="breadcrumb-item">
            <Link to={`/products?categoryId=${product.categoryId}`}>{product.categoryName}</Link>
          </li>
          <li className="breadcrumb-item active text-truncate" aria-current="page">{product.name}</li>
        </ol>
      </nav>

      <div className="row g-4 g-lg-5">
        <div className="col-md-6">
          <ProductImage product={product} className="detail-image" />
        </div>

        <div className="col-md-6">
          <Link to={`/products?anime=${encodeURIComponent(product.animeSeries)}`} className="chip mb-3">
            {product.animeSeries}
          </Link>
          <h1 className="h2 fw-bold mb-2" data-testid="detail-name">{product.name}</h1>
          <div className="d-flex align-items-center gap-3 mb-4">
            <span className="price detail-price" data-testid="detail-price">{formatPrice(product.price)}</span>
            <StockBadge stock={product.stock} />
          </div>

          <p className="text-muted mb-4">{product.description}</p>

          <div className="d-flex flex-wrap align-items-center gap-3 mb-3">
            <QuantityStepper
              value={outOfStock ? 0 : quantity}
              max={maxQty}
              onChange={setQuantity}
              disabled={outOfStock || adding}
              testId="quantity"
            />
            <button
              className="btn btn-ida btn-lg flex-grow-1"
              onClick={handleAddToCart}
              disabled={outOfStock || adding}
              data-testid="add-to-cart"
            >
              {adding ? (
                <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />
              ) : (
                <i className="bi bi-bag-plus me-2" />
              )}
              {outOfStock ? 'Out of stock' : 'Add to cart'}
            </button>
          </div>
          {inCart > 0 && (
            <p className="small text-muted mb-4" data-testid="in-cart-note">
              <i className="bi bi-check2-circle me-1 text-success" />
              You already have {inCart} in your cart.
            </p>
          )}

          <ul className="list-unstyled info-list border-top pt-3">
            <li><i className="bi bi-tag" />Category: {product.categoryName}</li>
            <li><i className="bi bi-truck" />Delivered across the UAE</li>
            <li><i className="bi bi-arrow-repeat" />Cancel any time before it ships</li>
          </ul>
        </div>
      </div>

      {related.length > 0 && (
        <section className="mt-5 pt-3">
          <div className="section-title">
            <div>
              <div className="eyebrow">You might also like</div>
              <h2 className="h3">More from {product.animeSeries}</h2>
            </div>
          </div>
          <ProductGrid products={related} />
        </section>
      )}
    </div>
  );
}
