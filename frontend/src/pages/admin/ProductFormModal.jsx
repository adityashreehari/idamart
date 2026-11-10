import { useEffect, useState } from 'react';
import Modal from '../../components/Modal.jsx';
import ProductImage from '../../components/ProductImage.jsx';
import { createProductRequest, updateProductRequest } from '../../api/admin';
import { getErrorMessage } from '../../utils/format';

const EMPTY = { name: '', animeSeries: '', categoryId: '', price: '', stock: '', imageUrl: '', description: '' };

/** Same rules as the backend's ProductRequest. */
function validate(f) {
  const e = {};
  if (!f.name.trim()) e.name = 'Name is required.';
  else if (f.name.length > 150) e.name = 'At most 150 characters.';
  if (!f.animeSeries.trim()) e.animeSeries = 'Anime series is required.';
  if (!f.categoryId) e.categoryId = 'Choose a category.';
  const price = Number(f.price);
  if (f.price === '' || Number.isNaN(price) || price <= 0) e.price = 'Price must be more than 0.';
  else if (!/^\d+(\.\d{1,2})?$/.test(String(f.price))) e.price = 'Use at most 2 decimal places.';
  const stock = Number(f.stock);
  if (f.stock === '' || !Number.isInteger(stock) || stock < 0) e.stock = 'Stock must be a whole number, 0 or more.';
  if (f.description.length > 1000) e.description = 'At most 1000 characters.';
  return e;
}

/** Add or edit a product. `product` = null means "add new". */
export default function ProductFormModal({ show, product, categories, animeSeries, onClose, onSaved }) {
  const [form, setForm] = useState(EMPTY);
  const [touched, setTouched] = useState({});
  const [saving, setSaving] = useState(false);
  const [serverError, setServerError] = useState('');

  useEffect(() => {
    if (!show) return;
    setForm(
      product
        ? {
            name: product.name,
            animeSeries: product.animeSeries,
            categoryId: String(product.categoryId),
            price: String(product.price),
            stock: String(product.stock),
            imageUrl: product.imageUrl || '',
            description: product.description || '',
          }
        : EMPTY,
    );
    setTouched({});
    setServerError('');
  }, [show, product]);

  const errors = validate(form);
  const set = (e) => setForm({ ...form, [e.target.name]: e.target.value });
  const blur = (e) => setTouched({ ...touched, [e.target.name]: true });
  const cls = (name, base = 'form-control') => `${base} ${touched[name] && errors[name] ? 'is-invalid' : ''}`;
  const err = (name) => touched[name] && errors[name] && <div className="invalid-feedback">{errors[name]}</div>;

  async function handleSave() {
    setTouched(Object.fromEntries(Object.keys(EMPTY).map((k) => [k, true])));
    if (Object.keys(errors).length > 0) return;
    setSaving(true);
    setServerError('');
    const payload = {
      name: form.name.trim(),
      animeSeries: form.animeSeries.trim(),
      categoryId: Number(form.categoryId),
      price: Number(form.price),
      stock: Number(form.stock),
      imageUrl: form.imageUrl.trim(),
      description: form.description.trim(),
    };
    try {
      const saved = product
        ? await updateProductRequest(product.id, payload)
        : await createProductRequest(payload);
      onSaved(saved, Boolean(product));
    } catch (e) {
      setServerError(getErrorMessage(e));
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal
      show={show}
      size="lg"
      title={product ? `Edit: ${product.name}` : 'Add a product'}
      onClose={onClose}
      testId="product-form"
      footer={
        <>
          <button className="btn btn-ghost" onClick={onClose} disabled={saving}>Cancel</button>
          <button className="btn btn-ida" onClick={handleSave} disabled={saving} data-testid="product-save">
            {saving && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}
            {product ? 'Save changes' : 'Add product'}
          </button>
        </>
      }
    >
      {serverError && <div className="alert alert-danger py-2">{serverError}</div>}
      <div className="row g-3">
        <div className="col-md-8">
          <div className="row g-3">
            <div className="col-12">
              <label className="form-label" htmlFor="pf-name">Name</label>
              <input id="pf-name" name="name" className={cls('name')} value={form.name} onChange={set} onBlur={blur}
                data-testid="pf-name" />
              {err('name')}
            </div>
            <div className="col-md-6">
              <label className="form-label" htmlFor="pf-anime">Anime series</label>
              <input id="pf-anime" name="animeSeries" list="anime-options" className={cls('animeSeries')}
                value={form.animeSeries} onChange={set} onBlur={blur} data-testid="pf-anime" />
              <datalist id="anime-options">
                {animeSeries.map((a) => <option key={a} value={a} />)}
              </datalist>
              {err('animeSeries')}
            </div>
            <div className="col-md-6">
              <label className="form-label" htmlFor="pf-category">Category</label>
              <select id="pf-category" name="categoryId" className={cls('categoryId', 'form-select')}
                value={form.categoryId} onChange={set} onBlur={blur} data-testid="pf-category">
                <option value="">Choose…</option>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
              {err('categoryId')}
            </div>
            <div className="col-6">
              <label className="form-label" htmlFor="pf-price">Price (AED)</label>
              <input id="pf-price" name="price" type="number" min="0.01" step="0.01" className={cls('price')}
                value={form.price} onChange={set} onBlur={blur} data-testid="pf-price" />
              {err('price')}
            </div>
            <div className="col-6">
              <label className="form-label" htmlFor="pf-stock">Stock</label>
              <input id="pf-stock" name="stock" type="number" min="0" step="1" className={cls('stock')}
                value={form.stock} onChange={set} onBlur={blur} data-testid="pf-stock" />
              {err('stock')}
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <label className="form-label">Preview</label>
          <ProductImage
            key={form.imageUrl}
            product={{ name: form.name || 'New product', imageUrl: form.imageUrl, stock: Number(form.stock) || 1 }}
            className="rounded-3"
          />
        </div>

        <div className="col-12">
          <label className="form-label" htmlFor="pf-image">Image URL</label>
          <input id="pf-image" name="imageUrl" className="form-control" value={form.imageUrl} onChange={set}
            placeholder="/images/products/my-product.svg or https://…" data-testid="pf-image" />
          <div className="form-text">Use your own photos or free-licence images (Unsplash, Pexels).</div>
        </div>
        <div className="col-12">
          <label className="form-label" htmlFor="pf-desc">Description</label>
          <textarea id="pf-desc" name="description" rows="3" className={cls('description')} value={form.description}
            onChange={set} onBlur={blur} data-testid="pf-description" />
          {err('description')}
        </div>
      </div>
    </Modal>
  );
}
