package com.crumb.product.dto;

import com.crumb.product.entity.BakeryProduct;
import com.crumb.product.entity.Bread;
import com.crumb.product.entity.Cake;
import com.crumb.product.entity.Pastry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ProductResponse - Output presentation DTO for bakery catalog items.
 * 
 * WHY: Presents serialized product information including polymorphic calculations
 * (category, serving guidelines, storage instructions) to frontend consumers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    // ENCAPSULATION: private fields with Lombok accessors
    private Long id;
    private String name;
    private String description;
    private double basePrice;
    private String imageUrl;
    private int stockQuantity;
    private boolean available;
    private double rating;
    private int reviewCount;
    private String allergens;
    private String productType; // CAKE, PASTRY, BREAD
    private LocalDateTime createdAt;

    // POLYMORPHIC calculated fields
    private String category;
    private String storageInstructions;
    private String servingRecommendation;

    // Subclass specific fields
    private String frostingType;
    private Integer layers;
    private String pastryType;
    private Boolean warmable;
    private Integer loafWeightGrams;
    private Boolean sourdough;

    /**
     * Converts a BakeryProduct entity (Cake, Pastry, or Bread) into a ProductResponse DTO.
     * Demonstrates POLYMORPHISM by calling polymorphic methods on the BakeryProduct instance.
     */
    public static ProductResponse fromEntity(BakeryProduct product) {
        if (product == null) return null;

        ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .imageUrl(product.getImageUrl())
                .stockQuantity(product.getStockQuantity())
                .available(product.isAvailable())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .allergens(product.getAllergens())
                .createdAt(product.getCreatedAt())
                // POLYMORPHIC CALLS:
                .category(product.getProductCategory())
                .storageInstructions(product.getStorageInstructions())
                .servingRecommendation(product.getServingRecommendation());

        if (product instanceof Cake cake) {
            builder.productType("CAKE")
                   .frostingType(cake.getFrostingType())
                   .layers(cake.getLayers());
        } else if (product instanceof Pastry pastry) {
            builder.productType("PASTRY")
                   .pastryType(pastry.getPastryType())
                   .warmable(pastry.isWarmable());
        } else if (product instanceof Bread bread) {
            builder.productType("BREAD")
                   .loafWeightGrams(bread.getLoafWeightGrams())
                   .sourdough(bread.isSourdough());
        }

        return builder.build();
    }
}
