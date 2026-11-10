import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import ProductImage from '../components/ProductImage.jsx';
import ProductGrid from '../components/ProductGrid.jsx';
import StockBadge from '../components/StockBadge.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
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

          <div className="d-flex flex-wrap align-items-center gap-3 mb-4">
            <div className="input-group qty-group">
              <button
                className="btn btn-outline-secondary"
                type="button"
                onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                disabled={outOfStock || quantity <= 1}
                aria-label="Decrease quantity"
              >
                <i className="bi bi-dash" />
              </button>
              <input
                className="form-control"
                value={outOfStock ? 0 : quantity}
                readOnly
                aria-label="Quantity"
                data-testid="quantity"
              />
              <button
                className="btn btn-outline-secondary"
                type="button"
                onClick={() => setQuantity((q) => Math.min(maxQty, q + 1))}
                disabled={outOfStock || quantity >= maxQty}
                aria-label="Increase quantity"
              >
                <i className="bi bi-plus" />
              </button>
            </div>

            {/* Session 7 connects this button to the cart */}
            <button className="btn btn-ida btn-lg flex-grow-1" disabled data-testid="add-to-cart">
              <i className="bi bi-bag-plus me-2" />
              {outOfStock ? 'Out of stock' : 'Add to cart'}
            </button>
          </div>
          {!outOfStock && (
            <p className="small text-muted mb-4">
              <i className="bi bi-info-circle me-1" />
              Login and cart are coming in the next update.
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
