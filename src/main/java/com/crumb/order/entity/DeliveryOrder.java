package com.crumb.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


// INHERITANCE
@Entity
@DiscriminatorValue("DELIVERY")
@Getter
@Setter
@NoArgsConstructor
public class DeliveryOrder extends Order {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "special_delivery_instructions")
    private String specialDeliveryInstructions;

    @Column(name = "distance_km")
    private double distanceKm = 4.0;

    /**
     * // POLYMORPHISM:
     * Calculates delivery fee based on distance minus customer VIP tier delivery discount.
     */
    // POLYMORPHISM
    @Override
    public double calculateDeliveryFee() {
        double fee = 4.50; // Base city delivery fee
        if (distanceKm > 3.0) {
            fee += (distanceKm - 3.0) * 0.80; // $0.80 per additional kilometer
        }

        // Apply polymorphic customer delivery discount (e.g. $3.00 off for VIPs)
        if (getCustomer() != null) {
            double discount = getCustomer().getDeliveryFeeDiscount();
            fee = Math.max(0.0, fee - discount);
        }

        return Math.round(fee * 100.0) / 100.0;
    }

    // POLYMORPHISM
    @Override
    public String getDeliverySummary() {
        return "Doorstep Courier Delivery to: " + (deliveryAddress != null ? deliveryAddress : "Specified Address");
    }

    // POLYMORPHISM
    @Override
    public String getFulfillmentTimeEstimate() {
        return "Estimated delivery in 45-60 minutes via Crumb Express Courier";
    }
}
