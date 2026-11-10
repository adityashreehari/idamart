import ProductCard from './ProductCard.jsx';

/** Responsive grid: 2 columns on phones, 3 on tablets, 4 on laptops. */
export default function ProductGrid({ products, loading, skeletonCount = 8 }) {
  if (loading) {
    return (
      <div className="row g-3 g-md-4" aria-busy="true">
        {Array.from({ length: skeletonCount }).map((_, i) => (
          <div className="col-6 col-md-4 col-xl-3" key={i}>
            <div className="product-card">
              <div className="skeleton" style={{ aspectRatio: '1 / 1', borderRadius: 0 }} />
              <div className="p-3">
                <div className="skeleton mb-2" style={{ height: 12, width: '50%' }} />
                <div className="skeleton mb-3" style={{ height: 16 }} />
                <div className="skeleton" style={{ height: 18, width: '35%' }} />
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="row g-3 g-md-4">
      {products.map((product) => (
        <div className="col-6 col-md-4 col-xl-3" key={product.id}>
          <ProductCard product={product} />
        </div>
      ))}
    </div>
  );
}
