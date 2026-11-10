import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Modal from './Modal.jsx';
import ProductImage from './ProductImage.jsx';
import QuantityStepper from './QuantityStepper.jsx';
import StockBadge from './StockBadge.jsx';
import useAddToCart from '../hooks/useAddToCart';
import { formatPrice } from '../utils/format';

const MAX_PER_ITEM = 10;

/** Peek at a product and add it to the cart without leaving the grid. */
export default function QuickViewModal({ product, onClose }) {
  const [quantity, setQuantity] = useState(1);
  const { addToCart, adding } = useAddToCart();

  useEffect(() => setQuantity(1), [product?.id]);

  if (!product) return null;
  const outOfStock = product.stock <= 0;

  return (
    <Modal show={Boolean(product)} onClose={onClose} size="lg" testId="quick-view">
      <button type="button" className="btn-close position-absolute top-0 end-0 m-3 z-3" aria-label="Close" onClick={onClose} />
      <div className="row g-4 align-items-center">
        <div className="col-md-6">
          <ProductImage product={product} className="rounded-4" />
        </div>
        <div className="col-md-6">
          <span className="chip mb-3">{product.animeSeries}</span>
          <h2 className="h4 fw-bold mb-2">{product.name}</h2>
          <div className="d-flex align-items-center gap-3 mb-3">
            <span className="price fs-3">{formatPrice(product.price)}</span>
            <StockBadge stock={product.stock} />
          </div>
          <p className="text-muted small mb-4">{product.description}</p>
          <div className="d-flex flex-wrap gap-2 mb-3">
            <QuantityStepper
              value={outOfStock ? 0 : quantity}
              max={Math.min(product.stock, MAX_PER_ITEM)}
              onChange={setQuantity}
              disabled={outOfStock || adding}
            />
            <button
              className="btn btn-ida flex-grow-1"
              disabled={outOfStock || adding}
              onClick={() => addToCart(product, quantity, { onAdded: onClose })}
              data-testid="quick-view-add"
            >
              <i className="bi bi-bag-plus me-2" />
              {outOfStock ? 'Out of stock' : 'Add to cart'}
            </button>
          </div>
          <Link to={`/products/${product.id}`} onClick={onClose} className="small">
            See full details <i className="bi bi-arrow-right" />
          </Link>
        </div>
      </div>
    </Modal>
  );
}
