# Customer Component (`com.crumb.customer`)

## Overview
The `customer` module manages user identity, account tiers (Regular and Premium), secure authentication using BCrypt password hashing, session management, personal profile maintenance, and administrative customer moderation.

## Classes & Structure
- **Entity**:
  - `Customer.java`: Abstract base entity defining core attributes (name, email, phone, role) and polymorphic methods.
  - `RegularCustomer.java`: Concrete retail customer with standard loyalty points.
  - `PremiumCustomer.java`: Concrete VIP customer with order discounts, waived delivery fees, and concierge notes.
- **Repository**:
  - `CustomerRepository.java`: JPA repository handling queries by email, search filtering, and role counts.
- **Service**:
  - `CustomerService.java`: Business contract defining registration, authentication, tier upgrade, and administrative operations.
  - `CustomerServiceImpl.java`: Transactional implementation with BCrypt hashing and tier polymorphism.
- **Controller**:
  - `CustomerAuthController.java`: Endpoints for login, registration, logout, and active session checking.
  - `CustomerController.java`: Endpoints for customer profile view/edit, tier upgrades, and admin customer CRUD.
- **DTOs**:
  - `RegisterRequest.java`: Input validation for registration.
  - `LoginRequest.java`: Credentials validation for sign-in.
  - `CustomerUpdateRequest.java`: Form object for profile modifications.
  - `CustomerResponse.java`: Safe output projection masking passwords and exposing tier properties.

## REST Endpoints
| HTTP Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new Regular/Premium account | Public |
| `POST` | `/api/auth/login` | Authenticate customer & start session | Public |
| `POST` | `/api/auth/logout` | End session | Logged in |
| `GET` | `/api/auth/me` | Check active session | Public |
| `GET` | `/api/customers/{id}` | View customer profile | Owner / Admin |
| `PUT` | `/api/customers/{id}` | Update customer profile | Owner / Admin |
| `POST` | `/api/customers/{id}/upgrade` | Upgrade to VIP Gold Membership | Owner / Admin |
| `GET` | `/api/customers` | Search & list all customers | Admin only |
| `PUT` | `/api/customers/{id}/status` | Activate or suspend account | Admin only |
| `DELETE` | `/api/customers/{id}` | Delete customer record | Admin only |

## OOP Concepts Used
1. **Encapsulation**: Private attributes in `Customer`, `RegularCustomer`, and `PremiumCustomer` accessed via Lombok-generated getters and setters. Sensitive fields like hashed passwords are hidden from output DTOs.
2. **Inheritance**: `RegularCustomer` and `PremiumCustomer` inherit from the abstract `Customer` class using JPA `InheritanceType.SINGLE_TABLE` and discriminator `customer_type`.
3. **Polymorphism**: `calculateDiscount(double subtotal)`, `getCustomerBadge()`, `getMaxActiveBookings()`, and `getDeliveryFeeDiscount()` are declared abstract in `Customer` and overridden specifically in `RegularCustomer` (0% discount, standard limits) and `PremiumCustomer` (10% discount, $3 delivery waiver, 10 active bookings).
4. **Abstraction**: The `Customer` abstract class defines the blueprint for accounts, while `CustomerService` exposes high-level business capabilities while hiding JPA implementation details.
