import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="ida-footer mt-5 pt-5 pb-4">
      <div className="container">
        <div className="row g-4">
          <div className="col-md-5">
            <div className="brand mb-2">Ida<span>Mart</span></div>
            <p className="small mb-0">
              Figures, apparel, manga and collectibles from the series you love.
              A portfolio project built with Spring Boot, MySQL and React.
            </p>
          </div>
          <div className="col-6 col-md-3">
            <h6 className="text-white">Shop</h6>
            <ul className="list-unstyled small">
              <li><Link to="/products">All products</Link></li>
              <li><Link to="/products?sort=price_asc">Lowest price</Link></li>
              <li><Link to="/products?sort=newest">New arrivals</Link></li>
            </ul>
          </div>
          <div className="col-6 col-md-4">
            <h6 className="text-white">Good to know</h6>
            <ul className="list-unstyled small">
              <li><i className="bi bi-truck me-2" />Delivery across the UAE</li>
              <li><i className="bi bi-arrow-repeat me-2" />Cancel any time before shipping</li>
              <li><i className="bi bi-shield-check me-2" />Secure login with JWT</li>
            </ul>
          </div>
        </div>
        <hr className="border-secondary my-4" />
        <p className="small mb-0 text-center">
          © {new Date().getFullYear()} IdaMart · Demo store — product names and images are placeholders.
        </p>
      </div>
    </footer>
  );
}
