# 🥐 Crumb - Bakery Order & Bespoke Custom Cake Booking Platform

> **University Project:** SE1020 Object Oriented Programming  
> **Team:** 6 Students (Modular Component Ownership)  
> **Technology Stack:** Java 17+, Spring Boot 3, Spring Data JPA, Hibernate, MySQL, Lombok, Static Modern HTML5/CSS3/JavaScript  
> **Live Local URL:** [http://localhost:8080](http://localhost:8080)  
> **Default Admin Credentials:** `admin@crumb.com` / `Admin@123`

---

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Team Members & Component Allocation](#team-members--component-allocation)
3. [Object Oriented Programming (OOP) Demonstration](#object-oriented-programming-oop-demonstration)
4. [System Architecture & Package Structure](#system-architecture--package-structure)
5. [Database Configuration & Prerequisites](#database-configuration--prerequisites)
6. [How to Run the Application](#how-to-run-the-application)
7. [Comprehensive REST API Reference](#comprehensive-rest-api-reference)
8. [Storefront & Admin Feature Highlights](#storefront--admin-feature-highlights)
9. [Viva & Git Guidelines](#viva--git-guidelines)

---

## 🎂 Project Overview
**Crumb** is a full-stack, enterprise-grade Bakery Order and Bespoke Custom Cake Booking Platform designed for an artisan bakery. It combines high-volume retail bakery catalog sales (wild sourdoughs, flaky croissants, handcrafted cakes) with an interactive 2D celebration cake designer and delivery scheduler.

The application satisfies the university group requirement of **6 distinct modular components**, each featuring full **CRUD** (Create, Read, Update, Delete) capability on both customer-facing storefronts and an administrative management portal, backed by clear layered design and rigorous demonstration of **Encapsulation, Inheritance, Polymorphism, and Abstraction**.

---

## 👥 Team Members & Component Allocation

| Member | Component Package | Domain Entity & Subclasses | Responsibilities & Ownership |
|---|---|---|---|
| **Student 1** | `com.crumb.customer` | `Customer` (abstract)<br>↳ `RegularCustomer`<br>↳ `PremiumCustomer` | Customer authentication (BCrypt), session management, profile CRUD, VIP Gold loyalty tier polymorphism. |
| **Student 2** | `com.crumb.product` | `BakeryProduct` (abstract)<br>↳ `Cake`<br>↳ `Pastry`<br>↳ `Bread` | Product catalog CRUD, volume discount rules, stock management, allergen & nutritional tagging. |
| **Student 3** | `com.crumb.customcake` | `CustomCake` (abstract)<br>↳ `BirthdayCake`<br>↳ `WeddingCake`<br>↳ `CelebrationCake` | Interactive custom cake builder, live price preview engine, multi-tier pricing, advance lead-time validation. |
| **Student 4** | `com.crumb.order` | `Order` (abstract)<br>↳ `DeliveryOrder`<br>↳ `PickupOrder` | Shopping cart checkout, polymorphic delivery fees, 5-stage live status tracking timeline, cancellation. |
| **Student 5** | `com.crumb.booking` | `Booking` (abstract)<br>↳ `PickupBooking`<br>↳ `DeliveryBooking` | Oven & dispatch capacity management, date/time slot checking, scheduling appointments, calendar moderation. |
| **Student 6** | `com.crumb.payment` | `Payment` (abstract)<br>↳ `CashPayment`<br>↳ `CardPayment`<br>+ `Review` entity | Checkout payment processing, card validation, printable receipts, refunds, and 5-star customer reviews. |

---

## 🧠 Object Oriented Programming (OOP) Demonstration

The codebase is structured to clearly showcase the **Four Pillars of OOP** for viva evaluation:

### 1. Encapsulation (`// ENCAPSULATION`)
- All entity, DTO, and service fields are declared `private`.
- State access and mutations are mediated via Lombok `@Getter` and `@Setter` annotations, enforcing data protection and validation invariants (e.g., negative prices or stock quantities are rejected).
- Sensitive fields (such as BCrypt password hashes and raw credit card numbers) are never exposed through response DTOs.

### 2. Inheritance (`// INHERITANCE`)
- Implemented with JPA `InheritanceType.SINGLE_TABLE` and a `@DiscriminatorColumn`:
  * `Customer` ➔ `RegularCustomer`, `PremiumCustomer` (discriminator: `customer_type`)
  * `BakeryProduct` ➔ `Cake`, `Pastry`, `Bread` (discriminator: `product_type`)
  * `CustomCake` ➔ `BirthdayCake`, `WeddingCake`, `CelebrationCake` (discriminator: `cake_type`)
  * `Order` ➔ `DeliveryOrder`, `PickupOrder` (discriminator: `fulfillment_type`)
  * `Booking` ➔ `PickupBooking`, `DeliveryBooking` (discriminator: `booking_type`)
  * `Payment` ➔ `CashPayment`, `CardPayment` (discriminator: `payment_type`)

### 3. Polymorphism (`// POLYMORPHISM`)
- Abstract base methods are overridden across subclasses with custom business logic:
  * **Customer Pricing & Perks**: `Customer.calculateDiscount(subtotal)` gives `0.0` for `RegularCustomer` and `10%` for `PremiumCustomer`.
  * **Product Volume Pricing**: `BakeryProduct.calculateFinalPrice(qty)` applies tiered volume discounts: Cakes (5% on 2+), Pastries (10% on 6+, 15% on 12+), Breads (5% on 3+).
  * **Custom Cake Estimation**: `CustomCake.calculateEstimatedPrice()` factors base fees, tiers, and decor add-ons differently per celebration type.
  * **Order Delivery Surcharges**: `Order.getDeliveryFee()` calculates doorstep logistics charges, with waivers applied for VIP members.

### 4. Abstraction (`// ABSTRACTION`)
- Abstract classes (`Customer`, `BakeryProduct`, `CustomCake`, `Order`, `Booking`, `Payment`) define domain blueprints without exposing concrete implementation details.
- Every component defines a clean Service interface (e.g. `ProductService`) implemented by a transactional implementation class (`ProductServiceImpl`), decoupling business contracts from persistence details.

---

## 📁 System Architecture & Package Structure

```text
com.crumb
├── CrumbApplication.java                     # Spring Boot Main Entry Point
│
├── common/                                  # Shared Configuration & Infrastructure
│   ├── auth/SessionAuthHelper.java          # Session-based auth & RBAC helper
│   ├── config/AppConfig.java                # PasswordEncoder & Bean definitions
│   ├── config/DataSeeder.java               # CommandLineRunner seeding admin & sample menu
│   ├── dto/ApiResponse.java                 # Standardized JSON response envelope
│   ├── exception/GlobalExceptionHandler.java# Centralized @ControllerAdvice
│   └── exception/*.java                     # Custom domain exceptions (400, 401, 403, 404)
│
├── customer/                                # Component 1 (Member 1)
│   ├── controller/                          # CustomerAuthController & CustomerController
│   ├── dto/                                 # RegisterRequest, LoginRequest, CustomerResponse
│   ├── entity/                              # Customer, RegularCustomer, PremiumCustomer
│   ├── repository/CustomerRepository.java   # Spring Data JPA repository
│   └── service/                             # CustomerService & CustomerServiceImpl
│
├── product/                                 # Component 2 (Member 2)
│   ├── controller/ProductController.java    # Catalog & Admin CRUD REST endpoints
│   ├── dto/                                 # ProductRequest & ProductResponse
│   ├── entity/                              # BakeryProduct, Cake, Pastry, Bread
│   ├── repository/ProductRepository.java    # Search & filtering JPA repository
│   └── service/                             # ProductService & ProductServiceImpl
│
├── customcake/                              # Component 3 (Member 3)
│   ├── controller/CustomCakeController.java # Live preview & booking endpoints
│   ├── dto/                                 # CustomCakeRequest, CustomCakeResponse
│   ├── entity/                              # CustomCake, BirthdayCake, WeddingCake, CelebrationCake
│   ├── repository/CustomCakeRepository.java # JPA repository
│   └── service/                             # CustomCakeService & CustomCakeServiceImpl
│
├── order/                                   # Component 4 (Member 4)
│   ├── controller/OrderController.java      # Cart checkout, tracking, dispatch
│   ├── dto/                                 # OrderCreateRequest, OrderResponse, OrderItemResponse
│   ├── entity/                              # Order, DeliveryOrder, PickupOrder, OrderItem
│   ├── repository/OrderRepository.java      # JPA repository
│   └── service/                             # OrderService & OrderServiceImpl
│
├── booking/                                 # Component 5 (Member 5)
│   ├── controller/BookingController.java    # Capacity checking & schedule CRUD
│   ├── dto/                                 # BookingRequest, BookingResponse, SlotAvailabilityResponse
│   ├── entity/                              # Booking, PickupBooking, DeliveryBooking
│   ├── repository/BookingRepository.java    # Capacity check JPA repository
│   └── service/                             # BookingService & BookingServiceImpl
│
└── payment/                                 # Component 6 (Member 6)
    ├── controller/                          # PaymentController & ReviewController
    ├── dto/                                 # PaymentRequest, PaymentResponse, ReviewRequest, ReviewResponse
    ├── entity/                              # Payment, CashPayment, CardPayment, Review
    ├── repository/                          # PaymentRepository & ReviewRepository
    └── service/                             # PaymentService & ReviewService (+ Impls)
```

### Static Web Frontend (`src/main/resources/static/`)
```text
static/
├── index.html                               # Modern Storefront (Hero, Catalog, Builder, Tracking, Cart)
├── admin.html                               # Dedicated Administrator Operations Portal
├── css/
│   ├── variables.css                        # Design tokens (Cream, Mocha Chocolate, Pink, Caramel)
│   ├── main.css                             # Typography, modals, toasts, WhatsApp float, animations
│   ├── storefront.css                       # Catalog grid, 2D visualizer, order timeline, cart drawer
│   └── admin.css                            # Sidebar, KPI summary cards, data tables, status badges
└── js/
    ├── common.js                            # Central API client, toasts, dark/light mode, WhatsApp
    ├── customer.js                          # Auth modal, registration, login, profile, VIP upgrade
    ├── product.js                           # Catalog loader, search/filter, quick-view, shopping cart
    ├── customcake.js                        # Step-by-step cake builder, 2D stage visualizer, preview
    ├── order.js                             # Multi-step checkout, live tracking timeline, order history
    ├── booking.js                           # Real-time capacity check, time slots, scheduling
    ├── payment.js                           # Card/cash settlements, printable receipts, 5-star reviews
    └── admin.js                             # Full administrative CRUD, metrics, dispatch, refunds
```

---

## 🗄️ Database Configuration & Prerequisites

The application is configured to run against MySQL 8.0:
- **Database URL:** `jdbc:mysql://localhost:3306/crumb_app`
- **Username:** `root`
- **Password:** `crumb123`
- **DDL Mode:** `spring.jpa.hibernate.ddl-auto=update`

> **Note on Port Configuration:** `application.properties` uses `localhost:${DB_PORT:3306}/crumb_app`. If your operating system reserves port 3306, you can override it seamlessly by setting the environment variable `DB_PORT=3307` without modifying code.

### MySQL Database Initialization
Run the following commands in MySQL if creating manually:
```sql
CREATE DATABASE IF NOT EXISTS crumb_app;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'crumb123';
FLUSH PRIVILEGES;
```

---

## 🚀 How to Run the Application

### 1. Start MySQL Server
Ensure your MySQL service is running on `localhost:3306` (or `3307`).

### 2. Build and Launch Spring Boot
Open a terminal in the project root directory and execute:
```bash
# On Windows
.\mvnw spring-boot:run

# Or with Maven directly
mvn spring-boot:run
```

### 3. Open in Browser
- **Customer Storefront:** [http://localhost:8080](http://localhost:8080)
- **Administrator Portal:** [http://localhost:8080/admin.html](http://localhost:8080/admin.html)  
  *(Login with `admin@crumb.com` / `Admin@123`)*

---

## 🔌 Comprehensive REST API Reference

### 1. Customer & Auth (`/api/auth`, `/api/customers`)
- `POST /api/auth/register` - Create customer account (opt-in VIP Gold)
- `POST /api/auth/login` - Authenticate & establish session
- `POST /api/auth/logout` - Clear active session
- `GET /api/auth/me` - Fetch active session profile
- `GET /api/customers/{id}` - View customer profile
- `PUT /api/customers/{id}` - Update customer contact details
- `POST /api/customers/{id}/upgrade` - Upgrade customer to VIP Gold
- `GET /api/customers` *(Admin)* - List & search all customers
- `PUT /api/customers/{id}/status` *(Admin)* - Toggle account active/suspended
- `DELETE /api/customers/{id}` *(Admin)* - Delete customer account

### 2. Product Catalog (`/api/products`)
- `GET /api/products` - Browse catalog (with keyword & category filters)
- `GET /api/products/{id}` - Get product details
- `GET /api/products/{id}/price?quantity=N` - Live polymorphic volume discount calculation
- `POST /api/products` *(Admin)* - Create new bakery product (Cake, Pastry, Bread)
- `PUT /api/products/{id}` *(Admin)* - Update product details & inventory
- `DELETE /api/products/{id}` *(Admin)* - Remove product from catalog

### 3. Custom Cake Studio (`/api/custom-cakes`)
- `POST /api/custom-cakes/preview` - Live price preview calculation
- `POST /api/custom-cakes` - Submit bespoke cake booking request
- `GET /api/custom-cakes/my` - View logged-in customer's custom cake history
- `GET /api/custom-cakes/{id}` - View bespoke cake details
- `PUT /api/custom-cakes/{id}/cancel` - Cancel custom cake request
- `PUT /api/custom-cakes/{id}/status` *(Admin)* - Update production status

### 4. Orders & Checkout (`/api/orders`)
- `POST /api/orders` - Place order from cart items
- `GET /api/orders/my` - View customer order history
- `GET /api/orders/track/{orderNumber}` - Live tracking status & timeline
- `GET /api/orders/{id}` - View itemized order details
- `PUT /api/orders/{id}/cancel` - Cancel order if pending
- `GET /api/orders` *(Admin)* - List all customer orders
- `PUT /api/orders/{id}/status` *(Admin)* - Dispatch status update

### 5. Bookings & Scheduling (`/api/bookings`)
- `GET /api/bookings/availability?date=YYYY-MM-DD` - Check open kitchen slots
- `POST /api/bookings` - Schedule pickup or delivery appointment
- `GET /api/bookings/my` - View personal bookings
- `GET /api/bookings/{id}` - Get booking details
- `PUT /api/bookings/{id}/cancel` - Cancel booking appointment
- `GET /api/bookings` *(Admin)* - List all bakery bookings
- `PUT /api/bookings/{id}/status` *(Admin)* - Update booking status

### 6. Payments & Reviews (`/api/payments`, `/api/reviews`)
- `POST /api/payments` - Process Card or Cash checkout settlement
- `GET /api/payments/my` - View personal payment receipts
- `GET /api/payments/{id}` - View transaction invoice
- `GET /api/payments` *(Admin)* - View financial audit ledger
- `POST /api/payments/{id}/refund` *(Admin)* - Process customer refund
- `POST /api/reviews` - Submit 5-star customer feedback
- `GET /api/reviews/testimonials` - Public approved testimonials
- `GET /api/reviews/product/{productId}` - Product-specific customer ratings
- `GET /api/reviews` *(Admin)* - View all reviews for moderation
- `PUT /api/reviews/{id}/toggle` *(Admin)* - Toggle public visibility
- `DELETE /api/reviews/{id}` *(Admin)* - Delete review

---

## 🎨 Storefront & Admin Feature Highlights

1. **Warm Appetizing Design System**: Palette crafted with clotted cream (`#FDFBF7`), rich roast chocolate (`#2C1810`), blush strawberry glaze (`#F7D6D0`), and warm golden caramel (`#D48B38`).
2. **Dark / Light Mode Toggle**: Persisted in `localStorage` and respects system OS preferences.
3. **Floating WhatsApp Button**: Situated bottom-right with a subtle breathing pulse animation and direct pre-filled WhatsApp chat link (`wa.me`).
4. **Back-to-Top Button**: Smooth scrolling button that appears automatically after scrolling 300px.
5. **Tabbed Auth Modal**: Public modal for Sign In and Sign Up with live field validation.
6. **Live Interactive Custom Cake Builder**: Real-time 2D visualizer updating sponge layers, colors, and toppers as options change, linked to dynamic price calculations.
7. **5-Stage Order Tracking Timeline**: Stepper line displaying kitchen progress (`PENDING` ➔ `CONFIRMED` ➔ `BAKING` ➔ `READY` ➔ `DELIVERED`).
8. **Slide-Over Shopping Cart**: Live quantity adjustments, subtotal, and automatic VIP Gold member discount line.
9. **Dedicated Admin Portal**: Protected dashboard with KPI cards, revenue analytics, and independent management views for all 6 components.

---

## 📚 Viva & Git Guidelines
- **Viva Preparation Guide:** Read [VIVA_GUIDE.md](file:///C:/Users/kdsth/OneDrive/Desktop/Crumb%20Web%20App/VIVA_GUIDE.md) for plain-English explanations of classes, OOP patterns, and 60 likely viva questions with model answers.
- **Git Commit Workflow Guide:** Read [COMMIT_GUIDE.md](file:///C:/Users/kdsth/OneDrive/Desktop/Crumb%20Web%20App/COMMIT_GUIDE.md) for the step-by-step commit sequence per component.
- **Git Branch & Author Script:** Check `scripts/git-setup.md` for team branch conventions (`feature/customer`, `feature/order`, etc.) and author attribution.
