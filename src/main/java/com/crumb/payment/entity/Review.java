package com.crumb.payment.entity;

import com.crumb.customer.entity.Customer;
import com.crumb.order.entity.Order;
import com.crumb.product.entity.BakeryProduct;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Review - Entity representing customer feedback, ratings, and reviews.
 * 
 * WHY: Captures customer satisfaction star ratings and feedback for bakery products
 * and completed orders, powering social proof and quality control.
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    // ENCAPSULATION: private fields with Lombok accessors
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private BakeryProduct product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "product_name")
    private String productName;

    @Min(value = 1, message = "Rating must be between 1 and 5 stars")
    @Max(value = 5, message = "Rating must be between 1 and 5 stars")
    @Column(nullable = false)
    private int rating;

    @NotBlank(message = "Review text is required")
    @Column(name = "comment", length = 1500, nullable = false)
    private String comment;

    @Column(name = "is_approved", nullable = false)
    private boolean approved = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Review(Customer customer, BakeryProduct product, int rating, String comment) {
        this.customer = customer;
        this.customerName = customer.getFullName();
        this.product = product;
        if (product != null) {
            this.productName = product.getName();
        }
        this.rating = rating;
        this.comment = comment;
        this.approved = true;
    }
}
