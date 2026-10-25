package com.crumb.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Customer - Abstract Base Entity for all customer accounts in the Crumb platform.
 * 
 * // ABSTRACTION:
 * This is an abstract class defining common attributes (name, email, password, role)
 * and abstract business methods that specialized subclasses (RegularCustomer, PremiumCustomer)
 * must implement with their own customized logic.
 * 
 * // INHERITANCE:
 * Uses JPA SINGLE_TABLE inheritance with discriminator column "customer_type".
 * All customer variants share the "customers" table while storing specialized behavior.
 */
// ABSTRACTION
// INHERITANCE
@Entity
@Table(name = "customers")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "customer_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Customer {

    // // ENCAPSULATION:
    // All fields are declared private. Lombok generates standard getter and setter methods
    // to provide controlled access and mutation while protecting internal state.
    // ENCAPSULATION
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false)
    private String phone;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(length = 500)
    private String address;

    @Column(nullable = false)
    private String role = "CUSTOMER"; // CUSTOMER or ADMIN

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "customer_type", insertable = false, updatable = false)
    private String customerType;

    /**
     * // POLYMORPHISM:
     * Abstract method to calculate discount percentage or amount based on customer tier.
     * Regular customers receive 0% or base loyalty, while Premium customers receive 10% off.
     */
    // POLYMORPHISM
    public abstract double calculateDiscount(double subtotal);

    /**
     * // POLYMORPHISM:
     * Returns the promotional badge label displayed on the customer dashboard and profile.
     */
    // POLYMORPHISM
    public abstract String getCustomerBadge();

    /**
     * // POLYMORPHISM:
     * Returns the maximum active bookings allowed simultaneously for this membership level.
     */
    // POLYMORPHISM
    public abstract int getMaxActiveBookings();

    /**
     * // POLYMORPHISM:
     * Returns the delivery fee waiver or discount (in currency) applicable to this member.
     */
    // POLYMORPHISM
    public abstract double getDeliveryFeeDiscount();
}
