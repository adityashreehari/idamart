export default function ErrorMessage({ message, onRetry }) {
  return (
    <div className="empty-state" role="alert" data-testid="error-message">
      <i className="bi bi-wifi-off d-block mb-2" />
      <p className="mb-3">{message}</p>
      {onRetry && (
        <button className="btn btn-ida" onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  );
}
