package com.crumb.customer.repository;

import com.crumb.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CustomerRepository - Data access interface for Customer entities.
 * 
 * WHY: Provides standard Spring Data JPA query methods and custom finders
 * for authentication and customer search across regular and premium customers.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Finds a customer by their unique email address.
     * WHY: Used during login and authentication validation.
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Checks if an email address is already registered.
     * WHY: Prevents duplicate accounts during registration.
     */
    boolean existsByEmail(String email);

    /**
     * Searches customers by matching either full name or email (case-insensitive).
     * WHY: Enables live search in the admin customer management table.
     */
    List<Customer> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    /**
     * Counts customers by their account role (e.g. CUSTOMER vs ADMIN).
     * WHY: Used by the admin dashboard for customer statistics.
     */
    long countByRole(String role);
}
