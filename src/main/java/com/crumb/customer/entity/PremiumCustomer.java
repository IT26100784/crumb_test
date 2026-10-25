package com.crumb.customer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * PremiumCustomer - Represents VIP patrons with exclusive benefits and pricing.
 * 
 * // INHERITANCE:
 * Subclasses Customer and is stored with customer_type = 'PREMIUM'.
 * 
 * // POLYMORPHISM:
 * Overrides calculateDiscount (10% discount on orders), getCustomerBadge,
 * getMaxActiveBookings (up to 10 active bookings), and getDeliveryFeeDiscount ($3 off).
 */
// INHERITANCE
@Entity
@DiscriminatorValue("PREMIUM")
@Getter
@Setter
@NoArgsConstructor
public class PremiumCustomer extends Customer {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "membership_expiry")
    private LocalDate membershipExpiry = LocalDate.now().plusYears(1);

    @Column(name = "vip_concierge_note")
    private String vipConciergeNote = "Priority custom cake consultation";

    public PremiumCustomer(String fullName, String email, String phone, String password, String address) {
        super();
        setFullName(fullName);
        setEmail(email);
        setPhone(phone);
        setPassword(password);
        setAddress(address);
        setRole("CUSTOMER");
        setLoyaltyPoints(100); // 100 bonus welcome points for VIPs
        setActive(true);
        this.membershipExpiry = LocalDate.now().plusYears(1);
    }

    /**
     * // POLYMORPHISM:
     * VIP patrons receive a generous 10% discount on every order subtotal.
     */
    // POLYMORPHISM
    @Override
    public double calculateDiscount(double subtotal) {
        return Math.round((subtotal * 0.10) * 100.0) / 100.0;
    }

    /**
     * // POLYMORPHISM:
     * Returns the VIP Gold Member badge displayed in the UI.
     */
    // POLYMORPHISM
    @Override
    public String getCustomerBadge() {
        return "VIP Gold Member";
    }

    /**
     * // POLYMORPHISM:
     * Premium members can maintain up to 10 active bookings concurrently.
     */
    // POLYMORPHISM
    @Override
    public int getMaxActiveBookings() {
        return 10;
    }

    /**
     * // POLYMORPHISM:
     * Premium customers receive a $3.00 flat discount on delivery charges.
     */
    // POLYMORPHISM
    @Override
    public double getDeliveryFeeDiscount() {
        return 3.00;
    }
}
