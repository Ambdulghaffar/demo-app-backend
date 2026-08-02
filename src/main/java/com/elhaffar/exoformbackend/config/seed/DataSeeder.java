package com.elhaffar.exoformbackend.config.seed;

import com.elhaffar.exoformbackend.common.enums.ProductStatus;
import com.elhaffar.exoformbackend.entities.Category;
import com.elhaffar.exoformbackend.entities.Product;
import com.elhaffar.exoformbackend.repository.CategoryRepository;
import com.elhaffar.exoformbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Component
@Profile("seed")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        long existing = categoryRepository.count();
        if (existing > 0) {
            log.info("[SEED] Base déjà peuplée ({} catégories trouvées) — seed ignoré.", existing);
            return;
        }

        log.info("[SEED] ══════════════════════════════════════════");
        log.info("[SEED] Démarrage du seed Électro-Chic…");

        Faker faker = new Faker(Locale.FRENCH);

        List<Category> categories = seedCategories();
        int productCount = seedProducts(categories, faker);

        log.info("[SEED] ✓ {} catégories insérées.", categories.size());
        log.info("[SEED] ✓ {} produits insérés.", productCount);
        log.info("[SEED] ══════════════════════════════════════════");
    }

    private List<Category> seedCategories() {
        List<Category> categories = List.of(
            Category.builder()
                .name("Smartphones")
                .description("Téléphones intelligents haut de gamme et milieu de gamme des plus grandes marques mondiales.")
                .imageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9")
                .build(),
            Category.builder()
                .name("Ordinateurs")
                .description("Laptops, ultrabooks et ordinateurs portables pour le travail, la création et les études.")
                .imageUrl("https://images.unsplash.com/photo-1496181133206-80ce9b88a853")
                .build(),
            Category.builder()
                .name("Tablettes")
                .description("Tablettes tactiles pour la productivité, le divertissement et la créativité numérique.")
                .imageUrl("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0")
                .build(),
            Category.builder()
                .name("Montres Connectées")
                .description("Smartwatches et trackers d'activité pour suivre votre santé et rester connecté.")
                .imageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30")
                .build(),
            Category.builder()
                .name("Écouteurs & Casques")
                .description("Casques et écouteurs sans fil avec réduction de bruit active pour une écoute immersive.")
                .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e")
                .build(),
            Category.builder()
                .name("Gaming & Consoles")
                .description("Consoles de jeux, manettes et accessoires gaming pour tous les joueurs.")
                .imageUrl("https://images.unsplash.com/photo-1493711662062-fa541adb3fc8")
                .build(),
            Category.builder()
                .name("Téléviseurs & Image")
                .description("Téléviseurs OLED, QLED et 4K pour une expérience cinéma à domicile.")
                .imageUrl("https://images.unsplash.com/photo-1593359677879-a4bb92f829d1")
                .build(),
            Category.builder()
                .name("Chargeurs & Batteries")
                .description("Chargeurs rapides, câbles et batteries externes pour ne jamais manquer d'énergie.")
                .imageUrl("https://images.unsplash.com/photo-1585771724684-38269d6639fd")
                .build(),
            Category.builder()
                .name("Gadgets Lifestyle")
                .description("Enceintes connectées, objets IoT et gadgets technologiques pour simplifier votre quotidien.")
                .imageUrl("https://images.unsplash.com/photo-1518444065439-e933c06ce9cd")
                .build()
        );

        return categoryRepository.saveAll(categories);
    }

    private int seedProducts(List<Category> cats, Faker faker) {
        Category smartphones    = findCat(cats, "Smartphones");
        Category ordinateurs    = findCat(cats, "Ordinateurs");
        Category tablettes      = findCat(cats, "Tablettes");
        Category montres        = findCat(cats, "Montres Connectées");
        Category ecouteurs      = findCat(cats, "Écouteurs & Casques");
        Category gaming         = findCat(cats, "Gaming & Consoles");
        Category televiseurs    = findCat(cats, "Téléviseurs & Image");
        Category chargeurs      = findCat(cats, "Chargeurs & Batteries");
        Category gadgets        = findCat(cats, "Gadgets Lifestyle");

        List<Product> products = List.of(

            // ── Smartphones (4) ──────────────────────────────────────────────
            product("Samsung Galaxy S24 Ultra 256 Go",
                faker.lorem().sentence(8) + " Processeur Snapdragon 8 Gen 3, écran Dynamic AMOLED 6,8\", stylet S Pen intégré.",
                bd("1299.99"), 45, "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c",
                smartphones, ProductStatus.ACTIVE),

            product("Apple iPhone 15 Pro Max 512 Go",
                faker.lorem().sentence(7) + " Puce A17 Pro, écran Super Retina XDR 6,7\", système de caméra pro 48 MP.",
                bd("1499.00"), 30, "https://images.unsplash.com/photo-1592750475338-74b7b21085ab",
                smartphones, ProductStatus.ACTIVE),

            product("Xiaomi 13T Pro 256 Go",
                faker.lorem().sentence(6) + " Dimensity 9200+, écran AMOLED 6,67\" 144 Hz, charge rapide 120W.",
                bd("699.99"), 8, "https://images.unsplash.com/photo-1598327105666-5b89351aff97",
                smartphones, ProductStatus.ACTIVE),

            product("Google Pixel 8 128 Go",
                faker.lorem().sentence(8) + " Puce Tensor G3, meilleur appareil photo Android, 7 ans de mises à jour garanties.",
                bd("799.00"), 0, "https://images.unsplash.com/photo-1585060544812-6b45742d762f",
                smartphones, ProductStatus.OUT_OF_STOCK),

            // ── Ordinateurs (4) ──────────────────────────────────────────────
            product("Apple MacBook Pro M3 14\" 1 To",
                faker.lorem().sentence(7) + " Puce M3 Pro, écran Liquid Retina XDR, autonomie jusqu'à 18 heures.",
                bd("1999.00"), 12, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8",
                ordinateurs, ProductStatus.ACTIVE),

            product("Dell XPS 15 OLED Intel Core i7",
                faker.lorem().sentence(6) + " Écran OLED 15,6\" 3,5K tactile, RTX 4060, 32 Go RAM, SSD 1 To.",
                bd("1799.99"), 7, "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89",
                ordinateurs, ProductStatus.ACTIVE),

            product("Lenovo ThinkPad X1 Carbon Gen 11",
                faker.lorem().sentence(7) + " Ultra-fin 1,12 kg, Intel Core i7 13e gen, 16 Go RAM, clavier rétroéclairé.",
                bd("1399.00"), 15, "https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2",
                ordinateurs, ProductStatus.ACTIVE),

            product("ASUS ZenBook 14 OLED",
                faker.lorem().sentence(6) + " Écran OLED 2,8K 90 Hz, Ryzen 7 7730U, 16 Go RAM, design ultra-compact.",
                bd("899.99"), 0, "https://images.unsplash.com/photo-1587614382346-4ec70e388b28",
                ordinateurs, ProductStatus.OUT_OF_STOCK),

            // ── Tablettes (3) ────────────────────────────────────────────────
            product("Apple iPad Pro 12,9\" M2 Wi-Fi 256 Go",
                faker.lorem().sentence(7) + " Puce M2, écran Liquid Retina XDR ProMotion 120 Hz, compatible Apple Pencil 2.",
                bd("1199.00"), 25, "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0",
                tablettes, ProductStatus.ACTIVE),

            product("Samsung Galaxy Tab S9+ 256 Go",
                faker.lorem().sentence(6) + " Écran Dynamic AMOLED 12,4\" 120 Hz, Snapdragon 8 Gen 2, S Pen inclus.",
                bd("849.00"), 18, "https://images.unsplash.com/photo-1562156622-8eac27fec2b3",
                tablettes, ProductStatus.ACTIVE),

            product("Microsoft Surface Pro 9 Intel i5",
                faker.lorem().sentence(7) + " Tablette 2-en-1, écran PixelSense 13\" 120 Hz, Windows 11 Pro natif.",
                bd("1099.00"), 5, "https://images.unsplash.com/photo-1589003077984-894e133dabab",
                tablettes, ProductStatus.ACTIVE),

            // ── Montres Connectées (3) ───────────────────────────────────────
            product("Apple Watch Series 9 GPS 45 mm",
                faker.lorem().sentence(6) + " Puce S9, écran Always-On Retina, suivi santé avancé, résistance eau 50 m.",
                bd("429.00"), 50, "https://images.unsplash.com/photo-1551816230-ef5deaed4a26",
                montres, ProductStatus.ACTIVE),

            product("Samsung Galaxy Watch 6 Classic 47 mm",
                faker.lorem().sentence(7) + " Lunette tournante physique, Exynos W930, analyse composition corporelle, NFC.",
                bd("379.00"), 35, "https://images.unsplash.com/photo-1617043786394-f977fa12eddf",
                montres, ProductStatus.ACTIVE),

            product("Garmin Fenix 7 Pro Solar",
                faker.lorem().sentence(6) + " GPS multi-bandes, recharge solaire, 22 jours d'autonomie, cartes topographiques.",
                bd("699.99"), 3, "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1",
                montres, ProductStatus.INACTIVE),

            // ── Écouteurs & Casques (3) ──────────────────────────────────────
            product("Sony WH-1000XM5 Noir",
                faker.lorem().sentence(7) + " Réduction de bruit leader du marché, 30h d'autonomie, charge rapide 3 min.",
                bd("329.00"), 60, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
                ecouteurs, ProductStatus.ACTIVE),

            product("Apple AirPods Pro 2e génération",
                faker.lorem().sentence(6) + " ANC adaptative H2, audio spatial personnalisé, boîtier MagSafe USB-C.",
                bd("279.00"), 85, "https://images.unsplash.com/photo-1572569511254-d8f925fe2cbb",
                ecouteurs, ProductStatus.ACTIVE),

            product("Bose QuietComfort 45 Blanc",
                faker.lorem().sentence(6) + " Réduction de bruit Aware Mode, confort premium 24h, connectivité Bluetooth 5.1.",
                bd("259.00"), 4, "https://images.unsplash.com/photo-1590658268037-6bf12165a8df",
                ecouteurs, ProductStatus.ACTIVE),

            // ── Gaming & Consoles (3) ─────────────────────────────────────────
            product("Sony PlayStation 5 Slim Digital",
                faker.lorem().sentence(6) + " SSD ultra-rapide 825 Go, ray-tracing, 4K 120 fps, DualSense inclus.",
                bd("449.99"), 0, "https://images.unsplash.com/photo-1606813907291-d86efa9b94db",
                gaming, ProductStatus.OUT_OF_STOCK),

            product("Microsoft Xbox Series X 1 To",
                faker.lorem().sentence(7) + " 4K 60 fps, 8K ready, SSD NVMe 1 To, rétrocompatibilité 4 générations.",
                bd("499.99"), 6, "https://images.unsplash.com/photo-1621259182978-7be86b2f56b5",
                gaming, ProductStatus.ACTIVE),

            product("Nintendo Switch OLED Blanc",
                faker.lorem().sentence(6) + " Écran OLED 7\", dock de charge TV inclus, 64 Go stockage interne.",
                bd("349.99"), 120, "https://images.unsplash.com/photo-1578303512597-81e6cc155b3e",
                gaming, ProductStatus.ACTIVE),

            // ── Téléviseurs & Image (3) ──────────────────────────────────────
            product("Samsung QLED 4K QE65Q80C 65\"",
                faker.lorem().sentence(7) + " Quantum HDR 1500, Quantum Matrix Technology, Motion Xcelerator 144 Hz.",
                bd("1299.00"), 15, "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1",
                televiseurs, ProductStatus.ACTIVE),

            product("LG OLED evo C3 55\" 4K",
                faker.lorem().sentence(6) + " Panneau WOLED evo, α9 Gen6 AI, Dolby Vision IQ, HDMI 2.1 × 4, G-Sync.",
                bd("1799.00"), 8, "https://images.unsplash.com/photo-1571415060716-baff6f6e3f85",
                televiseurs, ProductStatus.ACTIVE),

            product("Sony Bravia XR A80L OLED 55\"",
                faker.lorem().sentence(7) + " Processeur XR Cognitive, Acoustic Surface Audio+, Perfect pour PS5, Google TV.",
                bd("1099.00"), 22, "https://images.unsplash.com/photo-1567690187548-f07b1d7d4578",
                televiseurs, ProductStatus.ACTIVE),

            // ── Chargeurs & Batteries (3) ────────────────────────────────────
            product("Anker PowerCore 26800 mAh USB-C",
                faker.lorem().sentence(5) + " 65W Power Delivery, charge 3 appareils simultanément, recharge complète en 4h.",
                bd("59.99"), 200, "https://images.unsplash.com/photo-1585771724684-38269d6639fd",
                chargeurs, ProductStatus.ACTIVE),

            product("Chargeur Rapide USB-C GaN 65W",
                faker.lorem().sentence(5) + " Technologie GaN, 3 ports USB-C + USB-A, compatible USB-PD 3.0 et MacBook.",
                bd("34.99"), 350, "https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5",
                chargeurs, ProductStatus.ACTIVE),

            product("Apple MagSafe Chargeur 15W",
                faker.lorem().sentence(5) + " Charge magnétique sans fil 15W, compatible iPhone 12 et supérieur, câble 1 m.",
                bd("45.00"), 150, "https://images.unsplash.com/photo-1611532736597-de2d4265fba3",
                chargeurs, ProductStatus.ACTIVE),

            // ── Gadgets Lifestyle (3) ────────────────────────────────────────
            product("Amazon Echo Dot 5e génération",
                faker.lorem().sentence(6) + " Alexa intégrée, son amélioré, détection de présence, hub Zigbee intégré.",
                bd("59.99"), 95, "https://images.unsplash.com/photo-1518444065439-e933c06ce9cd",
                gadgets, ProductStatus.ACTIVE),

            product("Fitbit Charge 6 Noir",
                faker.lorem().sentence(6) + " GPS intégré, ECG, SpO2, 7 jours d'autonomie, compatible Google Maps.",
                bd("159.99"), 2, "https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6",
                gadgets, ProductStatus.INACTIVE),

            product("Ring Video Doorbell 4 Noir",
                faker.lorem().sentence(6) + " Vidéo HD couleur en avant-première, détection de mouvement, Wi-Fi double bande.",
                bd("219.99"), 40, "https://images.unsplash.com/photo-1558618666-fcd25c85cd64",
                gadgets, ProductStatus.ACTIVE)
        );

        productRepository.saveAll(products);
        return products.size();
    }

    private Category findCat(List<Category> cats, String name) {
        return cats.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Catégorie introuvable : " + name));
    }

    private Product product(String name, String description, BigDecimal price,
                            int stock, String imageUrl, Category category, ProductStatus status) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .imageUrl(imageUrl)
                .category(category)
                .status(status)
                .build();
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
