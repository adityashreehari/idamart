import { useState } from 'react';

/**
 * Square product picture.
 * If there's no image, or it fails to load, show the product name on a dark tile instead.
 */
export default function ProductImage({ product, className = '', children }) {
  const [failed, setFailed] = useState(false);
  const showImage = product.imageUrl && !failed;
  const outOfStock = product.stock === 0;

  return (
    <div className={`product-image ${outOfStock ? 'out-of-stock' : ''} ${className}`}>
      {showImage ? (
        <img src={product.imageUrl} alt={product.name} loading="lazy" onError={() => setFailed(true)} />
      ) : (
        <div className="fallback">{product.name}</div>
      )}
      {children}
    </div>
  );
}
