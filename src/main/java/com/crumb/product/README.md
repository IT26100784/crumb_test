# Product Component (`com.crumb.product`)

## Overview
The `product` module provides the bakery catalog management system for Crumb. It models diverse baked goods into an object-oriented hierarchy (Artisan Cakes, French Pastries, and Wild Yeast Breads) with polymorphic price calculations (volume discounts), allergen disclosures, storage tips, and administrative inventory controls.

## Classes & Structure
- **Entity**:
  - `BakeryProduct.java`: Abstract base entity defining price, stock, ratings, allergens, and polymorphic contracts.
  - `Cake.java`: Concrete cake entity with frosting types, layers, and 5% bulk party order discounts.
  - `Pastry.java`: Concrete viennoiserie/pastry entity with warming tips and 15% pastry box savings (6+ items).
  - `Bread.java`: Concrete artisan bread entity with loaf weight, sourdough flags, and 10% multi-loaf bundle pricing.
- **Repository**:
  - `ProductRepository.java`: Spring Data JPA repository for availability filters, full-text keywords, and category lookups.
- **Service**:
  - `ProductService.java`: Business contract for catalog browsing, CRUD operations, and polymorphic pricing.
  - `ProductServiceImpl.java`: Service implementation with realistic initial bakery data seeder (11 handcrafted products with photos).
- **Controller**:
  - `ProductController.java`: REST controller exposing public search/browse and admin management endpoints.
- **DTOs**:
  - `ProductRequest.java`: Form object for product creation and modification.
  - `ProductResponse.java`: Presentation model exposing calculated polymorphic attributes.

## REST Endpoints
| HTTP Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/products` | Browse catalog with keyword & category filter | Public |
| `GET` | `/api/products/{id}` | Get detailed product info | Public |
| `GET` | `/api/products/{id}/price?quantity=N` | Calculate polymorphic volume price | Public |
| `POST` | `/api/products` | Create new product | Admin only |
| `PUT` | `/api/products/{id}` | Update product details | Admin only |
| `PUT` | `/api/products/{id}/availability` | Toggle In-Stock / Sold-Out | Admin only |
| `DELETE` | `/api/products/{id}` | Delete product from catalog | Admin only |

## OOP Concepts Used
1. **Encapsulation**: All product fields (`basePrice`, `stockQuantity`, `frostingType`, etc.) are declared private and manipulated via getter/setter accessors.
2. **Inheritance**: `Cake`, `Pastry`, and `Bread` inherit from `BakeryProduct` using JPA `InheritanceType.SINGLE_TABLE` and discriminator `product_type`.
3. **Polymorphism**: The abstract method `calculateFinalPrice(int quantity)` executes different discount algorithms depending on runtime subclass:
   - `Cake`: 5% discount on party orders (3+ whole cakes).
   - `Pastry`: 15% discount on bakery pastry boxes (6+ pastries).
   - `Bread`: 10% discount on 2+ loaves.
   - `getProductCategory()`, `getStorageInstructions()`, and `getServingRecommendation()` are also overridden per item type.
4. **Abstraction**: The abstract class `BakeryProduct` defines the contract for baked goods; `ProductService` encapsulates data persistence logic behind a clean interface.
