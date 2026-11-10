import { useEffect, useState } from 'react';
import api from '../api/client';

const SHOW_AFTER_MS = 2500;

/**
 * Free hosting puts the backend to sleep after ~15 minutes without visitors,
 * and the first request then takes up to a minute while it wakes up.
 * On page load we ping the API; if it hasn't answered within 2.5 s, we show a friendly note
 * instead of letting the page look broken. It disappears as soon as the server answers.
 */
export default function ServerWakeNotice() {
  const [waking, setWaking] = useState(false);

  useEffect(() => {
    let answered = false;
    const timer = setTimeout(() => !answered && setWaking(true), SHOW_AFTER_MS);

    api
      .get('/api/categories', { timeout: 90000 })
      .catch(() => {})
      .finally(() => {
        answered = true;
        clearTimeout(timer);
        setWaking(false);
      });

    return () => clearTimeout(timer);
  }, []);

  if (!waking) return null;

  return (
    <div className="wake-notice" role="status" data-testid="wake-notice">
      <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />
      Waking up the IdaMart server. On free hosting the first visit can take up to a minute.
    </div>
  );
}
