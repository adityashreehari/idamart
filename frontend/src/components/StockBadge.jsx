import { LOW_STOCK_LIMIT } from '../utils/format';

export default function StockBadge({ stock }) {
  if (stock <= 0) {
    return <span className="stock-badge out" data-testid="stock-badge">Out of stock</span>;
  }
  if (stock <= LOW_STOCK_LIMIT) {
    return <span className="stock-badge low" data-testid="stock-badge">Only {stock} left</span>;
  }
  return <span className="stock-badge in" data-testid="stock-badge">In stock</span>;
}
