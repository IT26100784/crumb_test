package com.crumb.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * BakeryProduct - Abstract Base Entity for all retail baked goods in Crumb.
 * 
 * // ABSTRACTION:
 * Defines common attributes for all bakery items and declares abstract methods
 * that specialized baked goods (Cake, Pastry, Bread) implement.
 * 
 * // INHERITANCE:
 * Uses JPA SINGLE_TABLE inheritance with discriminator column "product_type".
 * Cake, Pastry, and Bread rows live in the shared "products" table.
 */
// ABSTRACTION
// INHERITANCE
@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "product_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BakeryProduct {

    // // ENCAPSULATION:
    // All attributes are strictly private with Lombok-generated accessors.
    // ENCAPSULATION
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Column(name = "base_price", nullable = false)
    private double basePrice;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 20;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private double rating = 5.0;

    @Column(name = "review_count", nullable = false)
    private int reviewCount = 1;

    @Column(length = 300)
    private String allergens = "Contains Gluten, Dairy";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "product_type", insertable = false, updatable = false)
    private String productType;

    /**
     * // POLYMORPHISM:
     * Abstract price calculation method taking quantity into account.
     * Allows specific baked good categories to offer bundle discounts (e.g. baker's dozen, multi-packs).
     */
    // POLYMORPHISM
    public abstract double calculateFinalPrice(int quantity);

    /**
     * // POLYMORPHISM:
     * Returns human-readable display category for bakery catalog navigation.
     */
    // POLYMORPHISM
    public abstract String getProductCategory();

    /**
     * // POLYMORPHISM:
     * Returns appropriate shelf-life and storage instructions (chilled, pantry, dry).
     */
    // POLYMORPHISM
    public abstract String getStorageInstructions();

    /**
     * // POLYMORPHISM:
     * Returns serving guidelines (e.g. "Serves 8-10 people" vs "Single Serving").
     */
    // POLYMORPHISM
    public abstract String getServingRecommendation();
}
