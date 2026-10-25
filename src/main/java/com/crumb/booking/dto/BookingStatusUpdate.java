package com.crumb.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BookingStatusUpdate - Payload for advancing booking status (CONFIRMED, COMPLETED, CANCELLED).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusUpdate {

    // ENCAPSULATION: private fields with getters/setters
    @NotBlank(message = "Status cannot be empty")
    private String status;
}
