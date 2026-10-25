# 🛠️ Team Git Setup & Branching Guide

This document explains how each of the 6 students can set up their Git author credentials, checkout their assigned feature branches, make incremental commits, and merge into `main`.

---

## 👤 1. Set Your Individual Git Author Identity

Before making commits for your component, configure your Git author name and university email address locally in this repository (or globally):

```bash
# Student 1: Customer Component Lead
git config user.name "Student 1 Name"
git config user.email "student1@university.edu"

# Student 2: Product Catalog Lead
git config user.name "Student 2 Name"
git config user.email "student2@university.edu"

# Student 3: Custom Cake Studio Lead
git config user.name "Student 3 Name"
git config user.email "student3@university.edu"

# Student 4: Order & Checkout Lead
git config user.name "Student 4 Name"
git config user.email "student4@university.edu"

# Student 5: Booking & Scheduling Lead
git config user.name "Student 5 Name"
git config user.email "student5@university.edu"

# Student 6: Payment & Reviews Lead
git config user.name "Student 6 Name"
git config user.email "student6@university.edu"
```

To verify your current local identity:
```bash
git config user.name
git config user.email
```

---

## 🌿 2. Feature Branch Naming Convention

Each component should be developed on its own dedicated branch branched off `main`:

| Member | Branch Name | Component Area |
|---|---|---|
| Member 1 | `feature/customer` | `com.crumb.customer` + auth modal + profile UI |
| Member 2 | `feature/product` | `com.crumb.product` + catalog + quick view + cart |
| Member 3 | `feature/customcake` | `com.crumb.customcake` + 2D builder visualizer |
| Member 4 | `feature/order` | `com.crumb.order` + checkout + 5-stage tracking radar |
| Member 5 | `feature/booking` | `com.crumb.booking` + capacity check + slot picker |
| Member 6 | `feature/payment` | `com.crumb.payment` + card Luhn check + review modal |
| Integration | `feature/admin-portal`| `admin.html` + `admin.js` + `admin.css` |

### How to Create & Work on Your Branch:
```bash
# 1. Switch to main and pull latest code
git checkout main
git pull origin main

# 2. Create and switch to your feature branch
git checkout -b feature/customer

# 3. Stage and commit your component files in small steps
git add src/main/java/com/crumb/customer/entity/
git commit -m "customer: add abstract Customer entity with JPA single table inheritance"

# 4. Push your branch to GitHub
git push -u origin feature/customer
```

---

## 🔀 3. Merging Your Feature Branch into `main`

Once your component has passed tests and compiles with `mvn clean compile`:
```bash
# 1. Switch to main
git checkout main
git pull origin main

# 2. Merge your feature branch (preserves your individual commit history)
git merge --no-ff feature/customer -m "Merge branch 'feature/customer' into main"

# 3. Push to remote repository
git push origin main
```

---

## 🔍 4. Useful Git Verification Commands

```bash
# View formatted team commit history with authors
git log --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit

# Check commit stats per team member
git shortlog -sn --all

# View changes in your working directory
git status
```
