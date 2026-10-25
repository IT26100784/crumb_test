package com.crumb.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CustomerUpdateRequest - Form data for modifying customer profile.
 * 
 * WHY: Allows users to update their name, phone, address, or optional new password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {

    // ENCAPSULATION: private fields with getters/setters
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String address;

    private String newPassword;
}
