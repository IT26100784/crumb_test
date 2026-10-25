# Booking Component (`com.crumb.booking`)

## Overview
The `booking` module manages pickup and delivery appointment scheduling for bakery orders and custom cake consultations. It enforces slot capacities (preventing kitchen bottlenecks), validates customer tier active booking limits, and computes polymorphic reservation fees.

## Classes & Structure
- **Entity**:
  - `Booking.java`: Abstract base entity tracking booking reference, customer link, date, time slot, fee, and status.
  - `PickupBooking.java`: In-store counter collection entity with complimentary reservation and tasting table options.
  - `DeliveryBooking.java`: Scheduled courier reservation with distance handling and optional white-glove event table setup.
- **Repository**:
  - `BookingRepository.java`: Queries for member active quotas, daily slot capacity checks, and admin date calendars.
- **Service**:
  - `BookingService.java`: Business contract for slot availability, capacity checking, customer limits, and booking lifecycle.
  - `BookingServiceImpl.java`: Transactional implementation enforcing max active bookings via `Customer.getMaxActiveBookings()` and max 5 bookings per slot.
- **Controller**:
  - `BookingController.java`: REST API for live slot availability, booking checkout, tracking, and administrative calendar moderation.
- **DTOs**:
  - `BookingRequest.java`: Form object for reserving appointment slots.
  - `BookingResponse.java`: Presentation model with calculated polymorphic instructions and fees.
  - `BookingStatusUpdate.java`: Admin payload for advancing booking status.
  - `SlotAvailabilityResponse.java`: Slot capacity payload for frontend calendar pickers.

## REST Endpoints
| HTTP Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/bookings/availability?date=YYYY-MM-DD` | Check slot capacity & availability | Public |
| `POST` | `/api/bookings` | Schedule new booking | Customer |
| `GET` | `/api/bookings/my` | View my bookings | Customer |
| `GET` | `/api/bookings/{id}` | View detailed booking | Owner / Admin |
| `GET` | `/api/bookings/ref/{ref}` | Lookup booking by reference code | Public |
| `PUT` | `/api/bookings/{id}/cancel` | Cancel booking | Owner / Admin |
| `GET` | `/api/bookings` | Admin calendar list (filter status/type/date) | Admin only |
| `PUT` | `/api/bookings/{id}/status` | Update booking status | Admin only |
| `DELETE` | `/api/bookings/{id}` | Delete booking record | Admin only |

## OOP Concepts Used
1. **Encapsulation**: Private fields across `Booking`, `PickupBooking`, `DeliveryBooking`, and DTOs with Lombok accessors.
2. **Inheritance**: `PickupBooking` and `DeliveryBooking` subclass `Booking` using JPA `InheritanceType.SINGLE_TABLE` and discriminator `booking_type`.
3. **Polymorphism**:
   - `calculateBookingFee()`: `PickupBooking` returns $0.00 (or $10 for tasting tables), while `DeliveryBooking` computes $5 base fee + $20 white-glove setup minus VIP discounts.
   - `getBookingInstructions()`: Formats counter instructions vs courier delivery alerts.
   - `getBookingTypeName()`: Returns user-facing booking type string.
   - Cross-module polymorphism: Invokes `Customer.getMaxActiveBookings()` to dynamically enforce tier limits (Regular: max 3, Premium: max 10).
4. **Abstraction**: The abstract class `Booking` establishes reservation characteristics; `BookingService` isolates business operations from database repositories.
