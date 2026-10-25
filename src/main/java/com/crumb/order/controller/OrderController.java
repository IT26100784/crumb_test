package com.crumb.order.controller;

import com.crumb.common.auth.SessionAuthHelper;
import com.crumb.common.dto.ApiResponse;
import com.crumb.common.exception.ForbiddenException;
import com.crumb.order.dto.OrderCreateRequest;
import com.crumb.order.dto.OrderResponse;
import com.crumb.order.dto.OrderStatusUpdateRequest;
import com.crumb.order.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OrderController - REST API for retail bakery ordering, status timeline, and fulfillment.
 * 
 * WHY: Exposes endpoints for shopping cart checkout, customer order histories,
 * live order tracking, and administrative dispatch operations.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Customer endpoint: Place a new order from cart.
     * WHY: Persists order, calculates discounts/delivery fees, and deducts inventory.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderCreateRequest request,
            HttpSession session) {
        Long customerId = SessionAuthHelper.requireLoggedIn(session);
        OrderResponse response = orderService.placeOrder(customerId, request);
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully! Reference: " + response.getOrderNumber(), response));
    }

    /**
     * Customer endpoint: View personal order history.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(HttpSession session) {
        Long customerId = SessionAuthHelper.requireLoggedIn(session);
        List<OrderResponse> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Tracking endpoint: View order status timeline by tracking order number.
     * Accessible by customers or guests with the receipt code.
     */
    @GetMapping("/track/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(@PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * Retrieve detailed order by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id,
            HttpSession session) {
        Long currentUserId = SessionAuthHelper.requireLoggedIn(session);
        boolean isAdmin = SessionAuthHelper.isAdmin(session);

        OrderResponse order = orderService.getOrderById(id);
        if (!isAdmin && !order.getCustomerId().equals(currentUserId)) {
            throw new ForbiddenException("You are not authorized to view another customer's order.");
        }

        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * Cancel an order prior to kitchen preparation.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            HttpSession session) {
        Long currentUserId = SessionAuthHelper.requireLoggedIn(session);
        boolean isAdmin = SessionAuthHelper.isAdmin(session);

        OrderResponse cancelled = orderService.cancelOrder(id, currentUserId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Order has been cancelled.", cancelled));
    }

    /**
     * Admin endpoint: View all orders with optional status and fulfillment type filters.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderType,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        List<OrderResponse> orders = orderService.getAllOrders(status, orderType);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Admin endpoint: Advance order fulfillment status (CONFIRMED -> BAKING -> READY -> DELIVERED/COMPLETED).
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        OrderResponse updated = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated to " + updated.getStatus(), updated));
    }

    /**
     * Admin endpoint: Delete order record.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @PathVariable Long id,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order record deleted.", null));
    }

    /**
     * Admin endpoint: Get gross revenue metric.
     */
    @GetMapping("/stats/revenue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueStats(HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        double totalRevenue = orderService.getTotalRevenue();
        return ResponseEntity.ok(ApiResponse.success(Map.of("totalRevenue", totalRevenue)));
    }
}
