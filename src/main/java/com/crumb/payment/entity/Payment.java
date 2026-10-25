package com.crumb.payment.entity;

import com.crumb.customcake.entity.CustomCake;
import com.crumb.customer.entity.Customer;
import com.crumb.order.entity.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Payment - Abstract Base Entity representing a monetary checkout transaction.
 * 
 * // ABSTRACTION:
 * Encapsulates core financial transaction attributes (amount, customer, order links, status)
 * and declares abstract polymorphic execution, receipt generation, and fee methods.
 * 
 * // INHERITANCE:
 * Uses JPA SINGLE_TABLE inheritance with discriminator column "payment_type".
 * CashPayment and CardPayment entities reside in the shared "payments" table.
 */
// ABSTRACTION
// INHERITANCE
@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Payment {

    // // ENCAPSULATION:
    // All attributes are private. Access is mediated through Lombok getters and setters.
    // ENCAPSULATION
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_reference", nullable = false, unique = true)
    private String paymentReference;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "custom_cake_id")
    private CustomCake customCake;

    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    @Column(nullable = false)
    private double amount;

    @Column(name = "processing_fee", nullable = false)
    private double processingFee = 0.0;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, PAID, REFUNDED, FAILED

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Column(name = "payment_type", insertable = false, updatable = false)
    private String paymentType;

    /**
     * // POLYMORPHISM:
     * Executes the payment workflow according to the specific tender method.
     * Cash verifies tendered bills and computes change; Card authorizes digital gateway tokens.
     */
    // POLYMORPHISM
    public abstract boolean processPayment(double amount);

    /**
     * // POLYMORPHISM:
     * Generates a payment receipt summary for customer invoices.
     */
    // POLYMORPHISM
    public abstract String generateReceipt();

    /**
     * // POLYMORPHISM:
     * Returns human-readable payment method name.
     */
    // POLYMORPHISM
    public abstract String getPaymentMethodName();

    /**
     * // POLYMORPHISM:
     * Returns the gateway or merchant transaction fee for this payment type.
     */
    // POLYMORPHISM
    public abstract double getTransactionProcessingFee(double amount);
}
