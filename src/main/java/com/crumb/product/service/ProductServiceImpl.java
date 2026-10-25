package com.crumb.product.service;

import com.crumb.common.exception.BadRequestException;
import com.crumb.common.exception.ResourceNotFoundException;
import com.crumb.product.dto.ProductRequest;
import com.crumb.product.dto.ProductResponse;
import com.crumb.product.entity.BakeryProduct;
import com.crumb.product.entity.Bread;
import com.crumb.product.entity.Cake;
import com.crumb.product.entity.Pastry;
import com.crumb.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductServiceImpl - Business logic implementation for BakeryProduct management.
 * 
 * WHY: Implements catalog searching, product CRUD, polymorphic calculations,
 * and initial data seeding for cakes, pastries, and artisanal breads.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    // Constructor injection via Lombok
    private final ProductRepository productRepository;

    /**
     * Retrieves products filtered by search keyword, product type, and availability.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(String keyword, String type, Boolean availableOnly) {
        List<BakeryProduct> list;
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = productRepository.searchProducts(keyword.trim());
        } else {
            list = productRepository.findAll();
        }

        return list.stream()
                .filter(p -> availableOnly == null || !availableOnly || p.isAvailable())
                .filter(p -> {
                    if (type == null || type.trim().isEmpty() || "ALL".equalsIgnoreCase(type)) {
                        return true;
                    }
                    if ("CAKE".equalsIgnoreCase(type)) return p instanceof Cake;
                    if ("PASTRY".equalsIgnoreCase(type)) return p instanceof Pastry;
                    if ("BREAD".equalsIgnoreCase(type)) return p instanceof Bread;
                    return true;
                })
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves product DTO by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return ProductResponse.fromEntity(getProductEntity(id));
    }

    /**
     * Retrieves raw BakeryProduct entity by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public BakeryProduct getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    /**
     * Creates a new product entity (Cake, Pastry, or Bread) from form input.
     * Demonstrates dynamic instantiation based on product type.
     */
    @Override
    public ProductResponse createProduct(ProductRequest request) {
        BakeryProduct product;
        String type = request.getProductType() != null ? request.getProductType().toUpperCase() : "CAKE";

        switch (type) {
            case "CAKE" -> {
                Cake cake = new Cake(
                        request.getName().trim(),
                        request.getDescription(),
                        request.getBasePrice(),
                        request.getImageUrl() != null && !request.getImageUrl().isEmpty()
                                ? request.getImageUrl()
                                : "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600",
                        request.getStockQuantity(),
                        request.getAllergens() != null ? request.getAllergens() : "Gluten, Eggs, Dairy",
                        request.getFrostingType() != null ? request.getFrostingType() : "Swiss Buttercream",
                        request.getLayers() != null ? request.getLayers() : 3
                );
                product = cake;
            }
            case "PASTRY" -> {
                Pastry pastry = new Pastry(
                        request.getName().trim(),
                        request.getDescription(),
                        request.getBasePrice(),
                        request.getImageUrl() != null && !request.getImageUrl().isEmpty()
                                ? request.getImageUrl()
                                : "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600",
                        request.getStockQuantity(),
                        request.getAllergens() != null ? request.getAllergens() : "Gluten, Dairy",
                        request.getPastryType() != null ? request.getPastryType() : "Croissant",
                        request.getWarmable() != null ? request.getWarmable() : true
                );
                product = pastry;
            }
            case "BREAD" -> {
                Bread bread = new Bread(
                        request.getName().trim(),
                        request.getDescription(),
                        request.getBasePrice(),
                        request.getImageUrl() != null && !request.getImageUrl().isEmpty()
                                ? request.getImageUrl()
                                : "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=600",
                        request.getStockQuantity(),
                        request.getAllergens() != null ? request.getAllergens() : "Gluten",
                        request.getLoafWeightGrams() != null ? request.getLoafWeightGrams() : 750,
                        request.getSourdough() != null ? request.getSourdough() : true
                );
                product = bread;
            }
            default -> throw new BadRequestException("Invalid product type: " + type + ". Must be CAKE, PASTRY, or BREAD.");
        }

        BakeryProduct saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    /**
     * Updates common and subclass-specific product attributes.
     */
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        BakeryProduct product = getProductEntity(id);

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            product.setImageUrl(request.getImageUrl().trim());
        }
        product.setStockQuantity(request.getStockQuantity());
        if (request.getAllergens() != null) {
            product.setAllergens(request.getAllergens());
        }

        if (product instanceof Cake cake) {
            if (request.getFrostingType() != null) cake.setFrostingType(request.getFrostingType());
            if (request.getLayers() != null) cake.setLayers(request.getLayers());
        } else if (product instanceof Pastry pastry) {
            if (request.getPastryType() != null) pastry.setPastryType(request.getPastryType());
            if (request.getWarmable() != null) pastry.setWarmable(request.getWarmable());
        } else if (product instanceof Bread bread) {
            if (request.getLoafWeightGrams() != null) bread.setLoafWeightGrams(request.getLoafWeightGrams());
            if (request.getSourdough() != null) bread.setSourdough(request.getSourdough());
        }

        BakeryProduct saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    /**
     * Toggles availability of a product.
     */
    @Override
    public ProductResponse toggleAvailability(Long id) {
        BakeryProduct product = getProductEntity(id);
        product.setAvailable(!product.isAvailable());
        BakeryProduct saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    /**
     * Deletes a product by ID.
     */
    @Override
    public void deleteProduct(Long id) {
        BakeryProduct product = getProductEntity(id);
        productRepository.delete(product);
    }

    /**
     * Demonstrates POLYMORPHISM by calling calculateFinalPrice on the abstract reference.
     */
    @Override
    @Transactional(readOnly = true)
    public double calculateFinalPrice(Long id, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        BakeryProduct product = getProductEntity(id);
        // POLYMORPHIC CALL:
        return product.calculateFinalPrice(quantity);
    }

    /**
     * Seeds realistic bakery products on first launch.
     */
    @Override
    public void initSampleProducts() {
        if (productRepository.count() > 0) {
            return;
        }

        List<BakeryProduct> samples = new ArrayList<>();

        // 1. Cakes
        Cake c1 = new Cake("Triple Belgian Chocolate Fudge Cake",
                "Layers of rich dark chocolate sponge smothered with velvety Belgian chocolate ganache.",
                6500.00,
                "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800&auto=format&fit=crop&q=80",
                15, "Gluten, Eggs, Dairy, Soy", "Dark Chocolate Ganache", 3);
        c1.setRating(4.9);
        c1.setReviewCount(48);
        samples.add(c1);

        Cake c2 = new Cake("Madagascan Vanilla Bean & Strawberry Sponge",
                "Light fluffy Genoise sponge layered with freshly macerated organic strawberries and Swiss buttercream.",
                5800.00,
                "https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=800&auto=format&fit=crop&q=80",
                12, "Gluten, Eggs, Dairy", "Swiss Vanilla Buttercream", 3);
        c2.setRating(4.8);
        c2.setReviewCount(36);
        samples.add(c2);

        Cake c3 = new Cake("Salted Caramel Lotus Biscoff Cake",
                "Spiced sponge infused with crushed Lotus biscuits, salted caramel drizzle, and speculoos frosting.",
                6200.00,
                "https://images.unsplash.com/photo-1535141192574-5d4897c13136?w=800&auto=format&fit=crop&q=80",
                10, "Gluten, Dairy, Soy", "Salted Caramel Buttercream", 2);
        c3.setRating(5.0);
        c3.setReviewCount(52);
        samples.add(c3);

        Cake c4 = new Cake("Velvet Royale Red Velvet Cake",
                "Classic Southern red velvet sponge with silky cream cheese frosting and cocoa crumb crown.",
                5900.00,
                "https://images.unsplash.com/photo-1616541823729-00fe0aacd32c?w=800&auto=format&fit=crop&q=80",
                14, "Gluten, Eggs, Dairy", "Cream Cheese Frosting", 3);
        c4.setRating(4.7);
        c4.setReviewCount(29);
        samples.add(c4);

        // 2. Pastries
        Pastry p1 = new Pastry("Parisian Pure Butter Croissant",
                "Laminated artisan dough with 81 honeycomb butter layers baked to golden crispy perfection.",
                750.00,
                "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=800&auto=format&fit=crop&q=80",
                30, "Gluten, Dairy", "Croissant", true);
        p1.setRating(4.9);
        p1.setReviewCount(84);
        samples.add(p1);

        Pastry p2 = new Pastry("Almond Frangipane Flaked Croissant",
                "Twice-baked butter croissant loaded with almond frangipane cream and toasted sliced almonds.",
                950.00,
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800&auto=format&fit=crop&q=80",
                25, "Gluten, Dairy, Tree Nuts (Almonds)", "Filled Croissant", true);
        p2.setRating(4.8);
        p2.setReviewCount(67);
        samples.add(p2);

        Pastry p3 = new Pastry("Valrhona Dark Pain au Chocolat",
                "Crisp laminated French pastry encasing two generous batons of 70% dark Valrhona chocolate.",
                850.00,
                "https://images.unsplash.com/photo-1608198093002-ad4e005484ec?w=800&auto=format&fit=crop&q=80",
                28, "Gluten, Dairy, Soy", "Pain au Chocolat", true);
        p3.setRating(4.9);
        p3.setReviewCount(73);
        samples.add(p3);

        Pastry p4 = new Pastry("Wild Raspberry & Pistachio Tartlet",
                "Pâte sablée tart shell filled with creamy pistachio mousseline and fresh mountain raspberries.",
                1150.00,
                "https://images.unsplash.com/photo-1519915028121-7d3463d20b13?w=800&auto=format&fit=crop&q=80",
                18, "Gluten, Dairy, Tree Nuts (Pistachio), Eggs", "Fruit Tart", false);
        p4.setRating(4.8);
        p4.setReviewCount(41);
        samples.add(p4);

        // 3. Breads
        Bread b1 = new Bread("San Francisco Style Country Sourdough",
                "Slow-fermented for 36 hours with our century-old starter. Crisp blistered crust and airy custard crumb.",
                1450.00,
                "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=800&auto=format&fit=crop&q=80",
                22, "Gluten", 800, true);
        b1.setRating(4.9);
        b1.setReviewCount(95);
        samples.add(b1);

        Bread b2 = new Bread("Rosemary & Kalamata Olive Hearth Loaf",
                "Rustic country sourdough packed with Greek Kalamata olives and garden-fresh wild rosemary.",
                1650.00,
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800&auto=format&fit=crop&q=80",
                16, "Gluten", 750, true);
        b2.setRating(4.8);
        b2.setReviewCount(38);
        samples.add(b2);

        Bread b3 = new Bread("Parisian Golden Crust Baguette",
                "Traditional poolish fermented French baguette with a paper-thin crisp crust and open crumb.",
                650.00,
                "https://images.unsplash.com/photo-1598373182133-52452f7691ef?w=800&auto=format&fit=crop&q=80",
                35, "Gluten", 350, false);
        b3.setRating(4.7);
        b3.setReviewCount(59);
        samples.add(b3);

        productRepository.saveAll(samples);
        System.out.println(">>> SEED: Initialized 11 realistic bakery products (Cakes, Pastries, Breads).");
    }
}
