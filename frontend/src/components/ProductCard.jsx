import { Link } from 'react-router-dom';
import ProductImage from './ProductImage.jsx';
import StockBadge from './StockBadge.jsx';
import { formatPrice } from '../utils/format';

/** One product tile in a grid. "Props" = the data passed in by the parent. */
export default function ProductCard({ product, onQuickView }) {
  return (
    <article className="product-card" data-testid="product-card">
      <div className="position-relative">
        <Link to={`/products/${product.id}`} className="card-link d-block" tabIndex={-1} aria-hidden="true">
          <ProductImage product={product}>
            <span className="badge-corner">
              <StockBadge stock={product.stock} />
            </span>
          </ProductImage>
        </Link>
        {onQuickView && (
          <button
            type="button"
            className="btn quick-view-btn"
            onClick={() => onQuickView(product)}
            data-testid="quick-view-button"
          >
            <i className="bi bi-eye me-1" />Quick view
          </button>
        )}
      </div>

      <Link to={`/products/${product.id}`} className="card-link p-3 d-flex flex-column flex-grow-1">
        <div className="anime mb-1">{product.animeSeries} · {product.categoryName}</div>
        <h3 className="title mb-2" data-testid="product-name">{product.name}</h3>
        <div className="mt-auto d-flex align-items-center justify-content-between">
          <span className="price" data-testid="product-price">{formatPrice(product.price)}</span>
          <span className="small text-muted">
            View <i className="bi bi-arrow-right" />
          </span>
        </div>
      </Link>
    </article>
  );
}
