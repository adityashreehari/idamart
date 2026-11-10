import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import ProductGrid from '../components/ProductGrid.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import { fetchAnimeSeries, fetchCategories, fetchProducts } from '../api/catalog';
import { categoryIcon } from '../utils/categoryIcons';
import { getErrorMessage } from '../utils/format';

export default function HomePage() {
  const [newArrivals, setNewArrivals] = useState([]);
  const [totalProducts, setTotalProducts] = useState(0);
  const [categories, setCategories] = useState([]);
  const [animeSeries, setAnimeSeries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Load everything the home page needs, in parallel
  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [productsPage, cats, anime] = await Promise.all([
        fetchProducts({ sort: 'newest', size: 8 }),
        fetchCategories(),
        fetchAnimeSeries(),
      ]);
      setNewArrivals(productsPage.content);
      setTotalProducts(productsPage.totalElements);
      setCategories(cats);
      setAnimeSeries(anime);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <>
      {/* ---------- Hero ---------- */}
      <section className="hero py-5 mb-5">
        <div className="hero-kanji" aria-hidden="true">店</div>
        <div className="container py-lg-5 position-relative">
          <div className="row align-items-center">
            <div className="col-lg-7">
              <div className="eyebrow mb-3">Anime merch · Delivered in the UAE</div>
              <h1 className="display-4 mb-3">
                Your favourite series, <em>on your shelf.</em>
              </h1>
              <p className="lead mb-4">
                Figures, hoodies, manga and collectibles from Naruto, One Piece,
                Demon Slayer, Jujutsu Kaisen and more.
              </p>
              <div className="d-flex flex-wrap gap-2">
                <Link to="/products" className="btn btn-ida btn-lg" data-testid="shop-now">
                  Shop now <i className="bi bi-arrow-right ms-1" />
                </Link>
                <Link to="/products?sort=price_asc" className="btn btn-outline-light btn-lg rounded-pill">
                  Best prices
                </Link>
              </div>
            </div>
            <div className="col-lg-5 mt-5 mt-lg-0">
              <div className="row text-center g-3">
                <div className="col-4 hero-stat">
                  <strong>{loading ? '–' : totalProducts}</strong>
                  <small>Products</small>
                </div>
                <div className="col-4 hero-stat">
                  <strong>{loading ? '–' : animeSeries.length}</strong>
                  <small>Series</small>
                </div>
                <div className="col-4 hero-stat">
                  <strong>{loading ? '–' : categories.length}</strong>
                  <small>Categories</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="container">
        {error && (
          <div className="mb-5">
            <ErrorMessage message={error} onRetry={load} />
          </div>
        )}

        {/* ---------- Categories ---------- */}
        {!error && (
          <section className="mb-5">
            <div className="section-title">
              <div>
                <div className="eyebrow">Browse</div>
                <h2>Shop by category</h2>
              </div>
            </div>
            <div className="row g-3">
              {categories.map((c) => (
                <div className="col-6 col-md-4 col-lg-2" key={c.id}>
                  <Link to={`/products?categoryId=${c.id}`} className="category-tile" data-testid="category-tile">
                    <i className={`bi ${categoryIcon(c.name)} d-block mb-2`} />
                    <div className="name">{c.name}</div>
                    <div className="small text-muted text-truncate">{c.description}</div>
                  </Link>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* ---------- New arrivals ---------- */}
        {!error && (
          <section className="mb-5">
            <div className="section-title">
              <div>
                <div className="eyebrow">Just landed</div>
                <h2>New arrivals</h2>
              </div>
              <Link to="/products" className="btn btn-ghost btn-sm">View all</Link>
            </div>
            <ProductGrid products={newArrivals} loading={loading} />
          </section>
        )}

        {/* ---------- Anime series ---------- */}
        {!error && animeSeries.length > 0 && (
          <section className="mb-4">
            <div className="section-title">
              <div>
                <div className="eyebrow">Fandoms</div>
                <h2>Shop by series</h2>
              </div>
            </div>
            <div className="d-flex flex-wrap gap-2">
              {animeSeries.map((anime) => (
                <Link key={anime} to={`/products?anime=${encodeURIComponent(anime)}`} className="chip">
                  {anime}
                </Link>
              ))}
            </div>
          </section>
        )}
      </div>
    </>
  );
}
