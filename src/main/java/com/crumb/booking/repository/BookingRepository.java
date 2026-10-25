package com.crumb.booking.repository;

import com.crumb.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * BookingRepository - Data access interface for Booking entities.
 * 
 * WHY: Provides query methods for customer booking histories, slot capacity checks,
 * active booking limits, and admin schedule views.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Finds bookings created by a specific customer.
     */
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Finds booking by unique reference code.
     */
    Optional<Booking> findByBookingReference(String bookingReference);

    /**
     * Counts active bookings for a customer to enforce tier limits (Regular: max 3, Premium: max 10).
     */
    long countByCustomerIdAndStatusNotIn(Long customerId, Collection<String> terminalStatuses);

    /**
     * Counts reservations on a specific date and time slot to prevent kitchen overbooking.
     */
    long countByBookingDateAndTimeSlotAndStatusNot(LocalDate bookingDate, String timeSlot, String excludedStatus);

    /**
     * Finds all bookings for a given date.
     */
    List<Booking> findByBookingDateOrderByTimeSlotAsc(LocalDate bookingDate);

    /**
     * Finds bookings filtered by status.
     */
    List<Booking> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Finds all bookings sorted newest first.
     */
    List<Booking> findAllByOrderByCreatedAtDesc();

    /**
     * Counts bookings by status for the admin dashboard.
     */
    long countByStatus(String status);
}
