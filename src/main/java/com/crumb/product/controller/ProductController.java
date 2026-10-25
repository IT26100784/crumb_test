package com.crumb.product.controller;

import com.crumb.common.auth.SessionAuthHelper;
import com.crumb.common.dto.ApiResponse;
import com.crumb.product.dto.ProductRequest;
import com.crumb.product.dto.ProductResponse;
import com.crumb.product.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ProductController - REST API for bakery products (Cakes, Pastries, Breads).
 * 
 * WHY: Provides public catalog browsing and search endpoints for customers,
 * alongside protected CRUD operations for administrators.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Public endpoint: Retrieve products with optional search query and category filtering.
     * WHY: Powers the storefront catalog, category filter chips, and search input.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean availableOnly,
            HttpSession session) {
        // If unauthenticated or customer, only show available items unless admin requests all
        boolean isAdmin = SessionAuthHelper.isAdmin(session);
        Boolean filterAvailable = (availableOnly != null) ? availableOnly : (!isAdmin);

        List<ProductResponse> products = productService.getProducts(keyword, type, filterAvailable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Public endpoint: Retrieve product details by ID.
     * WHY: Used by product cards and the quick-view modal.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * Public endpoint: Calculates final price for a given quantity applying polymorphic volume rules.
     * WHY: Allows frontend cart / quick-view modals to show live discounted prices.
     */
    @GetMapping("/{id}/price")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculatePrice(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int quantity) {
        double finalPrice = productService.calculateFinalPrice(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "productId", id,
                "quantity", quantity,
                "finalPrice", finalPrice
        )));
    }

    /**
     * Admin endpoint: Create a new bakery product.
     * WHY: Enables bakery managers to expand their menu catalog.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.success("Product created successfully!", created));
    }

    /**
     * Admin endpoint: Update an existing product.
     * WHY: Allows updating prices, descriptions, stock, and photos.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        ProductResponse updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully!", updated));
    }

    /**
     * Admin endpoint: Toggle product availability.
     * WHY: Quickly mark sold-out items without deleting them.
     */
    @PutMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleAvailability(
            @PathVariable Long id,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        ProductResponse updated = productService.toggleAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(
                updated.isAvailable() ? "Product marked as In Stock." : "Product marked as Sold Out.",
                updated
        ));
    }

    /**
     * Admin endpoint: Delete product by ID.
     * WHY: Fulfills full CRUD delete specification for products.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product removed from catalog.", null));
    }
}
