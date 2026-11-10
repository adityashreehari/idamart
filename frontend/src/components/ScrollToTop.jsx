import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

/** When you open a new page, start at the top (React doesn't do this on its own). */
export default function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);
  return null;
}
