package com.crumb.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pastry - Entity representing single-serving pastries, croissants, and tarts.
 * 
 * // INHERITANCE:
 * Subclasses BakeryProduct with product_type = 'PASTRY'.
 * 
 * // POLYMORPHISM:
 * Overrides calculateFinalPrice (15% baker's box discount when buying 6 or more),
 * pastry-specific warming advice, and storage rules.
 */
// INHERITANCE
@Entity
@DiscriminatorValue("PASTRY")
@Getter
@Setter
@NoArgsConstructor
public class Pastry extends BakeryProduct {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "pastry_type")
    private String pastryType = "Croissant";

    @Column(name = "is_warmable")
    private boolean warmable = true;

    public Pastry(String name, String description, double basePrice, String imageUrl,
                  int stockQuantity, String allergens, String pastryType, boolean warmable) {
        setName(name);
        setDescription(description);
        setBasePrice(basePrice);
        setImageUrl(imageUrl);
        setStockQuantity(stockQuantity);
        setAllergens(allergens);
        setAvailable(true);
        this.pastryType = pastryType;
        this.warmable = warmable;
    }

    /**
     * // POLYMORPHISM:
     * Offers a 15% pastry box discount when purchasing 6 or more pastries.
     */
    // POLYMORPHISM
    @Override
    public double calculateFinalPrice(int quantity) {
        double subtotal = getBasePrice() * quantity;
        if (quantity >= 6) {
            subtotal = subtotal * 0.85; // 15% pastry box bundle discount
        }
        return Math.round(subtotal * 100.0) / 100.0;
    }

    // POLYMORPHISM
    @Override
    public String getProductCategory() {
        return "French Pastry & Viennoiserie";
    }

    // POLYMORPHISM
    @Override
    public String getStorageInstructions() {
        return "Best enjoyed fresh same day. Store at room temperature in a dry pastry box away from direct heat.";
    }

    // POLYMORPHISM
    @Override
    public String getServingRecommendation() {
        return warmable ? "Individual portion. Warm in oven at 160°C for 3 minutes for crisp flakes."
                        : "Individual chilled dessert portion; pair with espresso.";
    }
}
