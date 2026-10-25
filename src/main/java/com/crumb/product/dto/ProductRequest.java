package com.crumb.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ProductRequest - Data transfer object for creating and modifying bakery products.
 * 
 * WHY: Validates product attributes submitted by the bakery manager from the admin portal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    // ENCAPSULATION: private fields with getters/setters
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.10", message = "Price must be at least 0.10")
    private Double basePrice;

    private String imageUrl;

    @Builder.Default
    private int stockQuantity = 20;

    private String allergens;

    @NotBlank(message = "Product type (CAKE, PASTRY, BREAD) is required")
    private String productType;

    // Cake-specific fields
    private String frostingType;
    private Integer layers;

    // Pastry-specific fields
    private String pastryType;
    private Boolean warmable;

    // Bread-specific fields
    private Integer loafWeightGrams;
    private Boolean sourdough;
}
