# Order Component (`com.crumb.order`)

## Overview
The `order` module manages shopping cart checkout, order persistence, inventory reservation, polymorphic fulfillment calculations (Delivery vs Pickup), order status lifecycle workflows, and revenue reporting.

## Classes & Structure
- **Entity**:
  - `Order.java`: Abstract base entity defining order number, customer link, line items, subtotals, discounts, delivery fees, and status.
  - `OrderItem.java`: Line item entity mapping products, purchased quantities, and historical unit prices.
  - `DeliveryOrder.java`: Courier delivery entity calculating distance-based delivery fees minus VIP member fee waivers.
  - `PickupOrder.java`: In-store counter pickup entity providing counter time slots with zero delivery fees.
- **Repository**:
  - `OrderRepository.java`: JPA queries for customer history, tracking order numbers, status filtering, and total revenue aggregation.
- **Service**:
  - `OrderService.java`: Business contract for cart checkout, inventory validation, order timeline, and status updates.
  - `OrderServiceImpl.java`: Transactional implementation handling inventory deductions, customer tier discounts, delivery calculations, and loyalty point awards.
- **Controller**:
  - `OrderController.java`: REST API for placing orders, customer history, live order tracking by code, and admin queue moderation.
- **DTOs**:
  - `OrderItemRequest.java`: Cart line item specification (product ID + quantity).
  - `OrderCreateRequest.java`: Checkout payload with fulfillment choices.
  - `OrderItemResponse.java`: Serialized line item with product image and price.
  - `OrderResponse.java`: Comprehensive order receipt with timeline and polymorphic delivery summary.
  - `OrderStatusUpdateRequest.java`: Admin payload for advancing fulfillment status.

## REST Endpoints
| HTTP Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/orders` | Checkout cart & place new order | Customer |
| `GET` | `/api/orders/my` | View personal order history | Customer |
| `GET` | `/api/orders/{id}` | View detailed order receipt | Owner / Admin |
| `GET` | `/api/orders/track/{orderNumber}` | Live order tracking by code | Public |
| `PUT` | `/api/orders/{id}/cancel` | Cancel order before baking | Owner / Admin |
| `GET` | `/api/orders` | View all orders (filter status/type) | Admin only |
| `PUT` | `/api/orders/{id}/status` | Advance fulfillment status | Admin only |
| `DELETE` | `/api/orders/{id}` | Delete order record | Admin only |
| `GET` | `/api/orders/stats/revenue` | Total revenue metric | Admin only |

## OOP Concepts Used
1. **Encapsulation**: Private fields across `Order`, `DeliveryOrder`, `PickupOrder`, and `OrderItem` accessed via Lombok accessors.
2. **Inheritance**: `DeliveryOrder` and `PickupOrder` subclass `Order` using JPA `InheritanceType.SINGLE_TABLE` and discriminator `order_type`.
3. **Polymorphism**:
   - `calculateDeliveryFee()`: Overridden so `DeliveryOrder` computes distance fees and checks VIP customer waivers, while `PickupOrder` returns $0.00.
   - `getDeliverySummary()`: Formats courier dispatch notes vs in-store counter instructions.
   - `getFulfillmentTimeEstimate()`: Dynamic delivery timing estimates (45-60 mins courier vs 20-30 mins counter pickup).
   - In addition, checkout invokes polymorphic methods on `BakeryProduct` (`calculateFinalPrice`) and `Customer` (`calculateDiscount`).
4. **Abstraction**: `Order` base class abstracts common transactional details; `OrderService` separates controller interactions from JPA repository queries.
