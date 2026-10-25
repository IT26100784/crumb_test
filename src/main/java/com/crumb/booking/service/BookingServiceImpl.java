package com.crumb.booking.service;

import com.crumb.booking.dto.BookingRequest;
import com.crumb.booking.dto.BookingResponse;
import com.crumb.booking.dto.BookingStatusUpdate;
import com.crumb.booking.dto.SlotAvailabilityResponse;
import com.crumb.booking.entity.Booking;
import com.crumb.booking.entity.DeliveryBooking;
import com.crumb.booking.entity.PickupBooking;
import com.crumb.booking.repository.BookingRepository;
import com.crumb.common.exception.BadRequestException;
import com.crumb.common.exception.ForbiddenException;
import com.crumb.common.exception.ResourceNotFoundException;
import com.crumb.customcake.repository.CustomCakeRepository;
import com.crumb.customer.entity.Customer;
import com.crumb.customer.service.CustomerService;
import com.crumb.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * BookingServiceImpl - Business logic implementation for BookingService.
 * 
 * WHY: Enforces slot capacities, customer tier booking quotas,
 * calculates polymorphic reservation fees, and handles status transitions.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final int MAX_SLOT_CAPACITY = 5;
    private static final List<String> STANDARD_SLOTS = List.of(
            "09:00 - 11:00 AM",
            "11:00 AM - 01:00 PM",
            "01:00 - 03:00 PM",
            "03:00 - 05:00 PM",
            "05:00 - 07:00 PM"
    );

    private final BookingRepository bookingRepository;
    private final CustomerService customerService;
    private final OrderRepository orderRepository;
    private final CustomCakeRepository customCakeRepository;

    /**
     * Schedules a new booking with tier quota and capacity validation.
     */
    @Override
    public BookingResponse createBooking(Long customerId, BookingRequest request) {
        Customer customer = customerService.getCustomerEntity(customerId);

        // 1. POLYMORPHISM: Check customer's max active bookings based on membership tier
        long activeCount = bookingRepository.countByCustomerIdAndStatusNotIn(customerId, List.of("COMPLETED", "CANCELLED"));
        int maxAllowed = customer.getMaxActiveBookings();
        if (activeCount >= maxAllowed) {
            throw new BadRequestException("You have reached the maximum allowed limit of " + maxAllowed +
                    " active bookings for your " + customer.getCustomerBadge() + " tier. Please complete or cancel an existing reservation.");
        }

        // 2. Check time slot capacity
        long slotBookedCount = bookingRepository.countByBookingDateAndTimeSlotAndStatusNot(
                request.getBookingDate(), request.getTimeSlot(), "CANCELLED"
        );
        if (slotBookedCount >= MAX_SLOT_CAPACITY) {
            throw new BadRequestException("The time slot '" + request.getTimeSlot() + "' on " +
                    request.getBookingDate() + " is fully booked (capacity " + MAX_SLOT_CAPACITY + "). Please choose another slot.");
        }

        // 3. Instantiate concrete subclass
        Booking booking;
        String type = request.getBookingType() != null ? request.getBookingType().toUpperCase() : "PICKUP";

        if ("DELIVERY".equalsIgnoreCase(type)) {
            DeliveryBooking delivery = new DeliveryBooking();
            delivery.setDestinationAddress(request.getDestinationAddress() != null ? request.getDestinationAddress().trim() : customer.getAddress());
            delivery.setRecipientContactPhone(request.getRecipientContactPhone() != null ? request.getRecipientContactPhone().trim() : customer.getPhone());
            delivery.setWhiteGloveSetup(Boolean.TRUE.equals(request.getWhiteGloveSetup()));
            booking = delivery;
        } else {
            PickupBooking pickup = new PickupBooking();
            pickup.setPickupBranch(request.getPickupBranch() != null ? request.getPickupBranch().trim() : "Crumb Flagship Bakery, Colombo 03");
            pickup.setPartySize(request.getPartySize() != null ? request.getPartySize() : 1);
            pickup.setTastingTableReserved(Boolean.TRUE.equals(request.getTastingTableReserved()));
            booking = pickup;
        }

        // Generate unique booking code
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        booking.setBookingReference("CRUMB-BKG-" + datePart + "-" + rand);
        booking.setCustomer(customer);
        booking.setBookingDate(request.getBookingDate());
        booking.setTimeSlot(request.getTimeSlot());
        booking.setNotes(request.getNotes());
        booking.setStatus("CONFIRMED");

        // Optional links
        if (request.getOrderId() != null) {
            orderRepository.findById(request.getOrderId()).ifPresent(booking::setOrder);
        }
        if (request.getCustomCakeId() != null) {
            customCakeRepository.findById(request.getCustomCakeId()).ifPresent(booking::setCustomCake);
        }

        // POLYMORPHISM: compute booking fee
        double fee = booking.calculateBookingFee();
        booking.setBookingFee(fee);

        Booking saved = bookingRepository.save(booking);
        return BookingResponse.fromEntity(saved);
    }

    /**
     * Checks capacity across all standard slots for a given date.
     */
    @Override
    @Transactional(readOnly = true)
    public SlotAvailabilityResponse checkAvailability(LocalDate date) {
        List<SlotAvailabilityResponse.SlotInfo> slots = new ArrayList<>();
        boolean allFull = true;

        for (String slot : STANDARD_SLOTS) {
            long count = bookingRepository.countByBookingDateAndTimeSlotAndStatusNot(date, slot, "CANCELLED");
            int remaining = Math.max(0, MAX_SLOT_CAPACITY - (int) count);
            boolean isAvail = remaining > 0;
            if (isAvail) allFull = false;

            slots.add(new SlotAvailabilityResponse.SlotInfo(slot, remaining, isAvail));
        }

        return SlotAvailabilityResponse.builder()
                .date(date)
                .fullyBooked(allFull)
                .slots(slots)
                .build();
    }

    /**
     * Retrieves all bookings for a customer.
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getCustomerBookings(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Admin query: retrieve bookings filtered by status, type, and date.
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings(String status, String bookingType, LocalDate date) {
        List<Booking> list;
        if (date != null) {
            list = bookingRepository.findByBookingDateOrderByTimeSlotAsc(date);
        } else if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            list = bookingRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        } else {
            list = bookingRepository.findAllByOrderByCreatedAtDesc();
        }

        return list.stream()
                .filter(b -> {
                    if (bookingType == null || bookingType.trim().isEmpty() || "ALL".equalsIgnoreCase(bookingType)) {
                        return true;
                    }
                    if ("PICKUP".equalsIgnoreCase(bookingType)) return b instanceof PickupBooking;
                    if ("DELIVERY".equalsIgnoreCase(bookingType)) return b instanceof DeliveryBooking;
                    return true;
                })
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves booking by primary key.
     */
    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        return BookingResponse.fromEntity(getBookingEntity(id));
    }

    /**
     * Retrieves booking by reference code.
     */
    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String ref) {
        Booking booking = bookingRepository.findByBookingReference(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + ref));
        return BookingResponse.fromEntity(booking);
    }

    /**
     * Retrieves entity by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Booking getBookingEntity(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    /**
     * Updates booking status (CONFIRMED, COMPLETED, CANCELLED).
     */
    @Override
    public BookingResponse updateBookingStatus(Long id, BookingStatusUpdate update) {
        Booking booking = getBookingEntity(id);
        booking.setStatus(update.getStatus().trim().toUpperCase());
        Booking saved = bookingRepository.save(booking);
        return BookingResponse.fromEntity(saved);
    }

    /**
     * Cancels a booking.
     */
    @Override
    public BookingResponse cancelBooking(Long id, Long customerId, boolean isAdmin) {
        Booking booking = getBookingEntity(id);

        if (!isAdmin && !booking.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("You cannot cancel another customer's booking.");
        }

        if ("COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Completed appointments cannot be cancelled.");
        }

        booking.setStatus("CANCELLED");
        Booking saved = bookingRepository.save(booking);
        return BookingResponse.fromEntity(saved);
    }

    /**
     * Deletes a booking record (Admin only).
     */
    @Override
    public void deleteBooking(Long id) {
        Booking booking = getBookingEntity(id);
        bookingRepository.delete(booking);
    }
}
