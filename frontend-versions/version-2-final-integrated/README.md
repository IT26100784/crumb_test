# Frontend Version 2: Complete Full-Stack Integrated UI
**Phase 3 — End of Project Commit**

### What is this?
This is the **fully integrated, production-ready frontend** of the Crumb Artisan Bakery platform connected dynamically to all Spring Boot REST API endpoints.

### Key Characteristics:
- **Full REST API Integration:** Dynamic JavaScript modules calling real backend controllers:
  - `customer.js`: User registration, session-based login, VIP tier upgrade, and profile modal.
  - `product.js`: Dynamic product catalog fetching (`/api/products`), search, category filters, quick-view modal, and live shopping cart drawer.
  - `customcake.js`: Photorealistic 3D tiered artisan cake visualizer with real-time price preview calculation.
  - `order.js`: Multi-step fulfillment checkout (delivery fee calculation LKR 450.00), order placement, and live order status tracking timeline.
  - `booking.js`: Reservation date/time availability slots.
  - `payment.js`: Card/Cash payment submission and customer review feedback ratings.
  - `admin.js`: Complete Admin Management Portal (`admin.html`) with KPI summary cards and interactive tables with custom dark glass status dropdowns.

### How to use in Git (Final Integration Commits):
Once all teammates have committed their backend Java packages, copy these files into `src/main/resources/static/` and commit under your own GitHub account:
```bash
git add src/main/resources/static/
git commit -m "ui: connect storefront components and modals to backend REST APIs"
git commit -m "ui: add comprehensive administrative dashboard with dark luxury glass UI"
```
