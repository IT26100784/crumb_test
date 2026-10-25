package com.crumb.customcake.controller;

import com.crumb.common.auth.SessionAuthHelper;
import com.crumb.common.dto.ApiResponse;
import com.crumb.common.exception.ForbiddenException;
import com.crumb.customcake.dto.CustomCakeRequest;
import com.crumb.customcake.dto.CustomCakeResponse;
import com.crumb.customcake.dto.CustomCakeStatusUpdate;
import com.crumb.customcake.service.CustomCakeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CustomCakeController - REST endpoints for custom cake booking and price estimation.
 * 
 * WHY: Powers the interactive custom cake builder UI, price preview engine,
 * order history, and admin review queue.
 */
@RestController
@RequestMapping("/api/custom-cakes")
@RequiredArgsConstructor
public class CustomCakeController {

    private final CustomCakeService customCakeService;

    /**
     * Public endpoint: Live price preview for the frontend custom cake builder.
     * WHY: Recalculates dynamically as user toggles tiers, flavors, candles, and florals.
     */
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewPrice(@Valid @RequestBody CustomCakeRequest request) {
        double estimatedPrice = customCakeService.calculatePreviewPrice(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "estimatedPrice", estimatedPrice,
                "cakeType", request.getCakeType()
        )));
    }

    /**
     * Customer endpoint: Submit custom cake order request.
     * WHY: Persists bespoke order to kitchen queue. Requires user login.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CustomCakeResponse>> createRequest(
            @Valid @RequestBody CustomCakeRequest request,
            HttpSession session) {
        Long customerId = SessionAuthHelper.requireLoggedIn(session);
        CustomCakeResponse response = customCakeService.createRequest(customerId, request);
        return ResponseEntity.ok(ApiResponse.success("Custom cake request submitted successfully!", response));
    }

    /**
     * Customer endpoint: Retrieve current customer's custom cake booking history.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CustomCakeResponse>>> getMyRequests(HttpSession session) {
        Long customerId = SessionAuthHelper.requireLoggedIn(session);
        List<CustomCakeResponse> requests = customCakeService.getCustomerRequests(customerId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    /**
     * Endpoint to view details of a custom cake request by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomCakeResponse>> getRequestById(
            @PathVariable Long id,
            HttpSession session) {
        Long currentUserId = SessionAuthHelper.requireLoggedIn(session);
        boolean isAdmin = SessionAuthHelper.isAdmin(session);

        CustomCakeResponse cake = customCakeService.getRequestById(id);
        if (!isAdmin && !cake.getCustomerId().equals(currentUserId)) {
            throw new ForbiddenException("You are not authorized to view this custom cake order.");
        }

        return ResponseEntity.ok(ApiResponse.success(cake));
    }

    /**
     * Customer or admin cancel endpoint.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<CustomCakeResponse>> cancelRequest(
            @PathVariable Long id,
            HttpSession session) {
        Long currentUserId = SessionAuthHelper.requireLoggedIn(session);
        boolean isAdmin = SessionAuthHelper.isAdmin(session);

        CustomCakeResponse cancelled = customCakeService.cancelRequest(id, currentUserId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Custom cake request cancelled.", cancelled));
    }

    /**
     * Admin endpoint: View all custom cake requests with optional status filtering.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomCakeResponse>>> getAllRequests(
            @RequestParam(required = false) String status,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        List<CustomCakeResponse> list = customCakeService.getAllRequests(status);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * Admin endpoint: Update status (APPROVED, BAKING, READY, COMPLETED) or revise price.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CustomCakeResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomCakeStatusUpdate update,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        CustomCakeResponse updated = customCakeService.updateStatus(id, update);
        return ResponseEntity.ok(ApiResponse.success("Status updated to " + updated.getStatus(), updated));
    }

    /**
     * Admin endpoint: Delete custom cake request.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(
            @PathVariable Long id,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        customCakeService.deleteRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Custom cake request removed.", null));
    }
}
