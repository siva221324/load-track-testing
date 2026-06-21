# LoadTrack — Truck Operations Management System

A full-stack web application for sand transportation businesses to digitally manage trucks, drivers, dealers, trips, payments, dues, interest, receipts, and reports.

## Tech stack

- **Frontend:** Angular (latest LTS) · Angular Material · TypeScript · RxJS
- **Backend:** Spring Boot 3.x · Spring Security · Spring Data JPA · JWT
- **Database:** PostgreSQL (Neon cloud)
- **Build:** Maven (backend), npm (frontend)
- **Hosting (planned):** Vercel (frontend) · Render (backend) · Neon (DB)

## Project structure

```
LoadTrack/
├── backend/                  Spring Boot project (Maven)
├── frontend/                 Angular project
├── docs/                     Schema, ER diagram, API contracts (added in Phase 2)
├── IMPLEMENTATION_PLAN.md    Full phased plan (read this first)
├── .gitignore
└── README.md
```

## Roles

- **Admin** — full access (manage trucks, drivers, dealers, trips, payments, settings, receipts, reports)
- **Driver** — view assigned trips, view salary
- **Dealer** — view payment history, pending dues, receipts

## Local setup

### Prerequisites
- Java 21 (Temurin / Corretto / Microsoft OpenJDK)
- Node.js LTS (current LTS — 22.x or 24.x)
- Angular CLI (`npm install -g @angular/cli`)
- DBeaver Community (or any PostgreSQL GUI)
- A Neon account with a `dev` branch (free tier — neon.tech)

### Backend

```powershell
cd backend
# Copy the example properties file and fill in your real Neon credentials
copy src\main\resources\application.properties.example src\main\resources\application.properties
# Then edit application.properties with your Neon URL/user/password
# (application.properties is gitignored — your real password stays local)
.\mvnw spring-boot:run
```
Backend runs on **http://localhost:8080**.

### Frontend

```powershell
cd frontend
npm install
ng serve --open
```
Frontend runs on **http://localhost:4200**.

## Implementation phases

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the full 14-phase plan.

| Phase | Status |
|-------|--------|
| 0 — Tools & accounts | ✓ Done |
| 1 — Project scaffolding | ✓ Done |
| 2 — Database schema & ER diagram | Next |
| 3 — Authentication (JWT) | Pending |
| 4 — Truck CRUD | Pending |
| 5 — Driver CRUD | Pending |
| 6 — Dealer CRUD | Pending |
| 7 — Sand Types + Settings | Pending |
| 8 — Trip Management (core) | Pending |
| 9 — Payments + Interest | Pending |
| 10 — Receipt PDF generation | Pending |
| 11 — Driver / Dealer portals | Pending |
| 12 — Dashboard + Reports + Excel | Pending |
| 13 — Cross-cutting polish | Pending |
| 14 — Deployment (Vercel + Render + Neon) | Pending |
