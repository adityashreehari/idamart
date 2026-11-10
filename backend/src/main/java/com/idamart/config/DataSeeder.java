package com.idamart.config;

import com.idamart.entity.Category;
import com.idamart.entity.Product;
import com.idamart.repository.CategoryRepository;
import com.idamart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stocks the shop with a demo catalogue on startup.
 * Safe to run every time: anything that already exists (matched by name) is skipped.
 * Turn it off with app.seed.enabled=false (or the SEED_DATA=false environment variable).
 *
 * Product names are descriptive demo listings, not official licensed products.
 * Images are simple placeholders; the admin can replace imageUrl later.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Value("${app.seed.enabled}")
    private boolean seedEnabled;

    /** A small, readable way to describe one demo product. */
    private record SeedProduct(String name, String anime, String category, String price,
                               int stock, String color, String description) {
    }

    private static final Map<String, String> CATEGORIES = Map.of(
            "Figures", "Collectible anime figures and statues",
            "Posters", "Wall posters and art prints",
            "Apparel", "Hoodies, T-shirts and jackets",
            "Manga", "Manga volumes in English",
            "Accessories", "Keychains, headbands and collectibles",
            "Plushies", "Soft toys of your favourite characters"
    );

    private static final List<SeedProduct> PRODUCTS = List.of(
            // ---- Figures ----
            new SeedProduct("Naruto Uzumaki Sage Mode Figure", "Naruto", "Figures", "189.00", 15, "f97316",
                    "20 cm PVC figure of Naruto in Sage Mode on a rocky base."),
            new SeedProduct("Monkey D. Luffy Gear 5 Figure", "One Piece", "Figures", "249.00", 8, "e11d48",
                    "Dynamic 24 cm figure of Luffy's Gear 5 form with swirling cloud effects."),
            new SeedProduct("Tanjiro Kamado Water Breathing Figure", "Demon Slayer", "Figures", "179.00", 12, "0ea5e9",
                    "18 cm figure of Tanjiro mid-strike with a water-effect sword trail."),
            new SeedProduct("Satoru Gojo Domain Expansion Figure", "Jujutsu Kaisen", "Figures", "229.00", 6, "6366f1",
                    "22 cm figure of Gojo with a translucent infinity-void base."),
            new SeedProduct("Goku Super Saiyan Blue Figure", "Dragon Ball", "Figures", "199.00", 10, "2563eb",
                    "21 cm figure of Goku powering up in Super Saiyan Blue."),

            // ---- Posters ----
            new SeedProduct("Survey Corps Wings Poster", "Attack on Titan", "Posters", "39.00", 40, "65a30d",
                    "A3 matte poster featuring the Wings of Freedom emblem."),
            new SeedProduct("Straw Hat Crew Wanted Poster Set", "One Piece", "Posters", "59.00", 25, "ca8a04",
                    "Set of 10 A4 wanted posters for the whole crew."),
            new SeedProduct("Hashira Group Poster", "Demon Slayer", "Posters", "39.00", 30, "be123c",
                    "A3 gloss poster of all nine Hashira."),

            // ---- Apparel ----
            new SeedProduct("Akatsuki Cloud Hoodie", "Naruto", "Apparel", "149.00", 20, "111827",
                    "Black cotton-blend hoodie with red cloud prints. Unisex fit."),
            new SeedProduct("Survey Corps Jacket", "Attack on Titan", "Apparel", "199.00", 9, "78350f",
                    "Brown cropped jacket with embroidered Wings of Freedom patches."),
            new SeedProduct("Jujutsu High Uniform T-Shirt", "Jujutsu Kaisen", "Apparel", "79.00", 35, "1e293b",
                    "Navy T-shirt inspired by the Jujutsu High uniform."),
            new SeedProduct("Plus Ultra Training T-Shirt", "My Hero Academia", "Apparel", "75.00", 3, "16a34a",
                    "Green training T-shirt with the Plus Ultra slogan. Low stock!"),

            // ---- Manga ----
            new SeedProduct("One Piece Vol. 1", "One Piece", "Manga", "45.00", 50, "dc2626",
                    "Where it all begins: Luffy sets out to become King of the Pirates."),
            new SeedProduct("Demon Slayer Vol. 1", "Demon Slayer", "Manga", "42.00", 45, "059669",
                    "Tanjiro's journey begins after tragedy strikes his family."),
            new SeedProduct("Spy x Family Vol. 1", "Spy x Family", "Manga", "42.00", 0, "db2777",
                    "A spy, an assassin and a telepath pretend to be a family. Currently out of stock."),

            // ---- Accessories ----
            new SeedProduct("Hidden Leaf Headband", "Naruto", "Accessories", "35.00", 60, "475569",
                    "Metal-plate headband with the Hidden Leaf symbol and adjustable band."),
            new SeedProduct("Nichirin Sword Keychain Set", "Demon Slayer", "Accessories", "29.00", 80, "7c3aed",
                    "Set of 4 metal sword keychains."),
            new SeedProduct("Dragon Balls Collector Set", "Dragon Ball", "Accessories", "119.00", 14, "f59e0b",
                    "All seven 4.5 cm resin Dragon Balls in a display box."),

            // ---- Plushies ----
            new SeedProduct("Anya Forger Plush", "Spy x Family", "Plushies", "89.00", 18, "f472b6",
                    "30 cm soft plush of Anya with her signature smug face."),
            new SeedProduct("Tony Tony Chopper Plush", "One Piece", "Plushies", "95.00", 11, "fb7185",
                    "25 cm plush of Chopper wearing his pink hat.")
    );

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        Map<String, Category> categories = new HashMap<>();
        CATEGORIES.forEach((name, description) -> categories.put(name,
                categoryRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> categoryRepository.save(
                                Category.builder().name(name).description(description).build()))));

        int added = 0;
        for (SeedProduct sp : PRODUCTS) {
            if (productRepository.existsByNameIgnoreCase(sp.name())) {
                continue;
            }
            productRepository.save(Product.builder()
                    .name(sp.name())
                    .animeSeries(sp.anime())
                    .category(categories.get(sp.category()))
                    .price(new BigDecimal(sp.price()))
                    .stock(sp.stock())
                    .description(sp.description())
                    .imageUrl(placeholderImage(sp.name(), sp.color()))
                    .active(true)
                    .build());
            added++;
        }
        if (added > 0) {
            log.info("Demo catalogue: added {} products", added);
        }
    }

    private static String placeholderImage(String text, String background) {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        return "https://placehold.co/600x600/" + background + "/ffffff/png?text=" + encoded;
    }
}
