package com.crumb.product.service;

import com.crumb.product.dto.ProductRequest;
import com.crumb.product.dto.ProductResponse;
import com.crumb.product.entity.BakeryProduct;

import java.util.List;

/**
 * ProductService - Business contract for bakery product catalog operations.
 * 
 * // ABSTRACTION:
 * Defines business methods for catalog searching, product CRUD, and polymorphic price calculation.
 */
// ABSTRACTION
public interface ProductService {

    /**
     * Retrieves products with optional keyword and type filtering.
     */
    List<ProductResponse> getProducts(String keyword, String type, Boolean availableOnly);

    /**
     * Retrieves a single product by its ID.
     */
    ProductResponse getProductById(Long id);

    /**
     * Retrieves the underlying BakeryProduct entity for cross-module relations.
     */
    BakeryProduct getProductEntity(Long id);

    /**
     * Creates a new product (Cake, Pastry, or Bread).
     */
    ProductResponse createProduct(ProductRequest request);

    /**
     * Updates an existing bakery product.
     */
    ProductResponse updateProduct(Long id, ProductRequest request);

    /**
     * Toggles availability of a product (in stock / out of stock).
     */
    ProductResponse toggleAvailability(Long id);

    /**
     * Deletes a product from the database (admin only).
     */
    void deleteProduct(Long id);

    /**
     * Calculates final price using polymorphic volume pricing rules.
     */
    double calculateFinalPrice(Long id, int quantity);

    /**
     * Seeds initial realistic bakery products on first launch if empty.
     */
    void initSampleProducts();
}
