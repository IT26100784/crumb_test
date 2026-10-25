# Payment & Review Component (`com.crumb.payment`)

## Overview
The `payment` module manages financial settlements, invoice receipts, gateway integrations, transaction refunds, and customer reviews/ratings. It supports both physical cash settlement (with tendered validation and change calculation) and card gateway processing (with interchange fee calculation).

## Classes & Structure
- **Entity**:
  - `Payment.java`: Abstract base entity tracking transaction reference, customer, order/cake links, amount, fees, and status.
  - `CashPayment.java`: Cash settlement entity computing tendered notes, change due, and zero processing fees.
  - `CardPayment.java`: Card gateway entity simulating authorization tokens, cardholder records, and 2.5% interchange fees.
  - `Review.java`: Customer feedback entity with 1-5 star ratings, comments, and product links.
- **Repository**:
  - `PaymentRepository.java`: Queries for customer payment history, status filters, and gross collected volume.
  - `ReviewRepository.java`: Queries for product testimonials, average rating recalculation, and moderation queues.
- **Service**:
  - `PaymentService.java`: Business contract for payment processing, receipts, and refunds.
  - `PaymentServiceImpl.java`: Transactional implementation linking payments to orders and custom cakes.
  - `ReviewService.java`: Business contract for feedback submission, average rating updates, and moderation.
  - `ReviewServiceImpl.java`: Implementation with authentic bakery review seeder.
- **Controller**:
  - `PaymentController.java`: Endpoints for checking out payments, receipts, and refund management.
  - `ReviewController.java`: Endpoints for submitting star ratings, reading product testimonials, and moderation.
- **DTOs**:
  - `PaymentRequest.java`: Form payload for cash or card settlement.
  - `PaymentResponse.java`: Detailed payment receipt with polymorphic summary.
  - `ReviewRequest.java`: Rating and feedback submission payload.
  - `ReviewResponse.java`: Presentation model for public customer testimonials.

## REST Endpoints
| HTTP Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/payments` | Process order/cake payment | Customer |
| `GET` | `/api/payments/my` | View personal payment receipts | Customer |
| `GET` | `/api/payments/{id}` | View detailed payment receipt | Owner / Admin |
| `GET` | `/api/payments/ref/{reference}` | Lookup payment by ref code | Public |
| `POST` | `/api/payments/{id}/refund` | Refund transaction | Admin only |
| `GET` | `/api/payments` | View all payments | Admin only |
| `GET` | `/api/payments/stats/revenue` | Total collected payments | Admin only |
| `POST` | `/api/reviews` | Submit product or bakery review | Customer |
| `GET` | `/api/reviews/product/{productId}` | Product reviews | Public |
| `GET` | `/api/reviews/testimonials` | Public homepage testimonials | Public |
| `GET` | `/api/reviews/my` | View my reviews | Customer |
| `DELETE` | `/api/reviews/{id}` | Delete review | Owner / Admin |
| `GET` | `/api/reviews` | View all reviews | Admin only |
| `PUT` | `/api/reviews/{id}/toggle` | Approve or hide review | Admin only |

## OOP Concepts Used
1. **Encapsulation**: Private attributes for financial figures, card data, and review comments guarded by Lombok accessors.
2. **Inheritance**: `CashPayment` and `CardPayment` subclass `Payment` using JPA `InheritanceType.SINGLE_TABLE` and discriminator `payment_type`.
3. **Polymorphism**:
   - `processPayment(double amount)`: `CashPayment` calculates change due based on cash tendered; `CardPayment` executes digital authorization.
   - `generateReceipt()`: Formats tender-specific receipts (e.g. Cash Tendered vs Card Auth Token).
   - `getTransactionProcessingFee(double amount)`: Returns 0.0 for cash payments; calculates 2.5% fee for card payments.
   - `getPaymentMethodName()`: Returns human-friendly tender name.
4. **Abstraction**: Base class `Payment` abstracts monetary transaction models; `PaymentService` and `ReviewService` interfaces cleanly abstract persistence logic from HTTP controllers.
