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

@Entity
@Table(name = "orders")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "order_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Order {

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

    /*
    Calculates delivery / logistics fees for this order type.
    Delivery orders compute courier distance fees (minus VIP discount),
    while Pickup orders always return 0.0.
    */
    public abstract double calculateDeliveryFee();

    public abstract String getDeliverySummary();

    public abstract String getFulfillmentTimeEstimate();
}
