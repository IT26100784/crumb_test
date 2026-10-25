package com.crumb.order.entity;

import com.crumb.customer.entity.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order - Abstract Base Entity representing a retail bakery order.
 * 
 * // ABSTRACTION:
 * Defines common order attributes (items, subtotal, discount, totals, status)
 * and declares abstract polymorphic fulfillment methods implemented by DeliveryOrder and PickupOrder.
 * 
 * // INHERITANCE:
 * Uses JPA SINGLE_TABLE inheritance with discriminator column "order_type".
 * Both DeliveryOrder and PickupOrder records share the "orders" table.
 */
// ABSTRACTION
// INHERITANCE
@Entity
@Table(name = "orders")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "order_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Order {

    // // ENCAPSULATION:
    // All attributes are private. Access is mediated by Lombok-generated getters and setters.
    // ENCAPSULATION
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private double subtotal;

    @Column(name = "discount_amount", nullable = false)
    private double discountAmount = 0.0;

    @Column(name = "delivery_fee", nullable = false)
    private double deliveryFee = 0.0;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING -> CONFIRMED -> BAKING -> READY -> DELIVERED/COMPLETED or CANCELLED

    @Column(name = "order_notes", length = 500)
    private String orderNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "order_type", insertable = false, updatable = false)
    private String orderType;

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * // POLYMORPHISM:
     * Calculates delivery / logistics fees for this order type.
     * Delivery orders compute courier distance fees (minus VIP discount),
     * while Pickup orders always return 0.0.
     */
    // POLYMORPHISM
    public abstract double calculateDeliveryFee();

    /**
     * // POLYMORPHISM:
     * Returns a human-readable fulfillment summary (dispatch address vs store pickup counter).
     */
    // POLYMORPHISM
    public abstract String getDeliverySummary();

    /**
     * // POLYMORPHISM:
     * Returns estimated preparation and dispatch timing.
     */
    // POLYMORPHISM
    public abstract String getFulfillmentTimeEstimate();
}
