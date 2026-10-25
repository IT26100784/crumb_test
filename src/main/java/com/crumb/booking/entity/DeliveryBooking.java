package com.crumb.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DeliveryBooking - Concrete entity representing a scheduled dispatch and on-site delivery reservation.
 * 
 * // INHERITANCE:
 * Subclasses Booking with booking_type = 'DELIVERY'.
 * 
 * // POLYMORPHISM:
 * Implements scheduled delivery fee calculation with optional white-glove event table setup.
 */
// INHERITANCE
@Entity
@DiscriminatorValue("DELIVERY")
@Getter
@Setter
@NoArgsConstructor
public class DeliveryBooking extends Booking {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "destination_address")
    private String destinationAddress;

    @Column(name = "recipient_contact_phone")
    private String recipientContactPhone;

    @Column(name = "is_white_glove_setup")
    private boolean whiteGloveSetup = false;

    /**
     * // POLYMORPHISM:
     * Base scheduled delivery booking is $5.00. White glove event table setup adds $20.00.
     * Applies VIP customer delivery fee discounts.
     */
    // POLYMORPHISM
    @Override
    public double calculateBookingFee() {
        double fee = 5.00;
        if (whiteGloveSetup) {
            fee += 20.00; // Cake stand assembly, chilling check, on-site staging
        }

        if (getCustomer() != null) {
            double discount = getCustomer().getDeliveryFeeDiscount();
            fee = Math.max(0.0, fee - discount);
        }

        return Math.round(fee * 100.0) / 100.0;
    }

    // POLYMORPHISM
    @Override
    public String getBookingInstructions() {
        return "Courier dispatch scheduled to " + (destinationAddress != null ? destinationAddress : "Your Address") +
               " during window [" + getTimeSlot() + "]. Contact recipient at: " +
               (recipientContactPhone != null ? recipientContactPhone : "Provided Phone");
    }

    // POLYMORPHISM
    @Override
    public String getBookingTypeName() {
        return "Scheduled Courier Delivery";
    }
}
