package com.crumb.booking.service;

import com.crumb.booking.dto.BookingRequest;
import com.crumb.booking.dto.BookingResponse;
import com.crumb.booking.dto.BookingStatusUpdate;
import com.crumb.booking.dto.SlotAvailabilityResponse;
import com.crumb.booking.entity.Booking;

import java.time.LocalDate;
import java.util.List;

/**
 * BookingService - Business contract for scheduling bakery reservations.
 * 
 * // ABSTRACTION:
 * Defines business specifications for capacity checking, customer tier limits,
 * booking creation, and administrative calendar management.
 */
// ABSTRACTION
public interface BookingService {

    /**
     * Creates a new scheduled booking, enforcing member active limits and slot capacity.
     */
    BookingResponse createBooking(Long customerId, BookingRequest request);

    /**
     * Checks available capacity across daily time slots.
     */
    SlotAvailabilityResponse checkAvailability(LocalDate date);

    /**
     * Retrieves all bookings submitted by a customer.
     */
    List<BookingResponse> getCustomerBookings(Long customerId);

    /**
     * Admin endpoint: Lists bookings with optional status, type, and date filters.
     */
    List<BookingResponse> getAllBookings(String status, String bookingType, LocalDate date);

    /**
     * Retrieves booking by ID.
     */
    BookingResponse getBookingById(Long id);

    /**
     * Retrieves booking by unique tracking reference code.
     */
    BookingResponse getBookingByReference(String ref);

    /**
     * Retrieves raw Booking entity by ID for cross-component operations.
     */
    Booking getBookingEntity(Long id);

    /**
     * Updates booking status (CONFIRMED, COMPLETED, CANCELLED).
     */
    BookingResponse updateBookingStatus(Long id, BookingStatusUpdate update);

    /**
     * Cancels an appointment.
     */
    BookingResponse cancelBooking(Long id, Long customerId, boolean isAdmin);

    /**
     * Deletes a booking record (Admin only).
     */
    void deleteBooking(Long id);
}
