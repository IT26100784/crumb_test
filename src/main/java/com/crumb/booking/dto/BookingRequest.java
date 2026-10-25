package com.crumb.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * BookingRequest - Form payload for scheduling a pickup or delivery booking.
 * 
 * WHY: Validates appointment date, slot, and fulfillment options.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    // ENCAPSULATION: private fields with Lombok accessors
    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date must be today or in the future")
    private LocalDate bookingDate;

    @NotBlank(message = "Time slot is required")
    private String timeSlot;

    @NotBlank(message = "Booking type (PICKUP or DELIVERY) is required")
    private String bookingType;

    private Long orderId;
    private Long customCakeId;
    private String notes;

    // Pickup options
    private String pickupBranch;
    @Builder.Default
    private Integer partySize = 1;
    @Builder.Default
    private Boolean tastingTableReserved = false;

    // Delivery options
    private String destinationAddress;
    private String recipientContactPhone;
    @Builder.Default
    private Boolean whiteGloveSetup = false;
}
