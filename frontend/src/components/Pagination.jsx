/**
 * Page buttons. The backend counts pages from 0; people count from 1,
 * so we show page + 1 but send the 0-based number back.
 */
export default function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null;

  const pages = Array.from({ length: totalPages }, (_, i) => i);

  return (
    <nav aria-label="Product pages" className="mt-4">
      <ul className="pagination justify-content-center flex-wrap">
        <li className={`page-item ${page === 0 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onChange(page - 1)} aria-label="Previous page">
            <i className="bi bi-chevron-left" />
          </button>
        </li>
        {pages.map((p) => (
          <li key={p} className={`page-item ${p === page ? 'active' : ''}`}>
            <button className="page-link" onClick={() => onChange(p)} aria-current={p === page ? 'page' : undefined}>
              {p + 1}
            </button>
          </li>
        ))}
        <li className={`page-item ${page >= totalPages - 1 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onChange(page + 1)} aria-label="Next page">
            <i className="bi bi-chevron-right" />
          </button>
        </li>
      </ul>
    </nav>
  );
}
