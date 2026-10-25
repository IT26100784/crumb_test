package com.crumb.customcake.repository;

import com.crumb.customcake.entity.CustomCake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CustomCakeRepository - Data access interface for custom cake requests.
 * 
 * WHY: Provides query methods for customer order histories, status tracking,
 * and admin kitchen queue monitoring.
 */
@Repository
public interface CustomCakeRepository extends JpaRepository<CustomCake, Long> {

    /**
     * Finds all custom cake requests submitted by a specific customer.
     */
    List<CustomCake> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Finds custom cake requests filtered by status (PENDING_REVIEW, APPROVED, BAKING, etc.).
     */
    List<CustomCake> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Finds all requests ordered with the newest first.
     */
    List<CustomCake> findAllByOrderByCreatedAtDesc();

    /**
     * Counts requests by status for the admin summary cards.
     */
    long countByStatus(String status);
}
