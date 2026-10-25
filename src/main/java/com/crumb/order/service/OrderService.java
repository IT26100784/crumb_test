package com.crumb.order.service;

import com.crumb.order.dto.OrderCreateRequest;
import com.crumb.order.dto.OrderResponse;
import com.crumb.order.dto.OrderStatusUpdateRequest;
import com.crumb.order.entity.Order;

import java.util.List;

/**
 * OrderService - Business contract for retail order placement, tracking, and management.
 * 
 * // ABSTRACTION:
 * Exposes core ordering operations while concealing entity mapping, line item subtotals,
 * and polymorphic fee calculation behind an abstract contract.
 */
// ABSTRACTION
public interface OrderService {

    /**
     * Creates and places a new customer order from shopping cart items.
     */
    OrderResponse placeOrder(Long customerId, OrderCreateRequest request);

    /**
     * Retrieves all orders placed by a specific customer.
     */
    List<OrderResponse> getCustomerOrders(Long customerId);

    /**
     * Admin query: retrieves all orders with optional status and orderType filtering.
     */
    List<OrderResponse> getAllOrders(String status, String orderType);

    /**
     * Retrieves single order by primary key ID.
     */
    OrderResponse getOrderById(Long id);

    /**
     * Retrieves single order by its public reference tracking number.
     */
    OrderResponse getOrderByNumber(String orderNumber);

    /**
     * Retrieves raw Order entity by ID (internal use by Booking and Payment modules).
     */
    Order getOrderEntity(Long id);

    /**
     * Updates order fulfillment status (PENDING -> CONFIRMED -> BAKING -> READY -> DELIVERED/COMPLETED).
     */
    OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request);

    /**
     * Cancels an order (by customer before preparation or by admin).
     */
    OrderResponse cancelOrder(Long id, Long customerId, boolean isAdmin);

    /**
     * Deletes an order record (admin only).
     */
    void deleteOrder(Long id);

    /**
     * Calculates total gross revenue across all successful orders.
     */
    double getTotalRevenue();
}
