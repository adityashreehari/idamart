// Bootstrap Icons name for each category (any other category gets a grid icon)
const ICONS = {
  figures: 'bi-person-arms-up',
  posters: 'bi-image',
  apparel: 'bi-handbag',
  manga: 'bi-book',
  accessories: 'bi-key',
  plushies: 'bi-emoji-smile',
};

export function categoryIcon(name = '') {
  return ICONS[name.toLowerCase()] || 'bi-grid';
}
