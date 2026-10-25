package com.crumb.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CashPayment - Concrete entity representing physical currency transactions.
 * 
 * // INHERITANCE:
 * Subclasses Payment with payment_type = 'CASH'.
 * 
 * // POLYMORPHISM:
 * Implements cash tendered validation, change calculation, and zero processing fee.
 */
// INHERITANCE
@Entity
@DiscriminatorValue("CASH")
@Getter
@Setter
@NoArgsConstructor
public class CashPayment extends Payment {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "cash_tendered")
    private double cashTendered;

    @Column(name = "change_due")
    private double changeDue = 0.0;

    @Column(name = "is_collected_at_counter")
    private boolean collectedAtCounter = true;

    @Column(name = "collector_staff_name")
    private String collectorStaffName = "Crumb Cashier";

    /**
     * // POLYMORPHISM:
     * Validates that cash received is greater than or equal to bill total, computing change.
     */
    // POLYMORPHISM
    @Override
    public boolean processPayment(double amount) {
        if (cashTendered < amount) {
            setStatus("FAILED");
            return false;
        }
        this.changeDue = Math.round((cashTendered - amount) * 100.0) / 100.0;
        setAmount(amount);
        setProcessingFee(0.0);
        setStatus("PAID");
        return true;
    }

    // POLYMORPHISM
    @Override
    public String generateReceipt() {
        return "CASH PAYMENT RECEIPT - Total: $" + getAmount() +
               " | Tendered: $" + cashTendered + " | Change Due: $" + changeDue +
               " | Collected by: " + collectorStaffName;
    }

    // POLYMORPHISM
    @Override
    public String getPaymentMethodName() {
        return "Cash on Delivery / In-Store Cash";
    }

    // POLYMORPHISM
    @Override
    public double getTransactionProcessingFee(double amount) {
        return 0.0; // Cash payments incur no payment gateway processing fees
    }
}
