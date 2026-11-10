import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import ProductGrid from '../components/ProductGrid.jsx';
import Pagination from '../components/Pagination.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import useDebounce from '../hooks/useDebounce';
import { fetchAnimeSeries, fetchCategories, fetchProducts } from '../api/catalog';
import { getErrorMessage } from '../utils/format';

const SORT_OPTIONS = [
  { value: 'newest', label: 'Newest' },
  { value: 'price_asc', label: 'Price: low to high' },
  { value: 'price_desc', label: 'Price: high to low' },
  { value: 'name', label: 'Name: A to Z' },
];

/**
 * The shop page.
 * All filters live in the URL (?keyword=…&categoryId=…&anime=…&sort=…&page=…)
 * so a filtered page can be bookmarked, shared, and survives a refresh.
 */
export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const keyword = searchParams.get('keyword') || '';
  const categoryId = searchParams.get('categoryId') || '';
  const anime = searchParams.get('anime') || '';
  const sort = searchParams.get('sort') || 'newest';
  const page = Number(searchParams.get('page') || 0);

  const [keywordInput, setKeywordInput] = useState(keyword);
  const debouncedKeyword = useDebounce(keywordInput, 400);

  const [result, setResult] = useState(null);
  const [categories, setCategories] = useState([]);
  const [animeSeries, setAnimeSeries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  /** Change one or more filters in the URL. Any filter change goes back to page 0. */
  const updateParams = useCallback(
    (changes, { resetPage = true } = {}) => {
      const next = new URLSearchParams(searchParams);
      Object.entries(changes).forEach(([key, value]) => {
        if (value === '' || value === null || value === undefined) next.delete(key);
        else next.set(key, value);
      });
      if (resetPage) next.delete('page');
      setSearchParams(next);
    },
    [searchParams, setSearchParams],
  );

  // Filter dropdown options (load once)
  useEffect(() => {
    Promise.all([fetchCategories(), fetchAnimeSeries()])
      .then(([cats, anime]) => {
        setCategories(cats);
        setAnimeSeries(anime);
      })
      .catch(() => {}); // the product list below shows the error message
  }, []);

  // Typing in the search box updates the URL after a short pause
  useEffect(() => {
    if (debouncedKeyword.trim() !== keyword) {
      updateParams({ keyword: debouncedKeyword.trim() });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedKeyword]);

  // If the URL keyword changes from outside (navbar search), update the box
  useEffect(() => {
    setKeywordInput(keyword);
  }, [keyword]);

  // Fetch products whenever the URL filters change
  const loadProducts = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchProducts({ keyword, categoryId, anime, sort, page, size: 12 });
      setResult(data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [keyword, categoryId, anime, sort, page]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  const hasFilters = keyword || categoryId || anime || sort !== 'newest';
  const products = result?.content ?? [];

  function goToPage(p) {
    updateParams({ page: p > 0 ? p : '' }, { resetPage: false });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  return (
    <div className="container py-4 py-lg-5">
      <div className="d-flex flex-wrap align-items-end justify-content-between gap-3 mb-4">
        <div>
          <div className="eyebrow">Shop</div>
          <h1 className="h2 fw-bold mb-0">All products</h1>
        </div>
        {result && !loading && (
          <span className="text-muted" data-testid="result-count">
            {result.totalElements} {result.totalElements === 1 ? 'product' : 'products'} found
          </span>
        )}
      </div>

      <div className="row g-4">
        {/* ---------- Filters ---------- */}
        <aside className="col-lg-3">
          <div className="filter-panel position-sticky" style={{ top: '6rem' }}>
            <div className="mb-3">
              <label htmlFor="keyword" className="form-label">Search</label>
              <input
                id="keyword"
                type="search"
                className="form-control"
                placeholder="e.g. Luffy, hoodie"
                value={keywordInput}
                onChange={(e) => setKeywordInput(e.target.value)}
                data-testid="filter-keyword"
              />
            </div>

            <div className="mb-3">
              <label htmlFor="category" className="form-label">Category</label>
              <select
                id="category"
                className="form-select"
                value={categoryId}
                onChange={(e) => updateParams({ categoryId: e.target.value })}
                data-testid="filter-category"
              >
                <option value="">All categories</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>

            <div className="mb-3">
              <label htmlFor="anime" className="form-label">Anime series</label>
              <select
                id="anime"
                className="form-select"
                value={anime}
                onChange={(e) => updateParams({ anime: e.target.value })}
                data-testid="filter-anime"
              >
                <option value="">All series</option>
                {animeSeries.map((a) => (
                  <option key={a} value={a}>{a}</option>
                ))}
              </select>
            </div>

            <div className="mb-3">
              <label htmlFor="sort" className="form-label">Sort by</label>
              <select
                id="sort"
                className="form-select"
                value={sort}
                onChange={(e) => updateParams({ sort: e.target.value === 'newest' ? '' : e.target.value })}
                data-testid="filter-sort"
              >
                {SORT_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>

            {hasFilters && (
              <button
                className="btn btn-ghost w-100"
                onClick={() => {
                  setKeywordInput('');
                  setSearchParams({});
                }}
                data-testid="clear-filters"
              >
                Clear filters
              </button>
            )}
          </div>
        </aside>

        {/* ---------- Results ---------- */}
        <section className="col-lg-9">
          {error ? (
            <ErrorMessage message={error} onRetry={loadProducts} />
          ) : !loading && products.length === 0 ? (
            <div className="empty-state" data-testid="no-results">
              <i className="bi bi-search d-block mb-2" />
              <p className="mb-3">No products match your filters.</p>
              <button className="btn btn-ida" onClick={() => setSearchParams({})}>Show all products</button>
            </div>
          ) : (
            <>
              <ProductGrid products={products} loading={loading} skeletonCount={6} />
              {result && <Pagination page={result.page} totalPages={result.totalPages} onChange={goToPage} />}
            </>
          )}
        </section>
      </div>
    </div>
  );
}
