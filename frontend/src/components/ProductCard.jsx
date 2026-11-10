import { Link } from 'react-router-dom';
import ProductImage from './ProductImage.jsx';
import StockBadge from './StockBadge.jsx';
import { formatPrice } from '../utils/format';

/** One product tile in a grid. "Props" = the data passed in by the parent. */
export default function ProductCard({ product }) {
  return (
    <article className="product-card" data-testid="product-card">
      <Link to={`/products/${product.id}`} className="card-link d-flex flex-column h-100">
        <ProductImage product={product}>
          <span className="badge-corner">
            <StockBadge stock={product.stock} />
          </span>
        </ProductImage>

        <div className="p-3 d-flex flex-column flex-grow-1">
          <div className="anime mb-1">{product.animeSeries} · {product.categoryName}</div>
          <h3 className="title mb-2" data-testid="product-name">{product.name}</h3>
          <div className="mt-auto d-flex align-items-center justify-content-between">
            <span className="price" data-testid="product-price">{formatPrice(product.price)}</span>
            <span className="small text-muted">
              View <i className="bi bi-arrow-right" />
            </span>
          </div>
        </div>
      </Link>
    </article>
  );
}
