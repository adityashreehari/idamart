import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import Modal from '../../components/Modal.jsx';
import Pagination from '../../components/Pagination.jsx';
import ProductImage from '../../components/ProductImage.jsx';
import StockBadge from '../../components/StockBadge.jsx';
import ProductFormModal from './ProductFormModal.jsx';
import useDebounce from '../../hooks/useDebounce';
import { useToast } from '../../context/ToastContext.jsx';
import { fetchAnimeSeries, fetchCategories, fetchProducts } from '../../api/catalog';
import { createCategoryRequest, deleteProductRequest } from '../../api/admin';
import { formatPrice, getErrorMessage } from '../../utils/format';

export default function AdminProducts() {
  const { showToast } = useToast();
  const [keyword, setKeyword] = useState('');
  const debouncedKeyword = useDebounce(keyword, 350);
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [categories, setCategories] = useState([]);
  const [animeSeries, setAnimeSeries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [editing, setEditing] = useState(null);       // product being edited
  const [formOpen, setFormOpen] = useState(false);
  const [deleting, setDeleting] = useState(null);     // product waiting for delete confirmation
  const [busy, setBusy] = useState(false);
  const [categoryOpen, setCategoryOpen] = useState(false);
  const [newCategory, setNewCategory] = useState('');

  const loadLookups = useCallback(() => {
    Promise.all([fetchCategories(), fetchAnimeSeries()]).then(([c, a]) => {
      setCategories(c);
      setAnimeSeries(a);
    });
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setResult(await fetchProducts({ keyword: debouncedKeyword.trim(), sort: 'name', page, size: 10 }));
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [debouncedKeyword, page]);

  useEffect(() => {
    loadLookups();
  }, [loadLookups]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => setPage(0), [debouncedKeyword]);

  function openForm(product = null) {
    setEditing(product);
    setFormOpen(true);
  }

  function handleSaved(saved, wasEdit) {
    setFormOpen(false);
    showToast(wasEdit ? `Saved ${saved.name}` : `Added ${saved.name}`);
    load();
    loadLookups();
  }

  async function handleDelete() {
    setBusy(true);
    try {
      await deleteProductRequest(deleting.id);
      showToast(`Removed ${deleting.name} from the shop`, 'info');
      setDeleting(null);
      load();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setBusy(false);
    }
  }

  async function handleAddCategory() {
    if (!newCategory.trim()) return;
    setBusy(true);
    try {
      const c = await createCategoryRequest({ name: newCategory.trim(), description: '' });
      showToast(`Category "${c.name}" added`);
      setNewCategory('');
      setCategoryOpen(false);
      loadLookups();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setBusy(false);
    }
  }

  const products = result?.content ?? [];

  return (
    <>
      <div className="d-flex flex-wrap gap-2 mb-3">
        <div className="position-relative flex-grow-1" style={{ maxWidth: 360 }}>
          <i className="bi bi-search position-absolute top-50 translate-middle-y ms-3 text-muted" />
          <input className="form-control ps-5 rounded-pill" type="search" placeholder="Search products…"
            value={keyword} onChange={(e) => setKeyword(e.target.value)} data-testid="admin-product-search" />
        </div>
        <div className="ms-auto d-flex gap-2">
          <button className="btn btn-ghost" onClick={() => setCategoryOpen(true)}>
            <i className="bi bi-tags me-1" />New category
          </button>
          <button className="btn btn-ida" onClick={() => openForm(null)} data-testid="add-product">
            <i className="bi bi-plus-lg me-1" />Add product
          </button>
        </div>
      </div>

      {error ? (
        <ErrorMessage message={error} onRetry={load} />
      ) : (
        <div className="cart-panel">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0 admin-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th className="d-none d-md-table-cell">Category</th>
                  <th className="text-end">Price</th>
                  <th>Stock</th>
                  <th className="text-end">Actions</th>
                </tr>
              </thead>
              <tbody className={loading ? 'opacity-50' : ''}>
                {products.length === 0 && !loading && (
                  <tr><td colSpan="5" className="text-center text-muted py-4">No products found.</td></tr>
                )}
                {products.map((p) => (
                  <tr key={p.id} data-testid="admin-product-row">
                    <td>
                      <div className="d-flex align-items-center gap-3">
                        <div className="table-thumb"><ProductImage product={p} /></div>
                        <div className="min-w-0">
                          <Link to={`/products/${p.id}`} className="fw-semibold text-reset text-decoration-none">{p.name}</Link>
                          <div className="small text-muted">{p.animeSeries}</div>
                        </div>
                      </div>
                    </td>
                    <td className="d-none d-md-table-cell">{p.categoryName}</td>
                    <td className="text-end fw-semibold">{formatPrice(p.price)}</td>
                    <td><StockBadge stock={p.stock} /> <span className="small text-muted ms-1">{p.stock}</span></td>
                    <td className="text-end text-nowrap">
                      <button className="btn btn-sm btn-outline-secondary rounded-pill me-1" onClick={() => openForm(p)}
                        aria-label={`Edit ${p.name}`} data-testid="edit-product">
                        <i className="bi bi-pencil" />
                      </button>
                      <button className="btn btn-sm btn-outline-danger rounded-pill" onClick={() => setDeleting(p)}
                        aria-label={`Remove ${p.name}`} data-testid="delete-product">
                        <i className="bi bi-trash" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
      {result && <Pagination page={result.page} totalPages={result.totalPages} onChange={setPage} />}

      <ProductFormModal
        show={formOpen}
        product={editing}
        categories={categories}
        animeSeries={animeSeries}
        onClose={() => setFormOpen(false)}
        onSaved={handleSaved}
      />

      <Modal
        show={Boolean(deleting)}
        title="Remove this product?"
        onClose={() => setDeleting(null)}
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setDeleting(null)} disabled={busy}>Keep it</button>
            <button className="btn btn-danger rounded-pill" onClick={handleDelete} disabled={busy} data-testid="confirm-delete">
              Remove from shop
            </button>
          </>
        }
      >
        <strong>{deleting?.name}</strong> will be hidden from the shop. Past orders keep their record of it
        (this is a soft delete).
      </Modal>

      <Modal
        show={categoryOpen}
        title="New category"
        onClose={() => setCategoryOpen(false)}
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setCategoryOpen(false)}>Cancel</button>
            <button className="btn btn-ida" onClick={handleAddCategory} disabled={busy || !newCategory.trim()}>Add</button>
          </>
        }
      >
        <label className="form-label" htmlFor="new-category">Category name</label>
        <input id="new-category" className="form-control" value={newCategory} maxLength={60}
          onChange={(e) => setNewCategory(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && handleAddCategory()} />
      </Modal>
    </>
  );
}
