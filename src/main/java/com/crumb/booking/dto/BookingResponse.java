package com.crumb.booking.dto;

import com.crumb.booking.entity.Booking;
import com.crumb.booking.entity.DeliveryBooking;
import com.crumb.booking.entity.PickupBooking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * BookingResponse - Presentation output DTO for scheduled bakery appointments.
 * 
 * WHY: Presents appointment details, linked order references, and polymorphic instructions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    // ENCAPSULATION: private fields with getters/setters
    private Long id;
    private String bookingReference;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Long orderId;
    private String orderNumber;
    private Long customCakeId;
    private LocalDate bookingDate;
    private String timeSlot;
    private String status;
    private double bookingFee;
    private String notes;
    private String bookingType;
    private LocalDateTime createdAt;

    // POLYMORPHIC calculated fields
    private String bookingTypeName;
    private String instructions;

    // Subclass fields
    private String pickupBranch;
    private Integer partySize;
    private Boolean tastingTableReserved;
    private String destinationAddress;
    private String recipientContactPhone;
    private Boolean whiteGloveSetup;

    /**
     * Converts a Booking entity to a BookingResponse DTO.
     * Demonstrates POLYMORPHISM by calling overridden methods on the Booking instance.
     */
    public static BookingResponse fromEntity(Booking booking) {
        if (booking == null) return null;

        BookingResponseBuilder builder = BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .customerId(booking.getCustomer() != null ? booking.getCustomer().getId() : null)
                .customerName(booking.getCustomer() != null ? booking.getCustomer().getFullName() : "N/A")
                .customerEmail(booking.getCustomer() != null ? booking.getCustomer().getEmail() : "N/A")
                .customerPhone(booking.getCustomer() != null ? booking.getCustomer().getPhone() : "N/A")
                .orderId(booking.getOrder() != null ? booking.getOrder().getId() : null)
                .orderNumber(booking.getOrder() != null ? booking.getOrder().getOrderNumber() : null)
                .customCakeId(booking.getCustomCake() != null ? booking.getCustomCake().getId() : null)
                .bookingDate(booking.getBookingDate())
                .timeSlot(booking.getTimeSlot())
                .status(booking.getStatus())
                .bookingFee(booking.getBookingFee())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                // POLYMORPHIC CALLS:
                .bookingTypeName(booking.getBookingTypeName())
                .instructions(booking.getBookingInstructions());

        if (booking instanceof PickupBooking p) {
            builder.bookingType("PICKUP")
                   .pickupBranch(p.getPickupBranch())
                   .partySize(p.getPartySize())
                   .tastingTableReserved(p.isTastingTableReserved());
        } else if (booking instanceof DeliveryBooking d) {
            builder.bookingType("DELIVERY")
                   .destinationAddress(d.getDestinationAddress())
                   .recipientContactPhone(d.getRecipientContactPhone())
                   .whiteGloveSetup(d.isWhiteGloveSetup());
        }

        return builder.build();
    }
}
