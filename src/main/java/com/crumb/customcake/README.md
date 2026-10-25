# Custom Cake Component (`com.crumb.customcake`)

## Overview
The `customcake` module manages bespoke artisan cake commissions. It powers an interactive online cake builder allowing patrons to customize dimensions, flavor pairings, fillings, frostings, and decorative additions for specific life events (Birthdays, Weddings, Milestone Celebrations) with dynamic polymorphic price estimation and kitchen preparation lead-time enforcement.

## Classes & Structure
- **Entity**:
  - `CustomCake.java`: Abstract base entity tracking customer references, flavor, size, inscriptions, dietary notes, event dates, and status.
  - `BirthdayCake.java`: Specialized birthday centerpiece with candle sets, age toppers, and 2-day lead notice.
  - `WeddingCake.java`: Multi-tiered wedding architecture with food-grade doweling, floral cascades, tasting kits, and 7-day advance notice.
  - `CelebrationCake.java`: Milestone cakes for graduations/anniversaries with 24k edible gold leaf, custom plaques, and 3-day notice.
- **Repository**:
  - `CustomCakeRepository.java`: Queries for customer order history, status filtering, and kitchen queue monitoring.
- **Service**:
  - `CustomCakeService.java`: Business contract for live price calculation, request lifecycle, and approval workflows.
  - `CustomCakeServiceImpl.java`: Implementation enforcing kitchen lead times, applying polymorphic pricing, and checking VIP discounts.
- **Controller**:
  - `CustomCakeController.java`: REST endpoints for live builder price preview, order submission, tracking, and admin moderation.
- **DTOs**:
  - `CustomCakeRequest.java`: Form object capturing builder options.
  - `CustomCakeResponse.java`: Presentation model exposing calculated polymorphic event summaries and lead days.
  - `CustomCakeStatusUpdate.java`: Admin payload for updating kitchen status and revising price quotes.

## REST Endpoints
| HTTP Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/custom-cakes/preview` | Live price calculation for builder | Public |
| `POST` | `/api/custom-cakes` | Submit bespoke cake request | Customer |
| `GET` | `/api/custom-cakes/my` | View my custom cake requests | Customer |
| `GET` | `/api/custom-cakes/{id}` | View request details | Owner / Admin |
| `PUT` | `/api/custom-cakes/{id}/cancel` | Cancel pending request | Owner / Admin |
| `GET` | `/api/custom-cakes` | View all requests (filter by status) | Admin only |
| `PUT` | `/api/custom-cakes/{id}/status` | Update kitchen status & revise price | Admin only |
| `DELETE` | `/api/custom-cakes/{id}` | Delete custom cake record | Admin only |

## OOP Concepts Used
1. **Encapsulation**: Private fields for all cake dimensions, prices, and status variables with Lombok accessors.
2. **Inheritance**: `BirthdayCake`, `WeddingCake`, and `CelebrationCake` subclass `CustomCake` with JPA `SINGLE_TABLE` inheritance and discriminator `cake_type`.
3. **Polymorphism**:
   - `calculateEstimatedPrice()`: Subclasses apply specialized pricing formulas (tiers, fresh florals, gold leaf, doweling).
   - `getEventSummary()`: Returns customized event descriptions for order summaries and invoices.
   - `getPreparationLeadDays()`: Enforces minimum advance notice based on complexity (2 days for birthdays, 7 days for tiered weddings).
   - `getSpecialtyAddons()`: Returns itemized included decorations.
4. **Abstraction**: Base class `CustomCake` abstracts design concepts; `CustomCakeService` interface abstracts database interactions from REST controllers.
