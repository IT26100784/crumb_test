package com.crumb.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cake - Entity representing whole artisan and specialty cakes.
 * 
 * // INHERITANCE:
 * Subclasses BakeryProduct with product_type = 'CAKE'.
 * 
 * // POLYMORPHISM:
 * Overrides calculateFinalPrice (5% party discount for 3+ whole cakes),
 * storage rules (refrigeration required), and serving guidelines.
 */
// INHERITANCE
@Entity
@DiscriminatorValue("CAKE")
@Getter
@Setter
@NoArgsConstructor
public class Cake extends BakeryProduct {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "frosting_type")
    private String frostingType = "Swiss Meringue Buttercream";

    @Column(name = "layers")
    private int layers = 3;

    public Cake(String name, String description, double basePrice, String imageUrl,
                int stockQuantity, String allergens, String frostingType, int layers) {
        setName(name);
        setDescription(description);
        setBasePrice(basePrice);
        setImageUrl(imageUrl);
        setStockQuantity(stockQuantity);
        setAllergens(allergens);
        setAvailable(true);
        this.frostingType = frostingType;
        this.layers = layers;
    }

    /**
     * // POLYMORPHISM:
     * Applies a 5% party discount when purchasing 3 or more full-size cakes.
     */
    // POLYMORPHISM
    @Override
    public double calculateFinalPrice(int quantity) {
        double subtotal = getBasePrice() * quantity;
        if (quantity >= 3) {
            subtotal = subtotal * 0.95; // 5% bulk party order discount
        }
        return Math.round(subtotal * 100.0) / 100.0;
    }

    // POLYMORPHISM
    @Override
    public String getProductCategory() {
        return "Artisan Cake";
    }

    // POLYMORPHISM
    @Override
    public String getStorageInstructions() {
        return "Keep refrigerated between 2°C - 4°C. Bring to room temperature 30 minutes before cutting.";
    }

    // POLYMORPHISM
    @Override
    public String getServingRecommendation() {
        return "Serves 10-12 generous dessert slices (" + layers + " sponge layers).";
    }
}
