package com.crumb.payment.dto;

import com.crumb.payment.entity.CardPayment;
import com.crumb.payment.entity.CashPayment;
import com.crumb.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PaymentResponse - Output presentation for transaction receipts.
 * 
 * WHY: Presents serialized payment status, amounts, and polymorphic receipt texts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    // ENCAPSULATION: private fields with getters/setters
    private Long id;
    private String paymentReference;
    private Long customerId;
    private String customerName;
    private Long orderId;
    private String orderNumber;
    private Long customCakeId;
    private double amount;
    private double processingFee;
    private String status;
    private LocalDateTime paymentDate;
    private String paymentType;

    // POLYMORPHIC calculated fields
    private String paymentMethodName;
    private String receiptSummary;

    // Cash details
    private Double cashTendered;
    private Double changeDue;
    private String collectorStaffName;

    // Card details
    private String cardType;
    private String lastFourDigits;
    private String cardHolderName;
    private String gatewayResponseCode;

    /**
     * Converts a Payment entity into a PaymentResponse DTO.
     * Demonstrates POLYMORPHISM by calling polymorphic methods on Payment.
     */
    public static PaymentResponse fromEntity(Payment payment) {
        if (payment == null) return null;

        PaymentResponseBuilder builder = PaymentResponse.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .customerId(payment.getCustomer() != null ? payment.getCustomer().getId() : null)
                .customerName(payment.getCustomer() != null ? payment.getCustomer().getFullName() : "N/A")
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
                .customCakeId(payment.getCustomCake() != null ? payment.getCustomCake().getId() : null)
                .amount(payment.getAmount())
                .processingFee(payment.getProcessingFee())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                // POLYMORPHIC CALLS:
                .paymentMethodName(payment.getPaymentMethodName())
                .receiptSummary(payment.generateReceipt());

        if (payment instanceof CashPayment cash) {
            builder.paymentType("CASH")
                   .cashTendered(cash.getCashTendered())
                   .changeDue(cash.getChangeDue())
                   .collectorStaffName(cash.getCollectorStaffName());
        } else if (payment instanceof CardPayment card) {
            builder.paymentType("CARD")
                   .cardType(card.getCardType())
                   .lastFourDigits(card.getLastFourDigits())
                   .cardHolderName(card.getCardHolderName())
                   .gatewayResponseCode(card.getGatewayResponseCode());
        }

        return builder.build();
    }
}
