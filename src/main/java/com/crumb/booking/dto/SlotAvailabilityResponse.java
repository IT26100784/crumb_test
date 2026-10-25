package com.crumb.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * SlotAvailabilityResponse - Calendar scheduling availability payload.
 * 
 * WHY: Enables date pickers to disable dates where capacity is full and show available hours.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotAvailabilityResponse {

    // ENCAPSULATION: private fields with getters/setters
    private LocalDate date;
    private boolean fullyBooked;
    private List<SlotInfo> slots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotInfo {
        private String timeSlot;
        private int remainingCapacity;
        private boolean available;
    }
}
