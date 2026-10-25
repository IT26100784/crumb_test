package com.crumb.customer.service;

import com.crumb.customer.dto.CustomerResponse;
import com.crumb.customer.dto.CustomerUpdateRequest;
import com.crumb.customer.dto.LoginRequest;
import com.crumb.customer.dto.RegisterRequest;
import com.crumb.customer.entity.Customer;

import java.util.List;

/**
 * CustomerService - Business logic contract for customer registration, authentication, and management.
 * 
 * // ABSTRACTION:
 * This interface decouples business logic specifications from their underlying database operations.
 * Controllers interact strictly with this abstraction rather than concrete implementations.
 */
// ABSTRACTION
public interface CustomerService {

    /**
     * Registers a new customer (Regular or Premium) with BCrypt password hashing.
     */
    CustomerResponse register(RegisterRequest request);

    /**
     * Authenticates credentials and returns the logged-in customer details.
     */
    CustomerResponse authenticate(LoginRequest request);

    /**
     * Retrieves customer profile by ID.
     */
    CustomerResponse getCustomerById(Long id);

    /**
     * Retrieves raw Customer entity by ID (internal use by other services).
     */
    Customer getCustomerEntity(Long id);

    /**
     * Updates an existing customer profile.
     */
    CustomerResponse updateProfile(Long id, CustomerUpdateRequest request);

    /**
     * Upgrades a regular customer to a VIP Premium membership.
     */
    CustomerResponse upgradeToPremium(Long id);

    /**
     * Searches customers by keyword or returns all if query is empty.
     */
    List<CustomerResponse> searchCustomers(String query);

    /**
     * Admin toggle to activate or deactivate a customer account.
     */
    CustomerResponse toggleCustomerStatus(Long id);

    /**
     * Deletes a customer account by ID (admin only).
     */
    void deleteCustomer(Long id);

    /**
     * Ensures default admin user exists on application startup.
     */
    void initDefaultAdmin();
}
