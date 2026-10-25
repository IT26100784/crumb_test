package com.crumb.payment.controller;

import com.crumb.common.auth.SessionAuthHelper;
import com.crumb.common.dto.ApiResponse;
import com.crumb.common.exception.ForbiddenException;
import com.crumb.payment.dto.PaymentRequest;
import com.crumb.payment.dto.PaymentResponse;
import com.crumb.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PaymentController - REST API for checkout settlements, receipts, and refund management.
 * 
 * WHY: Processes customer transactions (Cash on Delivery vs Digital Card),
 * issues invoices, and allows admin transaction auditing.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Customer endpoint: Process payment for an order or custom cake.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request,
            HttpSession session) {
        Long customerId = SessionAuthHelper.requireLoggedIn(session);
        PaymentResponse response = paymentService.processPayment(customerId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully! Reference: " + response.getPaymentReference(), response));
    }

    /**
     * Customer endpoint: View personal payment receipts history.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(HttpSession session) {
        Long customerId = SessionAuthHelper.requireLoggedIn(session);
        List<PaymentResponse> list = paymentService.getCustomerPayments(customerId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * Retrieve payment receipt by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable Long id,
            HttpSession session) {
        Long currentUserId = SessionAuthHelper.requireLoggedIn(session);
        boolean isAdmin = SessionAuthHelper.isAdmin(session);

        PaymentResponse payment = paymentService.getPaymentById(id);
        if (!isAdmin && !payment.getCustomerId().equals(currentUserId)) {
            throw new ForbiddenException("You are not authorized to view this transaction.");
        }

        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    /**
     * Lookup payment receipt by public reference code.
     */
    @GetMapping("/ref/{reference}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReference(@PathVariable String reference) {
        PaymentResponse payment = paymentService.getPaymentByReference(reference);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    /**
     * Admin endpoint: View all financial transactions with optional status and method filters.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentType,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        List<PaymentResponse> list = paymentService.getAllPayments(status, paymentType);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * Admin endpoint: Process transaction refund.
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable Long id,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        PaymentResponse refunded = paymentService.refundPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction refunded successfully.", refunded));
    }

    /**
     * Admin endpoint: Total collected payment revenue.
     */
    @GetMapping("/stats/revenue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentRevenueStats(HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        double total = paymentService.getTotalCollectedRevenue();
        return ResponseEntity.ok(ApiResponse.success(Map.of("totalCollected", total)));
    }
}
