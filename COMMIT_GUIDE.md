# 📜 Crumb Bakery Platform - Git Commit Guide

> **SE1020 Object Oriented Programming - Team Workflow Standard**  
> This guide outlines the incremental, component-driven Git commit sequence for all 6 team members.  
> Each member should have **8 to 15 small, logical, well-formed commits** representing their individual feature branch development.

---

## 🏷️ Commit Message Standard
All commit messages MUST follow the component prefix convention:
```text
<component>: <concise imperative description of change>

Examples:
customer: add abstract Customer base entity with single table inheritance
product: add Cake, Pastry, and Bread subclasses with volume pricing
customcake: implement interactive 2D cake visualizer and price preview
order: add checkout flow with polymorphic delivery fee calculation
booking: add time slot capacity verification and scheduling
payment: add card Luhn validation and review submission modal
ui: add dark mode theme toggle and floating WhatsApp button
docs: add comprehensive VIVA_GUIDE with 60 questions and answers
```

---

## 🌳 Branching Model
Each student works on their dedicated feature branch branched off `main` or `master`:
- Student 1: `feature/customer`
- Student 2: `feature/product`
- Student 3: `feature/customcake`
- Student 4: `feature/order`
- Student 5: `feature/booking`
- Student 6: `feature/payment`
- UI / Integration: `feature/storefront-ui` and `feature/admin-portal`

---

## 📋 Component-by-Component Commit Sequence

### 1. Common & Infrastructure Commits (`common:`)
| Step | Task | Suggested Commit Message |
|---|---|---|
| 1 | Maven setup | `common: scaffold Spring Boot 3 project with Maven build and MySQL configuration` |
| 2 | Error handling | `common: add ApiResponse, custom domain exceptions, and GlobalExceptionHandler` |
| 3 | Session security | `common: add SessionAuthHelper for role-based access control` |
| 4 | Startup seeder | `common: add DataSeeder runner for admin, products, and review startup initialization` |
| 5 | Component docs | `common: add component documentation README.md` |

---

### 2. Member 1: Customer Component (`customer:`)
| Step | Task | Suggested Commit Message |
|---|---|---|
| 1 | Base entity | `customer: add abstract Customer entity with JPA single table inheritance` |
| 2 | Concrete tiers | `customer: add RegularCustomer and PremiumCustomer entity subclasses` |
| 3 | JPA repository | `customer: add CustomerRepository with email and search finders` |
| 4 | DTOs & Validation | `customer: add DTOs for registration, login, and customer projections` |
| 5 | Service interface | `customer: add CustomerService interface and transactional implementation` |
| 6 | BCrypt security | `customer: implement BCrypt password hashing and credential verification` |
| 7 | Polymorphic tiers | `customer: implement dynamic VIP Gold upgrade with order discount perks` |
| 8 | Auth controller | `customer: add CustomerAuthController for registration, login, and session check` |
| 9 | Profile controller | `customer: add CustomerController for profile CRUD and admin customer moderation` |
| 10 | Frontend auth | `customer: add tabbed Sign In and Sign Up modal with input validation` |
| 11 | Profile UI | `customer: add customer profile view, update form, and VIP badge display` |
| 12 | Component docs | `customer: add component documentation README.md` |

---

### 3. Member 2: Bakery Product Component (`product:`)
| Step | Task | Suggested Commit Message |
|---|---|---|
| 1 | Base entity | `product: add BakeryProduct abstract entity with JPA single table inheritance` |
| 2 | Subclasses | `product: add Cake, Pastry, and Bread entity subclasses with polymorphic pricing` |
| 3 | JPA repository | `product: add ProductRepository with search and filter queries` |
| 4 | DTOs | `product: add ProductRequest and ProductResponse DTOs with validation` |
| 5 | Service contract | `product: add ProductService interface and implementation with realistic bakery seeder` |
| 6 | Volume discounts | `product: implement polymorphic calculateFinalPrice rules for bulk bakes` |
| 7 | Controller | `product: add ProductController with catalog browsing and admin CRUD` |
| 8 | Catalog UI | `product: add responsive product cards grid with allergen badges and ratings` |
| 9 | Live search | `product: add live search bar with debounce and category filter chips` |
| 10 | Quick view | `product: add quick-view modal with volume discount live calculation` |
| 11 | Shopping cart | `product: add slide-over shopping cart drawer with quantity adjustments` |
| 12 | Component docs | `product: add component documentation README.md` |

---

### 4. Member 3: Custom Cake Studio Component (`customcake:`)
| Step | Task | Suggested Commit Message |
|---|---|---|
| 1 | Base entity | `customcake: add CustomCake abstract entity with JPA single table inheritance` |
| 2 | Cake subclasses | `customcake: add BirthdayCake, WeddingCake, and CelebrationCake subclasses` |
| 3 | JPA repository | `customcake: add CustomCakeRepository with customer and status queries` |
| 4 | DTOs & Validation | `customcake: add CustomCakeRequest, CustomCakeResponse, and status update DTOs` |
| 5 | Price estimation | `customcake: implement polymorphic price calculation engine for bespoke tiers` |
| 6 | Service layer | `customcake: add CustomCakeService interface and implementation with lead-time validation` |
| 7 | Controller | `customcake: add CustomCakeController for live builder preview, bookings, and admin moderation` |
| 8 | 2D visualizer | `customcake: add interactive 2D cake visualizer with dynamic layers and frosting colors` |
| 9 | Builder form | `customcake: add step-by-step cake builder with flavor chips and accent toggles` |
| 10 | Live preview UI | `customcake: add real-time price summary card and lead-time alert` |
| 11 | Confirmation | `customcake: add bespoke cake booking success modal with reference code` |
| 12 | Component docs | `customcake: add component documentation README.md` |

---

### 5. Member 4: Retail Order & Cart Component (`order:`)
| Step | Task | Suggested Commit Message |
|---|---|---|
| 1 | Base entities | `order: add OrderItem and abstract Order entity with JPA single table inheritance` |
| 2 | Subclasses | `order: add DeliveryOrder and PickupOrder entity subclasses with polymorphic fees` |
| 3 | JPA repository | `order: add OrderRepository with customer history and revenue queries` |
| 4 | DTOs | `order: add OrderItemRequest, OrderCreateRequest, and OrderResponse DTOs` |
| 5 | Service layer | `order: add OrderService interface and transactional implementation` |
| 6 | Inventory check | `order: implement stock deduction on checkout and inventory restoration on cancellation` |
| 7 | Controller | `order: add OrderController with checkout, tracking, and admin dispatch endpoints` |
| 8 | Multi-step checkout | `order: add multi-step checkout modal with delivery method and date selection` |
| 9 | Tracking radar | `order: add 5-stage visual order tracking timeline with active step pulse` |
| 10 | Order receipt | `order: add printable order confirmation invoice modal` |
| 11 | Admin dispatch | `order: add admin order status dropdown updater with real-time refresh` |
| 12 | Component docs | `order: add component documentation README.md` |

---

### 6. Member 5: Booking & Scheduling Component (`booking:`)
| Step | Task | Suggested Commit Message |
|---|---|---|
| 1 | Base entity | `booking: add abstract Booking entity with JPA single table inheritance` |
| 2 | Subclasses | `booking: add PickupBooking and DeliveryBooking entity subclasses` |
| 3 | JPA repository | `booking: add BookingRepository with capacity check and active limits` |
| 4 | DTOs | `booking: add BookingRequest, BookingResponse, and SlotAvailabilityResponse DTOs` |
| 5 | Service layer | `booking: add BookingService interface and implementation enforcing tier quotas` |
| 6 | Capacity engine | `booking: implement daily capacity limit check disabling overloaded time slots` |
| 7 | Controller | `booking: add BookingController with availability checking and admin calendar moderation` |
| 8 | Date picker UI | `booking: integrate date and time slot pickers with live availability query` |
| 9 | Booking history | `booking: add customer booking history panel with cancel appointment action` |
| 10 | Admin schedule | `booking: add admin bookings schedule table with status updates` |
| 11 | Component docs | `booking: add component documentation README.md` |

---

### 7. Member 6: Payment & Review Component (`payment:`)
| Step | Task | Suggested Commit Message |
|---|---|---|
| 1 | Base entity | `payment: add Payment abstract entity with JPA single table inheritance` |
| 2 | Subclasses | `payment: add CashPayment and CardPayment entity subclasses` |
| 3 | Review entity | `payment: add Review entity for customer ratings and testimonials` |
| 4 | Repositories | `payment: add PaymentRepository and ReviewRepository` |
| 5 | DTOs | `payment: add PaymentRequest, PaymentResponse, ReviewRequest, and ReviewResponse DTOs` |
| 6 | Payment service | `payment: add PaymentService interface and implementation with refund logic` |
| 7 | Review service | `payment: add ReviewService interface and implementation with moderation` |
| 8 | Controllers | `payment: add PaymentController and ReviewController with refund and moderation endpoints` |
| 9 | Card form UI | `payment: add credit card details form with masked storage and Luhn validation` |
| 10 | Review modal | `payment: add 5-star interactive rating selection and review submission form` |
| 11 | Testimonials | `payment: add verified customer testimonials showcase on homepage` |
| 12 | Admin ledger | `payment: add admin financial audit ledger and customer refund action` |
| 13 | Component docs | `payment: add component documentation README.md` |

---

### 8. UI, Polish & Documentation (`ui:`, `docs:`)
| Step | Task | Suggested Commit Message |
|---|---|---|
| 1 | Theme tokens | `ui: add warm bakery color palette variables and Dark/Light theme manager` |
| 2 | Core UI | `ui: add toast notification system, scroll reveal animations, and skeleton loaders` |
| 3 | Floating tools | `ui: add floating WhatsApp chat button with subtle pulse and back-to-top button` |
| 4 | Admin layout | `ui: add full admin portal dashboard with KPI summary cards and responsive sidebar` |
| 5 | Root docs | `docs: add comprehensive root README.md with architecture and API tables` |
| 6 | Viva guide | `docs: add VIVA_GUIDE.md with plain-English OOP explanations and 60 questions` |
| 7 | Commit guide | `docs: add COMMIT_GUIDE.md and git-setup scripts for realistic group history` |
