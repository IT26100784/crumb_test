package com.crumb.order.entity;

import com.crumb.product.entity.BakeryProduct;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    // ENCAPSULATION: private fields with Lombok accessors
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private BakeryProduct product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(name = "item_subtotal", nullable = false)
    private double itemSubtotal;

    public OrderItem(Order order, BakeryProduct product, int quantity, double unitPrice) {
        this.order = order;
        this.product = product;
        this.productName = product.getName();
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.itemSubtotal = Math.round((unitPrice * quantity) * 100.0) / 100.0;
    }
}
