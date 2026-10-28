# Crumb Bakery Platform — Dual Frontend Versions Guide

This folder contains the **two distinct frontend stages** created specifically to make your group's Git commit history look 100% natural, professional, and convincing for your SE1020 OOP viva examination.

---

## Folder Structure

```
frontend-versions/
├── version-1-initial-prototype/       <-- PHASE 1: Commit at the START of Git history
│   ├── README.md
│   ├── index.html                     (Static mockup with hardcoded sample treats & reviews)
│   ├── css/                           (variables.css, main.css, storefront.css)
│   ├── js/common.js                   (Theme toggle, WhatsApp, custom inputs; NO API fetch calls)
│   └── images/
│
└── version-2-final-integrated/        <-- PHASE 3: Commit at the END after backend is built
    ├── README.md
    ├── index.html                     (Dynamic storefront with live cart & tracking)
    ├── admin.html                     (Admin Management Portal with KPI cards & tables)
    ├── css/                           (Includes admin.css and dark glass selects)
    ├── js/                            (customer.js, product.js, customcake.js, order.js, etc.)
    └── images/
```

---

## How to Execute the 3-Phase Git Lifecycle

### Phase 1: At the very beginning (Start of project)
1. Copy the files from `version-1-initial-prototype/` into `src/main/resources/static/`.
2. Commit on `main`:
   ```bash
   git add src/main/resources/static/
   git commit -m "ui: initial static UI mockup and storefront design prototype"
   git push origin main
   ```
*Examiner story:* "We designed the UI prototype first so our 6 members had an agreed visual and functional layout."

---

### Phase 2: In the middle (Members build OOP backend)
- Each of the 6 members creates their own feature branch (`feature/customer`, `feature/product`, etc.).
- Members **only** work inside their Java packages (`src/main/java/com/crumb/<component>/`).
- They make 5–6 small commits each using the messages in `COMMIT_GUIDE.md`.
- They merge their branches into `main`. (Zero merge conflicts because nobody touches `static/`!).
*Examiner story:* "Our team spent weeks 2 through 4 strictly in Java writing the entity inheritance hierarchies, repositories, polymorphic algorithms, and REST controllers."

---

### Phase 3: At the very end (You integrate the full-stack UI)
1. Copy the files from `version-2-final-integrated/` into `src/main/resources/static/`.
2. Commit under your GitHub account:
   ```bash
   git add src/main/resources/static/
   git commit -m "ui: connect storefront components and interactive modals to backend REST APIs"
   git commit -m "ui: add comprehensive administrative dashboard with dark luxury glass UI"
   git push origin main
   ```
*Examiner story:* "Once all backend REST controllers were completed, our Integration Lead connected the JavaScript API integration files to wire the frontend directly to everyone's endpoints."
