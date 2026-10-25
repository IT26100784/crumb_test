package com.crumb.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderStatusUpdateRequest - DTO for advancing order workflow status.
 * 
 * WHY: Validates order status transitions (CONFIRMED -> BAKING -> READY -> DELIVERED/COMPLETED).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    // ENCAPSULATION: private fields with getters/setters
    @NotBlank(message = "Status cannot be empty")
    private String status;
}
