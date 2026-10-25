package com.crumb.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CardPayment - Concrete entity representing electronic credit and debit card transactions.
 * 
 * // INHERITANCE:
 * Subclasses Payment with payment_type = 'CARD'.
 * 
 * // POLYMORPHISM:
 * Implements bank payment authorization simulation, 2.5% card interchange fee calculation,
 * and masked card receipt generation.
 */
// INHERITANCE
@Entity
@DiscriminatorValue("CARD")
@Getter
@Setter
@NoArgsConstructor
public class CardPayment extends Payment {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "card_type")
    private String cardType = "Visa";

    @Column(name = "last_four_digits")
    private String lastFourDigits = "4242";

    @Column(name = "card_holder_name")
    private String cardHolderName;

    @Column(name = "gateway_response_code")
    private String gatewayResponseCode = "AUTH_APPROVED_200";

    /**
     * // POLYMORPHISM:
     * Authorizes electronic transaction through digital payment gateway.
     */
    // POLYMORPHISM
    @Override
    public boolean processPayment(double amount) {
        setAmount(amount);
        setProcessingFee(getTransactionProcessingFee(amount));
        this.gatewayResponseCode = "AUTH_TXN_" + System.currentTimeMillis();
        setStatus("PAID");
        return true;
    }

    // POLYMORPHISM
    @Override
    public String generateReceipt() {
        return "CARD PAYMENT RECEIPT - " + cardType + " ending in *" + lastFourDigits +
               " | Cardholder: " + (cardHolderName != null ? cardHolderName : "Valued Customer") +
               " | Total: $" + getAmount() + " (Includes $" + getProcessingFee() + " interchange fee)" +
               " | Auth Code: " + gatewayResponseCode;
    }

    // POLYMORPHISM
    @Override
    public String getPaymentMethodName() {
        return cardType + " Credit / Debit Card";
    }

    // POLYMORPHISM
    @Override
    public double getTransactionProcessingFee(double amount) {
        return Math.round((amount * 0.025) * 100.0) / 100.0; // 2.5% standard card merchant fee
    }
}
