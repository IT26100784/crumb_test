package com.crumb.order.repository;

import com.crumb.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OrderRepository - Data access interface for Order entities.
 * 
 * WHY: Provides query methods for customer order histories, order numbers,
 * administrative queue monitoring, and revenue calculations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds orders placed by a specific customer, newest first.
     */
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Finds order by its unique tracking reference number.
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Finds orders filtered by status (PENDING, BAKING, READY, etc.).
     */
    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Retrieves all orders sorted newest first.
     */
    List<Order> findAllByOrderByCreatedAtDesc();

    /**
     * Counts active orders by status.
     */
    long countByStatus(String status);

    /**
     * Computes total revenue from all non-cancelled orders for dashboard metrics.
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.status != 'CANCELLED'")
    double calculateTotalRevenue();
}
