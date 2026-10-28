# Frontend Version 1: Initial UI Prototype & Mockup
**Phase 1 — Start of Project Commit**

### What is this?
This is the **initial standalone UI prototype** of the Crumb Artisan Bakery platform before any backend Java controllers or database tables existed.

### Key Characteristics:
- **Zero Backend Dependencies:** Can be opened directly in any browser (`file:///...` or live preview) without starting Spring Boot or MySQL.
- **Visual Mockups:** Contains hardcoded sample product cards (Cakes, Pastries, Hearth Sourdough), customer reviews, and static cake customization UI with Sri Lankan localization (LKR currency, Colombo locations, +94 contact info).
- **Interactive UI Elements:** Features the dark/light mode toggle, floating WhatsApp button, back-to-top scroll, and custom dark glass date & select pickers.
- **Scripts:** Only loads `/js/common.js` (no API fetch calls).

### How to use in Git (Initial Project Commit):
Copy these files to `src/main/resources/static/` and commit at the **very start** of your project history:
```bash
git add src/main/resources/static/
git commit -m "ui: initial static UI mockup and storefront design prototype"
```
