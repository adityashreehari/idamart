import { createContext, useCallback, useContext, useMemo, useState } from 'react';

/**
 * Small pop-up messages ("Added to cart ✓") in the bottom-left corner
 * (left, so they never cover the slide-out cart on the right).
 * Context = data any component can reach without passing props down through every level.
 * Uses Bootstrap's toast styles; React decides when they appear and disappear.
 */
const ToastContext = createContext(null);

const ICONS = {
  success: 'bi-check-circle-fill text-success',
  error: 'bi-exclamation-octagon-fill text-danger',
  info: 'bi-info-circle-fill text-primary',
};

let nextId = 1;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const dismiss = useCallback((id) => {
    setToasts((list) => list.filter((t) => t.id !== id));
  }, []);

  const showToast = useCallback(
    (message, type = 'success', duration = 3500) => {
      const id = nextId++;
      setToasts((list) => [...list, { id, message, type }]);
      setTimeout(() => dismiss(id), duration);
    },
    [dismiss],
  );

  const value = useMemo(() => ({ showToast }), [showToast]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-container position-fixed bottom-0 start-0 p-3" style={{ zIndex: 1090 }}>
        {toasts.map((t) => (
          <div
            key={t.id}
            className="toast show ida-toast"
            role={t.type === 'error' ? 'alert' : 'status'}
            aria-live={t.type === 'error' ? 'assertive' : 'polite'}
            data-testid={`toast-${t.type}`}
          >
            <div className="toast-body d-flex align-items-start gap-2">
              <i className={`bi ${ICONS[t.type] || ICONS.info} fs-5`} />
              <div className="flex-grow-1 pt-1">{t.message}</div>
              <button type="button" className="btn-close" aria-label="Close" onClick={() => dismiss(t.id)} />
            </div>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) throw new Error('useToast must be used inside <ToastProvider>');
  return context;
}
