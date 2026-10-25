package com.crumb.customcake.service;

import com.crumb.customcake.dto.CustomCakeRequest;
import com.crumb.customcake.dto.CustomCakeResponse;
import com.crumb.customcake.dto.CustomCakeStatusUpdate;
import com.crumb.customcake.entity.CustomCake;

import java.util.List;

/**
 * CustomCakeService - Business contract for bespoke cake design and quoting.
 * 
 * // ABSTRACTION:
 * Defines business operations for cake customization, live quotation calculation,
 * order tracking, and administrative review.
 */
// ABSTRACTION
public interface CustomCakeService {

    /**
     * Submits a new custom cake design request.
     */
    CustomCakeResponse createRequest(Long customerId, CustomCakeRequest request);

    /**
     * Calculates instant live price estimation for the builder without saving to DB.
     */
    double calculatePreviewPrice(CustomCakeRequest request);

    /**
     * Retrieves all custom cake requests submitted by a specific customer.
     */
    List<CustomCakeResponse> getCustomerRequests(Long customerId);

    /**
     * Retrieves all custom cake requests with optional status filtering (admin).
     */
    List<CustomCakeResponse> getAllRequests(String status);

    /**
     * Retrieves request details by ID.
     */
    CustomCakeResponse getRequestById(Long id);

    /**
     * Retrieves raw entity by ID for cross-component relations.
     */
    CustomCake getRequestEntity(Long id);

    /**
     * Updates cake status (APPROVED, BAKING, READY, COMPLETED) and optional revised price.
     */
    CustomCakeResponse updateStatus(Long id, CustomCakeStatusUpdate update);

    /**
     * Cancels a pending request.
     */
    CustomCakeResponse cancelRequest(Long id, Long customerId, boolean isAdmin);

    /**
     * Deletes a custom cake record (admin only).
     */
    void deleteRequest(Long id);
}
