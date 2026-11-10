import { useEffect } from 'react';

/**
 * A reusable pop-up window with Bootstrap's modal styles.
 * React decides when it's shown (the `show` prop); Escape or clicking outside closes it.
 */
export default function Modal({ show, title, onClose, children, footer, size = '', testId }) {
  useEffect(() => {
    if (!show) return undefined;
    const onKey = (e) => e.key === 'Escape' && onClose();
    document.addEventListener('keydown', onKey);
    document.body.classList.add('modal-open');
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.classList.remove('modal-open');
    };
  }, [show, onClose]);

  if (!show) return null;

  return (
    <>
      <div
        className="modal fade show d-block"
        role="dialog"
        aria-modal="true"
        aria-label={typeof title === 'string' ? title : undefined}
        onMouseDown={(e) => e.target === e.currentTarget && onClose()}
        data-testid={testId}
      >
        <div className={`modal-dialog modal-dialog-centered modal-dialog-scrollable ${size ? `modal-${size}` : ''}`}>
          <div className="modal-content ida-modal">
            {title && (
              <div className="modal-header">
                <h5 className="modal-title fw-bold">{title}</h5>
                <button type="button" className="btn-close" aria-label="Close" onClick={onClose} />
              </div>
            )}
            <div className="modal-body">{children}</div>
            {footer && <div className="modal-footer">{footer}</div>}
          </div>
        </div>
      </div>
      <div className="modal-backdrop fade show" />
    </>
  );
}
