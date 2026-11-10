import { useEffect, useState } from 'react';

/**
 * Waits until the user stops typing for `delay` ms before returning the new value.
 * Used in the search box so we don't call the API on every single key press.
 */
export default function useDebounce(value, delay = 400) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer); // typed again? cancel the old timer
  }, [value, delay]);

  return debounced;
}
