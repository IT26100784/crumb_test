package com.crumb.customer.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RegularCustomer - Represents standard retail bakery shoppers.
 * 
 * // INHERITANCE:
 * Inherits common fields and methods from the abstract Customer superclass.
 * Stored with customer_type = 'REGULAR'.
 * 
 * // POLYMORPHISM:
 * Overrides calculateDiscount, getCustomerBadge, getMaxActiveBookings, and getDeliveryFeeDiscount
 * providing regular consumer tier behavior.
 */
// INHERITANCE
@Entity
@DiscriminatorValue("REGULAR")
@Getter
@Setter
@NoArgsConstructor
public class RegularCustomer extends Customer {

    // ENCAPSULATION: private fields with Lombok accessors
    private String preferredBranch = "Main Bakery";

    public RegularCustomer(String fullName, String email, String phone, String password, String address) {
        super();
        setFullName(fullName);
        setEmail(email);
        setPhone(phone);
        setPassword(password);
        setAddress(address);
        setRole("CUSTOMER");
        setLoyaltyPoints(0);
        setActive(true);
    }

    /**
     * // POLYMORPHISM:
     * Regular members earn points but receive 0% flat discount on subtotal.
     */
    // POLYMORPHISM
    @Override
    public double calculateDiscount(double subtotal) {
        // Standard customers get 0% base discount (they earn standard loyalty points instead)
        return 0.0;
    }

    /**
     * // POLYMORPHISM:
     * Returns standard retail membership badge text.
     */
    // POLYMORPHISM
    @Override
    public String getCustomerBadge() {
        return "Classic Member";
    }

    /**
     * // POLYMORPHISM:
     * Standard customers can have up to 3 active bookings at one time.
     */
    // POLYMORPHISM
    @Override
    public int getMaxActiveBookings() {
        return 3;
    }

    /**
     * // POLYMORPHISM:
     * Standard members pay standard delivery fee without discount.
     */
    // POLYMORPHISM
    @Override
    public double getDeliveryFeeDiscount() {
        return 0.0;
    }
}
