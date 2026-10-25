package com.crumb.customcake.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CustomCakeStatusUpdate - Admin action DTO for changing request status and revising quotation.
 * 
 * WHY: Enables administrators to advance cake status (APPROVED, BAKING, READY) and revise prices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomCakeStatusUpdate {

    // ENCAPSULATION: private fields with getters/setters
    @NotBlank(message = "Status is required")
    private String status;

    private Double revisedPrice;
}
