package com.crumb.customer.controller;

import com.crumb.common.auth.SessionAuthHelper;
import com.crumb.common.dto.ApiResponse;
import com.crumb.common.exception.ForbiddenException;
import com.crumb.customer.dto.CustomerResponse;
import com.crumb.customer.dto.CustomerUpdateRequest;
import com.crumb.customer.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CustomerController - CRUD endpoints for user profiles and admin customer management.
 * 
 * WHY: Exposes REST APIs for updating personal profiles, VIP tier upgrades,
 * and admin customer search, activation toggle, and account deletion.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Retrieves profile details for a specific customer ID.
     * WHY: Users can view their own profile; admins can view any profile.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @PathVariable Long id,
            HttpSession session) {
        Long currentUserId = SessionAuthHelper.requireLoggedIn(session);
        boolean isAdmin = SessionAuthHelper.isAdmin(session);

        if (!isAdmin && !currentUserId.equals(id)) {
            throw new ForbiddenException("You are not authorized to view another customer's profile.");
        }

        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    /**
     * Updates profile details (name, phone, address, optional password).
     * WHY: Empowers customers to keep their contact info fresh.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request,
            HttpSession session) {
        Long currentUserId = SessionAuthHelper.requireLoggedIn(session);
        boolean isAdmin = SessionAuthHelper.isAdmin(session);

        if (!isAdmin && !currentUserId.equals(id)) {
            throw new ForbiddenException("You are not authorized to edit another customer's profile.");
        }

        CustomerResponse updated = customerService.updateProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully!", updated));
    }

    /**
     * Upgrades regular customer to VIP Gold membership.
     * WHY: Demonstrates dynamic polymorphic tier upgrade at runtime.
     */
    @PostMapping("/{id}/upgrade")
    public ResponseEntity<ApiResponse<CustomerResponse>> upgradeToPremium(
            @PathVariable Long id,
            HttpSession session) {
        Long currentUserId = SessionAuthHelper.requireLoggedIn(session);
        boolean isAdmin = SessionAuthHelper.isAdmin(session);

        if (!isAdmin && !currentUserId.equals(id)) {
            throw new ForbiddenException("You cannot upgrade someone else's account.");
        }

        CustomerResponse upgraded = customerService.upgradeToPremium(id);
        return ResponseEntity.ok(ApiResponse.success("Congratulations! Upgraded to VIP Gold Membership.", upgraded));
    }

    /**
     * Admin endpoint: Search or list all customer accounts.
     * WHY: Provides data for the admin customers table with live filtering.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(
            @RequestParam(required = false) String query,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        List<CustomerResponse> customers = customerService.searchCustomers(query);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * Admin endpoint: Toggle customer active / suspended status.
     * WHY: Allows bakery management to moderate accounts without deleting data.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CustomerResponse>> toggleStatus(
            @PathVariable Long id,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        CustomerResponse customer = customerService.toggleCustomerStatus(id);
        return ResponseEntity.ok(ApiResponse.success(
                customer.isActive() ? "Account activated." : "Account suspended.", customer
        ));
    }

    /**
     * Admin endpoint: Delete customer account.
     * WHY: Satisfies complete CRUD requirements for customer management.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable Long id,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer removed successfully.", null));
    }
}
