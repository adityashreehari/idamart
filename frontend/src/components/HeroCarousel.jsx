import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

/**
 * Home page banner that rotates every 6 seconds (Bootstrap carousel styles).
 * React holds which slide is showing; arrows and dots change it; it pauses on hover.
 */
const SLIDES = [
  {
    eyebrow: 'Anime merch · Delivered in the UAE',
    title: <>Your favourite series, <em>on your shelf.</em></>,
    text: 'Figures, hoodies, manga and collectibles from Naruto, One Piece, Demon Slayer, Jujutsu Kaisen and more.',
    cta: { label: 'Shop now', to: '/products' },
    secondary: { label: 'Best prices', to: '/products?sort=price_asc' },
    kanji: '店',
    theme: 'crimson',
  },
  {
    eyebrow: 'Collector corner',
    title: <>Figures that <em>steal the scene.</em></>,
    text: 'Gear 5 Luffy, Sage Mode Naruto, Domain Expansion Gojo. Limited stock on every piece.',
    cta: { label: 'Shop figures', category: 'Figures' },
    secondary: { label: 'One Piece', to: '/products?anime=One%20Piece' },
    kanji: '像',
    theme: 'violet',
  },
  {
    eyebrow: 'Wear the fandom',
    title: <>Hoodies & tees <em>from AED 75.</em></>,
    text: 'Akatsuki clouds, Survey Corps wings and Jujutsu High uniforms for everyday wear.',
    cta: { label: 'Shop apparel', category: 'Apparel' },
    secondary: { label: 'Naruto', to: '/products?anime=Naruto' },
    kanji: '服',
    theme: 'gold',
  },
];

const INTERVAL_MS = 6000;

export default function HeroCarousel({ stats, categories = [] }) {
  // Slides can point at a category by name; look up its id once categories have loaded
  const linkFor = (cta) => {
    if (!cta.category) return cta.to;
    const match = categories.find((c) => c.name.toLowerCase() === cta.category.toLowerCase());
    return match ? `/products?categoryId=${match.id}` : '/products';
  };

  const [index, setIndex] = useState(0);
  const [paused, setPaused] = useState(false);

  const goTo = useCallback((i) => setIndex((i + SLIDES.length) % SLIDES.length), []);

  // Auto-advance unless the mouse is over the banner
  useEffect(() => {
    if (paused) return undefined;
    const timer = setInterval(() => setIndex((i) => (i + 1) % SLIDES.length), INTERVAL_MS);
    return () => clearInterval(timer);
  }, [paused]);

  return (
    <section
      className="hero carousel slide mb-5"
      aria-roledescription="carousel"
      aria-label="Featured collections"
      onMouseEnter={() => setPaused(true)}
      onMouseLeave={() => setPaused(false)}
      data-testid="hero-carousel"
    >
      <div className="carousel-inner">
        {SLIDES.map((slide, i) => (
          <div
            key={slide.kanji}
            className={`carousel-item hero-slide theme-${slide.theme} ${i === index ? 'active' : ''}`}
            aria-roledescription="slide"
            aria-label={`${i + 1} of ${SLIDES.length}`}
            aria-hidden={i !== index}
          >
            <div className="hero-kanji" aria-hidden="true">{slide.kanji}</div>
            <div className="container py-5 py-lg-5 position-relative">
              <div className="row align-items-center py-lg-4">
                <div className="col-lg-7">
                  <div className="eyebrow mb-3">{slide.eyebrow}</div>
                  <h1 className="display-4 mb-3">{slide.title}</h1>
                  <p className="lead mb-4">{slide.text}</p>
                  <div className="d-flex flex-wrap gap-2">
                    <Link to={linkFor(slide.cta)} className="btn btn-ida btn-lg" data-testid={i === 0 ? 'shop-now' : undefined}>
                      {slide.cta.label} <i className="bi bi-arrow-right ms-1" />
                    </Link>
                    <Link to={slide.secondary.to} className="btn btn-outline-light btn-lg rounded-pill">
                      {slide.secondary.label}
                    </Link>
                  </div>
                </div>
                <div className="col-lg-5 mt-5 mt-lg-0">
                  <div className="row text-center g-3">
                    {stats.map((s) => (
                      <div className="col-4 hero-stat" key={s.label}>
                        <strong>{s.value}</strong>
                        <small>{s.label}</small>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      <button className="carousel-control-prev" type="button" onClick={() => goTo(index - 1)} aria-label="Previous slide">
        <span className="carousel-control-prev-icon" aria-hidden="true" />
      </button>
      <button className="carousel-control-next" type="button" onClick={() => goTo(index + 1)} aria-label="Next slide">
        <span className="carousel-control-next-icon" aria-hidden="true" />
      </button>

      <div className="carousel-indicators">
        {SLIDES.map((slide, i) => (
          <button
            key={slide.kanji}
            type="button"
            className={i === index ? 'active' : ''}
            aria-current={i === index ? 'true' : undefined}
            aria-label={`Go to slide ${i + 1}`}
            onClick={() => goTo(i)}
          />
        ))}
      </div>
    </section>
  );
}
