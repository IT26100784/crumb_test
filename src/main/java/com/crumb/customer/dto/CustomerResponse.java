package com.crumb.customer.dto;

import com.crumb.customer.entity.Customer;
import com.crumb.customer.entity.PremiumCustomer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CustomerResponse - Clean customer projection DTO sent to clients.
 * 
 * WHY: Masks sensitive credentials like hashed passwords while exposing
 * calculated polymorphic fields (badge, max active bookings, discounts).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    // ENCAPSULATION: private fields with getters/setters
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String customerType;
    private String badge;
    private Integer loyaltyPoints;
    private int maxActiveBookings;
    private double deliveryDiscount;
    private LocalDate membershipExpiry;
    private boolean active;
    private LocalDateTime createdAt;

    /**
     * Converts a Customer entity (Regular or Premium) into a CustomerResponse DTO.
     * Demonstrates POLYMORPHISM by invoking polymorphic methods on the Customer instance.
     */
    public static CustomerResponse fromEntity(Customer customer) {
        if (customer == null) return null;

        LocalDate expiry = null;
        if (customer instanceof PremiumCustomer premium) {
            expiry = premium.getMembershipExpiry();
        }

        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .role(customer.getRole())
                .customerType(customer instanceof PremiumCustomer ? "PREMIUM" : "REGULAR")
                // POLYMORPHIC INVOCATIONS:
                .badge(customer.getCustomerBadge())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .maxActiveBookings(customer.getMaxActiveBookings())
                .deliveryDiscount(customer.getDeliveryFeeDiscount())
                .membershipExpiry(expiry)
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
