package com.crumb.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PaymentRequest - Checkout payment processing payload.
 * 
 * WHY: Captures tender method (CASH vs CARD) and credentials to settle invoices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    // ENCAPSULATION: private fields with getters/setters
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private Double amount;

    @NotBlank(message = "Payment type (CASH or CARD) is required")
    private String paymentType;

    private Long orderId;
    private Long customCakeId;

    // Cash fields
    private Double cashTendered;
    private String collectorStaffName;

    // Card fields
    private String cardType;
    private String cardNumber;
    private String cardHolderName;
    private String expiry;
    private String cvv;
}
