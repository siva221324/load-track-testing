---
pdf_options:
  format: A4
  margin: 20mm
  printBackground: true
stylesheet:
  - https://cdn.jsdelivr.net/npm/water.css@2/out/water.css
---

# LoadTrack — Functional Requirements Specification

**Project:** LoadTrack — Truck Operations Management System
**Version:** 1.0
**Status:** Live in Production
**Author:** Pujitha
**Date:** May 2026

---

## 1. Document Overview

### 1.1 Purpose

This Functional Requirements Specification (FRS) describes **what** LoadTrack does. It captures every user-facing feature, the actors that use them, business rules, and acceptance criteria, without prescribing how the software is built (that is covered in the Technical Design Document).

### 1.2 Scope

LoadTrack is a multi-tenant web application designed to replace manual record-keeping for **sand transportation businesses**. Truck owners traditionally use diaries, spreadsheets, and printed bills to track:

- Which truck went where with which driver
- How many tons of sand were delivered to which dealer
- How much each dealer owes, and whether they paid on time
- How much interest has accrued on overdue payments
- Salary owed to each driver per trip

LoadTrack digitises all of that, adds role-based access (admin / driver / dealer), automates billing calculations, generates downloadable PDF receipts, and provides reporting and dashboards.

### 1.3 Audience

- Hiring panels reviewing this project
- Future maintainers extending the codebase
- Business stakeholders deciding on adoption

### 1.4 Intended deployment

- **Frontend:** publicly accessible website
- **Backend:** REST API behind HTTPS
- **Users:** small fleet owners (1–50 trucks), their drivers, and their B2B customers (dealers)

---

## 2. Project Objectives

| # | Objective | Why it matters |
|---|-----------|----------------|
| 1 | Replace paper / Excel record-keeping with a digital system | Eliminates lost slips, double-entry, manual arithmetic errors |
| 2 | Enforce role-based access | Drivers see only their trips; dealers see only their dues; admin sees everything |
| 3 | Auto-calculate trip totals and overdue interest | Removes a major source of disputes between owner and dealer |
| 4 | Generate downloadable PDF receipts and Excel reports | Professional appearance, easy sharing with accountants |
| 5 | Provide an admin dashboard with KPIs | Owner sees the health of the business at a glance |
| 6 | Be reachable from any device, anywhere | Owner often supervises operations from outside the office |

---

## 3. Stakeholders and Actors

### 3.1 Stakeholders

- **Business owner** — the fleet operator who pays for LoadTrack and owns the data
- **Operations clerk / Admin user** — day-to-day operator who creates trips and records payments
- **Drivers** — employees who run the trucks
- **Dealers** — B2B customers who buy sand and owe payments

### 3.2 System actors (login roles)

| Role | What they can do |
|------|------------------|
| **ADMIN** | Full access: master data CRUD, trip lifecycle, payments, receipts, reports, dashboards, user management, trip-request approval, system settings |
| **DRIVER** | Read-only personal portal: own trips, monthly salary summary, account settings (change own password) |
| **DEALER** | Personal portal: own payments and receipts, submit trip requests for admin approval, account settings |

A user has exactly one role. Driver and Dealer logins are auto-created when their profile is added by an admin.

---

## 4. Functional Requirements

Each requirement is tagged with a unique ID (e.g. **FR-AUTH-01**) for traceability.

### 4.1 Authentication and Authorization (FR-AUTH)

| ID | Requirement |
|----|-------------|
| FR-AUTH-01 | Users shall log in with a username and password. Successful login returns a JSON Web Token (JWT) valid for 24 hours. |
| FR-AUTH-02 | Passwords shall be stored as BCrypt hashes, never plain text. |
| FR-AUTH-03 | New users may self-register through a public signup page. Signups default to the **ADMIN** role to support first-time deployment by a fleet owner. |
| FR-AUTH-04 | Users who forget their password may request a reset; the system resets the password to a known temporary value (`Loadtrack@123`) and the user must change it on next login. |
| FR-AUTH-05 | Every API endpoint except `/api/auth/**`, `/swagger-ui/**`, and `/v3/api-docs/**` shall require a valid JWT. |
| FR-AUTH-06 | The system shall enforce role-based authorisation. Driver-portal endpoints reject ADMIN and DEALER tokens; dealer-portal endpoints reject ADMIN and DRIVER tokens; admin endpoints reject DRIVER and DEALER tokens. |
| FR-AUTH-07 | Tokens that are missing, invalid, or expired produce a 401 response and the frontend redirects the user to the login page. |
| FR-AUTH-08 | Users may change their own password from the **Account** page after authenticating with their current password. |

### 4.2 Truck Management (FR-TRK) — Admin only

| ID | Requirement |
|----|-------------|
| FR-TRK-01 | Admin shall create a truck with: truck number (unique), model, capacity in tons, insurance number, RC number. |
| FR-TRK-02 | Admin shall list trucks with pagination and may filter by status (`AVAILABLE` / `ON_TRIP` / `MAINTENANCE`). |
| FR-TRK-03 | Admin shall edit a truck's details, but cannot change its number after creation if it has historical trips. |
| FR-TRK-04 | Admin shall delete a truck only if it has no trips referencing it. Otherwise the system shall return a friendly message explaining the FK constraint. |
| FR-TRK-05 | A truck's status shall change automatically: `AVAILABLE` → `ON_TRIP` when a trip is created, → `AVAILABLE` again when the trip is marked `COMPLETED`. |

### 4.3 Driver Management (FR-DRV) — Admin only

| ID | Requirement |
|----|-------------|
| FR-DRV-01 | Admin shall create a driver with: name, phone number (unique), license number, address, salary per trip, optionally assign one truck. |
| FR-DRV-02 | When a driver is created, the system shall **auto-create a user login** for them with role DRIVER. Username defaults to the driver's phone number; password defaults to `Loadtrack@123`. The admin is shown the credentials in a snackbar for 12 seconds and may copy them. |
| FR-DRV-03 | If the auto-derived username collides with an existing user, the system shall fall back to `drv_<id>` to guarantee uniqueness. |
| FR-DRV-04 | Admin shall edit or delete drivers; deletes are blocked when the driver has trips. |

### 4.4 Dealer Management (FR-DLR) — Admin only

| ID | Requirement |
|----|-------------|
| FR-DLR-01 | Admin shall create a dealer with: name, phone (unique), address. |
| FR-DLR-02 | A user login of role DEALER shall be auto-created at the same time (same rules as drivers). |
| FR-DLR-03 | Admin shall edit or delete dealers; deletes are blocked when the dealer has trips or payments. |

### 4.5 Sand Types (FR-SND) — Admin only

| ID | Requirement |
|----|-------------|
| FR-SND-01 | Three sand types shall be seeded at initial deployment: Fine, Medium, Rough. |
| FR-SND-02 | Admin shall add, edit, or delete sand types. Each has a name (unique) and a price-per-ton in INR. |
| FR-SND-03 | Authenticated DEALER and DRIVER users shall be allowed to **read** the sand-type catalog (needed for dealer's trip-request form). Only ADMIN may create, update, or delete. |

### 4.6 Settings (FR-SET) — Admin only

| ID | Requirement |
|----|-------------|
| FR-SET-01 | The system shall maintain a single global settings record with two fields: `interestRatePercent` and `allowedDays`. |
| FR-SET-02 | `interestRatePercent` is a non-negative decimal applied to overdue payments. `allowedDays` is a positive integer denoting the number of days after the trip date before a payment is considered overdue. |
| FR-SET-03 | Saving Settings is upsert: first save creates the row; subsequent saves update it. |

### 4.7 Trip Management (FR-TRP) — Admin only

| ID | Requirement |
|----|-------------|
| FR-TRP-01 | A trip captures: truck, driver, dealer, sand type, tons delivered, source location, destination, trip date, and a snapshotted `ratePerTon` taken from the sand type at creation time. |
| FR-TRP-02 | On creation, the system shall compute `totalAmount = tons × ratePerTon` and store it. |
| FR-TRP-03 | On creation, the system shall atomically create a corresponding **Payment** row with status `PENDING`, paid amount 0, and `dueDate = tripDate + settings.allowedDays`. |
| FR-TRP-04 | On creation, the truck's status shall flip from `AVAILABLE` to `ON_TRIP`. A trip cannot be created for a truck whose status is not `AVAILABLE`. |
| FR-TRP-05 | A trip's status flows `PENDING → STARTED → COMPLETED`. On `COMPLETED`, the truck status reverts to `AVAILABLE`. |
| FR-TRP-06 | The trip list shall support filters by status, date range, driver, dealer, and pagination. |
| FR-TRP-07 | Changing `pricePerTon` on a sand type **shall not** retroactively change historical trip totals — the rate is snapshotted on the trip record. |

### 4.8 Payments and Interest (FR-PAY) — Admin only (read-only for affected dealer)

| ID | Requirement |
|----|-------------|
| FR-PAY-01 | Each payment is one-to-one with a trip. |
| FR-PAY-02 | The system shall calculate interest on the fly for any payment whose status is not `PAID` and whose due date has passed. Formula: `interest = originalAmount × (settings.interestRatePercent / 100)`. |
| FR-PAY-03 | Interest is **not snapshotted** while the payment is open — it is recomputed on every read so dealers see the current amount due. Interest is locked into the row only at the moment the payment becomes `PAID`. |
| FR-PAY-04 | Admin shall record a payment with a `paidAmount`. If the cumulative paid amount equals the final amount (original + interest), the status becomes `PAID`. Otherwise it becomes `PARTIAL`. |
| FR-PAY-05 | Overpayment shall be rejected with a clear error message stating the overage. |
| FR-PAY-06 | Every individual payment installment shall be recorded as a `PaymentTransaction` row, preserving a full installment history. |
| FR-PAY-07 | The payment list shall support filters by dealer, by status, and an `overdueOnly=true` flag. |

### 4.9 Receipt Generation (FR-RCP)

| ID | Requirement |
|----|-------------|
| FR-RCP-01 | Admin shall be able to generate a PDF receipt for any payment that has at least one transaction. |
| FR-RCP-02 | Dealers shall be able to view and download receipts for their own payments. |
| FR-RCP-03 | The receipt PDF shall include: receipt number, generation date, dealer block, trip block (truck/driver/dealer/sand type/tons/route/date), payment block (original / interest / final / paid amount / due date / status), an installment-history table, and the company brand. |
| FR-RCP-04 | The PDF shall be generated in memory (no disk writes) so it works on ephemeral cloud filesystems. |

### 4.10 Trip Request Workflow (FR-REQ)

| ID | Requirement |
|----|-------------|
| FR-REQ-01 | A logged-in DEALER shall be able to submit a trip request containing: sand type, tons, pickup location, delivery location, preferred date, optional notes. The system shows an estimated cost based on current rates. |
| FR-REQ-02 | A submitted request has status `PENDING`. |
| FR-REQ-03 | The DEALER may cancel any of their own `PENDING` requests. |
| FR-REQ-04 | ADMIN sees a paginated list of all dealer requests, filterable by status. A sidebar badge shows the count of PENDING requests. |
| FR-REQ-05 | ADMIN may **approve** a `PENDING` request by selecting a truck and driver. Approval shall atomically: create a Trip, create the corresponding Payment, flip the truck to `ON_TRIP`, and link the resulting Trip ID back onto the request. The request status becomes `APPROVED`. |
| FR-REQ-06 | ADMIN may **reject** a `PENDING` request with a mandatory rejection reason. Reason is saved to `adminNotes`. The request status becomes `REJECTED`. |
| FR-REQ-07 | DEALER sees their request list with current status, admin notes (if any), and a link to the resulting Trip ID when approved. |

### 4.11 Driver Portal (FR-DPO) — DRIVER only

| ID | Requirement |
|----|-------------|
| FR-DPO-01 | A logged-in DRIVER shall see a dashboard scoped to themselves: total trips this month, total tons hauled, monthly salary earned. |
| FR-DPO-02 | DRIVER shall list their own trips with status filter chips. They cannot see trips of other drivers. |
| FR-DPO-03 | All driver-portal endpoints derive the driver ID from the JWT principal — query parameters cannot override it. |

### 4.12 Dealer Portal (FR-EPO) — DEALER only

| ID | Requirement |
|----|-------------|
| FR-EPO-01 | A logged-in DEALER shall see a dashboard scoped to themselves: total outstanding amount, count of overdue payments, total purchased this month. |
| FR-EPO-02 | DEALER shall list their own payments and may download receipts for any of them. |
| FR-EPO-03 | DEALER shall submit trip requests (FR-REQ-01). |
| FR-EPO-04 | All dealer-portal endpoints derive the dealer ID from the JWT principal. |

### 4.13 Admin Dashboard (FR-DSH) — Admin only

| ID | Requirement |
|----|-------------|
| FR-DSH-01 | The admin dashboard shall display six KPI cards: total trucks (with available/on-trip breakdown), total drivers, total dealers, total trips, this-month earnings, total outstanding dues. |
| FR-DSH-02 | The dashboard shall include a bar chart of last-6-months collected earnings. |
| FR-DSH-03 | The dashboard shall include a doughnut chart of trip status distribution. |
| FR-DSH-04 | All numbers shall come from a single dashboard summary endpoint to avoid waterfalls. |

### 4.14 Reports (FR-RPT) — Admin only

| ID | Requirement |
|----|-------------|
| FR-RPT-01 | Admin shall run a **Trips report** with filters: date range, driver, dealer, truck, status. The result is a tabular list. |
| FR-RPT-02 | Admin shall run a **Payments report** with filters: date range, dealer, status, overdue-only. |
| FR-RPT-03 | Both reports shall export to PDF (landscape A4) and Excel (.xlsx) using the same data source. |
| FR-RPT-04 | The PDF export shall include a header, filter summary, the data table, and a totals row. |
| FR-RPT-05 | The Excel export shall use a frozen header row and apply currency formatting where appropriate. |

### 4.15 Account (FR-ACC) — all roles

| ID | Requirement |
|----|-------------|
| FR-ACC-01 | Any logged-in user shall be able to change their own password after re-authenticating with their current password. |
| FR-ACC-02 | The form shall enforce password strength rules (min 8 chars, mix of letters and numbers). |
| FR-ACC-03 | After a successful change, the user shall be logged out and prompted to log in again with the new password. |

### 4.16 API Documentation (FR-DOC)

| ID | Requirement |
|----|-------------|
| FR-DOC-01 | The system shall expose interactive API documentation at `/swagger-ui.html`. |
| FR-DOC-02 | All controllers shall be discoverable in the UI with operation summaries and request/response schemas. |

---

## 5. Non-Functional Requirements

| Category | Requirement |
|----------|-------------|
| **Security** | All traffic over HTTPS. Passwords BCrypt-hashed. Sessions stateless (JWT only). |
| **Availability** | Best-effort on free hosting tier; cold-start under one minute acceptable for a portfolio project. |
| **Performance** | Page loads under 2 seconds (warm). Backend API responses under 500 ms for typical CRUD calls. |
| **Scalability** | Stateless backend so it can be horizontally scaled later. Pagination on every list endpoint. |
| **Usability** | Material Design components, mobile-responsive (overlay sidebar under 768px, horizontally-scrollable tables), accessible to keyboard navigation. |
| **Maintainability** | Layered architecture; one service / repository per entity; DTOs separate from entities. |
| **Browser support** | Chrome, Edge, Firefox, Safari — last 2 versions. |
| **Data integrity** | All schema-level constraints enforced (NOT NULL, UNIQUE, FK). Application-layer validations via `jakarta.validation`. |
| **Auditability** | `createdAt` / `updatedAt` timestamps on key tables; payment installments preserved as separate rows. |

---

## 6. Assumptions and Dependencies

### 6.1 Assumptions

- Each user belongs to exactly one role (no multi-role users)
- Settings are global (no per-dealer interest rates)
- A driver has at most one assigned truck at a time
- A truck cannot run two concurrent trips
- Prices are in Indian Rupees (₹) only — no multi-currency
- The system is single-tenant per deployment (one fleet owner per database)
- Time zone is local server time; no per-user timezone handling

### 6.2 External dependencies

- Neon PostgreSQL serverless (database hosting)
- Render (backend hosting)
- Vercel (frontend hosting)
- Open-source libraries: Spring Boot 3.5, Angular 21, JJWT, OpenPDF, Apache POI, Chart.js, Angular Material

---

## 7. Acceptance Criteria

The system shall be considered acceptance-complete when:

1. An ADMIN, DRIVER, and DEALER can each log in via the public website
2. All 17 functional groups (FR-AUTH through FR-DOC) pass manual smoke tests
3. Creating a trip auto-creates a payment row with the correct due date and total amount
4. Interest applied to overdue payments matches manual calculation to two decimal places
5. PDF receipts download cleanly and contain installment history
6. Reports export correctly to both PDF and Excel
7. Dashboard KPIs reflect raw SQL counts
8. All endpoints reject unauthenticated requests with 401
9. All endpoints reject cross-role access with 403
10. The frontend renders correctly on a 360px-wide mobile viewport without horizontal page scroll

---

## 8. Out of Scope (v1)

Explicitly **not** included in this release:

- Multi-currency support
- Per-dealer interest rates
- Real-time GPS tracking
- SMS / Email notifications
- Two-factor authentication
- Audit log of admin actions
- Soft-delete / archive flow
- Bulk import (CSV / Excel)
- Public API for third-party integration
- Multi-language / i18n

These are candidates for v2.

---

## 9. Glossary

| Term | Definition |
|------|------------|
| **Admin** | User role with full system access. |
| **Driver** | User role limited to their own trips and salary. |
| **Dealer** | User role; a B2B customer who buys sand from the fleet owner. |
| **Trip** | A single hauling job — one truck delivering tonnage of sand from a source to a destination. |
| **Payment** | The billing record auto-generated for each trip. Has original amount, possible interest, final amount, paid amount, and a due date. |
| **Receipt** | A downloadable PDF document corresponding to a payment, optionally including installment history. |
| **Settings** | Single global record with interest rate (%) and allowed days before overdue. |
| **Trip Request** | A dealer-initiated proposal that admin can approve (auto-creates a Trip) or reject. |
| **JWT** | JSON Web Token used for stateless authentication. |
| **BCrypt** | Adaptive password-hashing algorithm with built-in salt. |

---

*End of Functional Requirements Specification — LoadTrack v1.0*
