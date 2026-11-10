/** The − [ 2 ] + control, used on product pages, the cart page and the slide-out cart. */
export default function QuantityStepper({ value, min = 1, max, onChange, disabled = false, size = '', testId }) {
  return (
    <div className={`input-group qty-group ${size === 'sm' ? 'input-group-sm qty-sm' : ''}`}>
      <button
        className="btn btn-outline-secondary"
        type="button"
        onClick={() => onChange(value - 1)}
        disabled={disabled || value <= min}
        aria-label="Decrease quantity"
        data-testid={testId ? `${testId}-minus` : undefined}
      >
        <i className="bi bi-dash" />
      </button>
      <input
        className="form-control"
        value={value}
        readOnly
        aria-label="Quantity"
        data-testid={testId}
      />
      <button
        className="btn btn-outline-secondary"
        type="button"
        onClick={() => onChange(value + 1)}
        disabled={disabled || value >= max}
        aria-label="Increase quantity"
        data-testid={testId ? `${testId}-plus` : undefined}
      >
        <i className="bi bi-plus" />
      </button>
    </div>
  );
}
