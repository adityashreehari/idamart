import { Route, Routes } from 'react-router-dom';
import AppNavbar from './components/AppNavbar.jsx';
import Footer from './components/Footer.jsx';
import ScrollToTop from './components/ScrollToTop.jsx';
import HomePage from './pages/HomePage.jsx';
import ProductsPage from './pages/ProductsPage.jsx';
import ProductDetailPage from './pages/ProductDetailPage.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';

/**
 * The page layout and the list of pages (routes).
 * Every URL maps to one page component.
 */
export default function App() {
  return (
    <div className="d-flex flex-column min-vh-100">
      <ScrollToTop />
      <AppNavbar />

      <main className="flex-grow-1">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>

      <Footer />
    </div>
  );
}
