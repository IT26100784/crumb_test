package com.crumb.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bread - Entity representing sourdough loaves, baguettes, and artisanal breads.
 * 
 * // INHERITANCE:
 * Subclasses BakeryProduct with product_type = 'BREAD'.
 * 
 * // POLYMORPHISM:
 * Overrides calculateFinalPrice (10% multi-loaf bundle discount on 2+ loaves),
 * loaf storage advice (linen bag), and yield slicing recommendations.
 */
// INHERITANCE
@Entity
@DiscriminatorValue("BREAD")
@Getter
@Setter
@NoArgsConstructor
public class Bread extends BakeryProduct {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "loaf_weight_grams")
    private int loafWeightGrams = 750;

    @Column(name = "is_sourdough")
    private boolean sourdough = true;

    public Bread(String name, String description, double basePrice, String imageUrl,
                 int stockQuantity, String allergens, int loafWeightGrams, boolean sourdough) {
        setName(name);
        setDescription(description);
        setBasePrice(basePrice);
        setImageUrl(imageUrl);
        setStockQuantity(stockQuantity);
        setAllergens(allergens);
        setAvailable(true);
        this.loafWeightGrams = loafWeightGrams;
        this.sourdough = sourdough;
    }

    /**
     * // POLYMORPHISM:
     * Applies a 10% morning bakery bundle discount when buying 2 or more loaves.
     */
    // POLYMORPHISM
    @Override
    public double calculateFinalPrice(int quantity) {
        double subtotal = getBasePrice() * quantity;
        if (quantity >= 2) {
            subtotal = subtotal * 0.90; // 10% multi-loaf bundle savings
        }
        return Math.round(subtotal * 100.0) / 100.0;
    }

    // POLYMORPHISM
    @Override
    public String getProductCategory() {
        return sourdough ? "Wild Yeast Sourdough" : "Artisan Hearth Bread";
    }

    // POLYMORPHISM
    @Override
    public String getStorageInstructions() {
        return "Keep at room temperature in a breathable linen bread bag. Do not refrigerate. Freezes sliced up to 1 month.";
    }

    // POLYMORPHISM
    @Override
    public String getServingRecommendation() {
        return "Loaf weight: " + loafWeightGrams + "g. Slices into 14-18 hearty slices for toast or sandwiches.";
    }
}
