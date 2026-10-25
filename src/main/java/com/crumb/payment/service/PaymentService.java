package com.crumb.payment.service;

import com.crumb.payment.dto.PaymentRequest;
import com.crumb.payment.dto.PaymentResponse;

import java.util.List;

/**
 * PaymentService - Business contract for processing payments and managing receipts.
 * 
 * // ABSTRACTION:
 * Defines business specifications for transaction settlement and refund operations.
 */
// ABSTRACTION
public interface PaymentService {

    /**
     * Processes a payment for an order or custom cake commission.
     */
    PaymentResponse processPayment(Long customerId, PaymentRequest request);

    /**
     * Retrieves all payments processed for a customer.
     */
    List<PaymentResponse> getCustomerPayments(Long customerId);

    /**
     * Admin endpoint: Lists all payments with optional status and method filters.
     */
    List<PaymentResponse> getAllPayments(String status, String paymentType);

    /**
     * Retrieves payment by primary key.
     */
    PaymentResponse getPaymentById(Long id);

    /**
     * Retrieves payment by public transaction reference code.
     */
    PaymentResponse getPaymentByReference(String reference);

    /**
     * Admin refund endpoint.
     */
    PaymentResponse refundPayment(Long id);

    /**
     * Total processed payment volume.
     */
    double getTotalCollectedRevenue();
}
