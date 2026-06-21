---
pdf_options:
  format: A4
  margin: 20mm
  printBackground: true
stylesheet:
  - https://cdn.jsdelivr.net/npm/water.css@2/out/water.css
---

# LoadTrack — Technical Design Document

**Project:** LoadTrack — Truck Operations Management System
**Audience:** Engineers, interviewers, future maintainers
**Companion to:** FRS document (1_FRS.pdf)

---

## 1. Architecture Overview

LoadTrack follows the **classic three-tier architecture**:

```
┌─────────────────────────────────────────────────────────────┐
│  Presentation Tier (Vercel — global CDN)                    │
│  ─────────────────────────────────────────────────────      │
│  Angular 21 SPA · Angular Material · Chart.js · TypeScript  │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS / JSON / JWT
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  Application Tier (Render — Docker container)               │
│  ─────────────────────────────────────────────────────      │
│  Spring Boot 3.5 · Spring Security · Spring Data JPA        │
│  REST controllers · Services · Repositories                 │
└───────────────────────────┬─────────────────────────────────┘
                            │ JDBC (TLS)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  Data Tier (Neon — serverless PostgreSQL)                   │
│  ─────────────────────────────────────────────────────      │
│  PostgreSQL 16 · pooler endpoint · production branch        │
└─────────────────────────────────────────────────────────────┘
```

### 1.1 Why three tiers (and not a monolith)?

- **Independent scaling.** Frontend traffic and API traffic scale separately; the frontend is just static files behind a CDN.
- **Security.** Database credentials never reach the browser. Cross-Origin Resource Sharing (CORS) is the only entry point and is locked to the Vercel domain.
- **Free hosting.** Each tier has a generous free plan: Vercel for static SPAs, Render for containers, Neon for Postgres.

### 1.2 Request lifecycle (login example)

1. User submits `{username, password}` from Angular login form
2. Angular's `AuthService` posts to `POST /api/auth/login` on Render
3. Spring's `AuthController` validates input, calls `UserDetailsService` and `AuthenticationManager`
4. On success, `JwtUtil` issues an HS256-signed token; response includes `{token, role, username, userId}`
5. Frontend stores token in `localStorage`; the `jwtInterceptor` attaches it to every subsequent HTTP request
6. The `JwtAuthFilter` on the backend validates the token on every protected request

---

## 2. Technology Stack

### 2.1 Backend

| Layer | Technology | Version | Why this choice |
|-------|-----------|---------|-----------------|
| Language | Java | 21 (Temurin) | LTS, modern record/pattern syntax, free with no Oracle license |
| Framework | Spring Boot | 3.5.14 | Industry standard, opinionated defaults, large ecosystem |
| Security | Spring Security | 6.x (transitive) | Battle-tested filter chain, easy JWT integration |
| ORM | Spring Data JPA / Hibernate | 6.x | Reduces boilerplate; supports JPA Specifications for dynamic queries |
| Database driver | PostgreSQL JDBC | Latest | Official driver |
| JWT | JJWT | 0.12.6 | Modern API (no deprecated builders), HS256 support |
| PDF generation | OpenPDF | 2.0.3 | LGPL fork of iText 5 — free for commercial use, no AGPL contagion |
| Excel generation | Apache POI (poi-ooxml) | 5.2.5 | De facto standard for .xlsx in Java |
| API docs | springdoc-openapi | 2.8.5 | Auto-generates Swagger UI from Spring annotations |
| Helper lib | Lombok | (transitive) | Removes getter/setter boilerplate |
| Build | Maven | 3.x (wrapper) | Wrapper ships with the repo; no global install needed |
| Container | Docker | Multi-stage | Build with JDK, run with JRE; image ~280 MB |

### 2.2 Frontend

| Layer | Technology | Version | Why |
|-------|-----------|---------|-----|
| Framework | Angular | 21.2 (NgModule mode) | Full-featured framework, RxJS, dependency injection, Material |
| UI kit | Angular Material | 21 (Azure/Blue theme) | Pre-built accessible components |
| Charts | Chart.js + ng2-charts | 4.5 / 10 | Lightweight, declarative bar/doughnut charts |
| HTTP | Angular HttpClient + functional interceptors | n/a | Cleaner than the older class-based interceptors |
| State | Angular Signals + RxJS where streams help | 21 | Signals for component-local state, RxJS for HTTP |
| Forms | Reactive Forms | 21 | Type-safe, supports cross-field validators |
| Auth | JWT in localStorage + auth guard + role guard | n/a | Stateless, survives refresh |
| Layout | CDK BreakpointObserver | 21 | Drives responsive sidenav |

### 2.3 Infrastructure

| Service | Used for | Free tier limits |
|---------|----------|------------------|
| **GitHub** | Source control, CI trigger | Unlimited public + private repos |
| **Vercel** | Static frontend hosting + CDN | 100 GB/month bandwidth |
| **Render** | Backend container hosting | 512 MB RAM, spins down after 15 min idle |
| **Neon** | Serverless PostgreSQL | 5 GB storage, no card required |

---

## 3. Database Design

### 3.1 Entity-Relationship summary

10 tables. PostgreSQL with `BIGSERIAL` primary keys, foreign keys with explicit `ON DELETE` behaviour.

```
roles ──────┐
            ├── users ─── (linked_driver_id) ─── drivers
            │           └─(linked_dealer_id) ─── dealers
            │
sand_types ─┼─── trips ───┬─── trucks
            │             ├─── drivers
            │             └─── dealers
            │              │
            │              └─── payments ─── payment_transactions
            │                       │
            │                       └─── receipts
            │
settings (singleton)
trip_requests ───── dealers
              ───── sand_types
              ───── trips (optional, set on approval)
```

### 3.2 Tables and key columns

| Table | Purpose | Notable columns / constraints |
|-------|---------|-------------------------------|
| `roles` | Lookup for ADMIN / DRIVER / DEALER | `name UNIQUE`, CHECK in (`ADMIN`, `DRIVER`, `DEALER`) |
| `users` | Authentication | `username UNIQUE`, BCrypt `password`, `linked_driver_id`, `linked_dealer_id` (mutually exclusive nullable FKs) |
| `trucks` | Fleet inventory | `truck_number UNIQUE`, `status` enum check |
| `drivers` | Driver master data | `phone UNIQUE`, FK to `trucks` (nullable, on-delete SET NULL) |
| `dealers` | B2B customer master data | `phone UNIQUE` |
| `sand_types` | Product catalog | `name UNIQUE`, `price_per_ton NUMERIC(10,2)` |
| `settings` | Singleton config | `interest_rate_percent NUMERIC(5,2)`, `allowed_days INT > 0` |
| `trips` | Each haul | FKs to all four masters + `rate_per_ton` (snapshotted), `total_amount` (computed and stored), `status` enum |
| `payments` | One-to-one with trips | `trip_id UNIQUE FK`, `original_amount`, `interest_amount`, `final_amount`, `paid_amount`, `due_date`, `payment_status` enum |
| `payment_transactions` | Installment history | FK to `payments` with `ON DELETE CASCADE` |
| `receipts` | PDF generation log | `receipt_number UNIQUE`, FK to `payments` |
| `trip_requests` | Dealer-initiated requests | FK to `dealers`, `sand_types`, `status` enum, optional FK to resulting `trips` |

### 3.3 Key design decisions

#### Why snapshot `rate_per_ton` on every trip?

If sand prices change next month, **historical trip totals must remain accurate**. By copying the rate at trip-creation time, the trip is immutable with respect to its billing — exactly what an accountant expects.

#### Why store `total_amount` instead of computing on read?

For reports. `SELECT SUM(total_amount) FROM trips WHERE ...` is fast. Reconstructing it on the fly via `tons × rate_per_ton` is fine for small datasets but breaks if the rate changes — see point above.

#### Why is `interest_amount` recomputed but `total_amount` stored?

Different lifecycles. `total_amount` is fixed at creation and never changes. `interest_amount` keeps growing while the payment is unpaid and overdue; the dealer expects to see the **current** number, not yesterday's snapshot. The system computes interest dynamically on every read and snapshots it into the row only at the moment the payment becomes `PAID`.

#### Why separate `payment_transactions`?

Real-world payments arrive in installments. Storing only a single `paidAmount` would lose the history. A separate table preserves "₹5,000 on Apr 1, ₹3,000 on Apr 7" — needed for the receipt PDF and for resolving disputes.

#### Why `users` references `drivers` and `dealers` (and not the other way round)?

Login is the optional concern. A driver entity exists in its own right; the login is added later. Reversing this (making `drivers.user_id` mandatory) would force creating a user even for drivers who never log in. Nullable FK on `users` keeps both flows clean and lets a driver record exist without a login.

#### Why a singleton `settings` table?

Interest rate and allowed-days are global business rules. A column-only design (constants in code) would require a redeploy to change. A table with one row is the simplest "admin can change without code change" approach.

---

## 4. Backend Architecture

### 4.1 Package layout

```
com.loadtrack
├── BackendApplication.java       (main, @SpringBootApplication)
├── config/                       Configuration beans
│   ├── SecurityConfig            JWT filter chain + role rules
│   ├── CorsConfig                Origin whitelist from env var
│   ├── OpenApiConfig             Swagger annotations
│   └── AdminUserSeeder           Creates admin/admin123 if users empty (NON-prod only)
├── controller/                   REST controllers (15 controllers)
├── service/                      Business logic (16 services)
├── repository/                   Spring Data JPA interfaces
├── entity/                       JPA @Entity classes (12 entities)
├── dto/                          Request / response shapes
├── security/                     JwtUtil, JwtAuthFilter, UserDetailsServiceImpl
├── exception/                    Custom exceptions + GlobalExceptionHandler
└── util/                         PDF builders, Excel builder, etc.
```

### 4.2 Layered request flow

```
HTTP request
  → JwtAuthFilter      (validates token, sets SecurityContext)
  → Controller         (parses DTO, delegates to service)
  → Service            (business logic, @Transactional)
  → Repository         (Spring Data JPA → SQL)
  → Database
  ← Entity
  ← Service returns DTO
  ← Controller wraps in ResponseEntity
  ← JSON response
```

Each layer has a single responsibility:

- **Controller** — handles HTTP (status codes, validation annotations, role checks via `@PreAuthorize` or path-level `SecurityConfig`). Never touches entities directly.
- **Service** — contains the business logic, manages transactions, converts entities to DTOs.
- **Repository** — extends `JpaRepository<T, Long>` and `JpaSpecificationExecutor<T>` for dynamic queries.
- **Entity** — Lombok-annotated POJOs mapped to tables.

### 4.3 Cross-cutting concerns

| Concern | Implementation |
|---------|----------------|
| **Validation** | `jakarta.validation` annotations on DTO fields (`@NotBlank`, `@Min`, `@Size`). Controllers add `@Valid`. Errors caught by `GlobalExceptionHandler` returning 400 with field-level messages. |
| **Exception handling** | `@RestControllerAdvice` class handles `MethodArgumentNotValidException`, `DataIntegrityViolationException`, `ResourceNotFoundException`, `NoResourceFoundException`, and generic `Exception`. |
| **Transactions** | Service methods annotated `@Transactional` (write) or `@Transactional(readOnly = true)` (read). |
| **Logging** | SLF4J + Logback. SQL logging silenced in dev to keep console clean. |
| **CORS** | `CorsConfig` reads `cors.allowed-origins` from `application.properties` or env var; supports comma-separated list. |
| **Profile-aware seeding** | `AdminUserSeeder` is `@Profile("!prod")` — production starts with no users so the deployer signs up. |

### 4.4 Security design

#### Filter chain

```
HTTP request
  → CorsFilter
  → JwtAuthFilter (custom, OncePerRequestFilter)
      ├─ extract "Authorization: Bearer ..." header
      ├─ validate signature + expiry with JwtUtil
      ├─ load UserDetails by username
      └─ set Authentication on SecurityContext
  → SecurityConfig.authorizeHttpRequests
      ├─ /api/auth/** → permitAll
      ├─ /swagger-ui/**, /v3/api-docs/** → permitAll
      ├─ /api/trucks/** → hasRole(ADMIN)
      ├─ /api/drivers/** → hasRole(ADMIN)
      ├─ /api/dealers/** → hasRole(ADMIN)
      ├─ GET /api/sand-types/** → hasAnyRole(ADMIN, DEALER, DRIVER)
      ├─ /api/sand-types/** → hasRole(ADMIN)
      ├─ /api/trips/**, /api/payments/**, /api/dashboard/**, /api/reports/**, /api/trip-requests/** → hasRole(ADMIN)
      ├─ /api/receipts/** → hasAnyRole(ADMIN, DEALER)
      ├─ /api/me/driver/** → hasRole(DRIVER)
      ├─ /api/me/dealer/** → hasRole(DEALER)
      └─ anyRequest → authenticated
  → Controller
```

#### JWT details

- Algorithm: **HS256** (HMAC-SHA256, symmetric key)
- Secret: 48+ character random string, injected via env var `JWT_SECRET` in production
- Claims: `sub` (username), `role`, `userId`, `iat`, `exp`
- Validity: 24 hours (`JWT_EXPIRATION_MS=86400000`)
- Storage on client: `localStorage` (acceptable for this project; HttpOnly cookies would be a v2 hardening)

#### Why stateless?

- No server-side session memory — backend can scale horizontally
- Survives a Render container restart without users being logged out
- Simpler than JSESSIONID for a SPA frontend

---

## 5. Frontend Architecture

### 5.1 Module structure

```
src/app/
├── core/                         Singletons, app-wide
│   ├── guards/                   authGuard, roleGuard
│   ├── interceptors/             jwt, error, loading (functional)
│   ├── services/                 auth, dashboard, truck, driver, ...
│   └── models/                   TypeScript interfaces matching DTOs
├── shared/                       Reusable across features
│   ├── layout/                   LayoutShell (sidenav + topbar)
│   ├── dialogs/                  ConfirmDialog, CreateLoginDialog
│   └── shared-module.ts
├── features/                     Lazy-loaded feature modules
│   ├── auth/                     login, signup, forgot-password
│   ├── home/                     admin dashboard
│   ├── trucks/, drivers/, dealers/, sand-types/, settings/
│   ├── trips/, payments/, receipts/
│   ├── reports/
│   ├── driver-portal/, dealer-portal/
│   ├── trip-requests/            admin view
│   ├── dealer-requests/          dealer view
│   └── account/                  change own password
├── app-module.ts
└── app-routing-module.ts
```

### 5.2 Routing strategy

- Root route `/login` (public) and `/app/**` (auth-guarded)
- Inside `/app/**`, each feature is **lazy-loaded** (`loadChildren: () => import(...).then(m => m.SomeModule)`)
- Each feature route also has a `roleGuard` with `data: { roles: ['ADMIN'] }` (or DRIVER / DEALER)
- Catch-all `**` redirects to `/app/home`

Lazy loading keeps the initial bundle under 500 KB; feature modules load on first navigation to that area.

### 5.3 State management

The project deliberately avoids a heavy state library (NgRx, Akita). Three patterns suffice:

- **Angular Signals** for component-local state (loading flags, computed UI flags)
- **RxJS Observables** for HTTP and effects
- **Services** for cross-component shared state (e.g. `AuthService` exposes the current user as a signal)

Rationale: this is a CRUD app with no real-time updates; flux-style state is overkill and creates ceremony.

### 5.4 Functional HTTP interceptors

Modern Angular replaced class-based interceptors with functional ones. Three are registered globally:

```typescript
provideHttpClient(withInterceptors([
  loadingInterceptor,  // shows mat-progress-bar on any pending request
  jwtInterceptor,      // attaches Authorization header
  errorInterceptor     // on 401 clears token and redirects to /login
]))
```

### 5.5 Material dialog pattern

Material's `MAT_DIALOG_DATA` cannot be injected as a constructor parameter when class fields are initialised with `this.data` — class field initialisers run **before** constructor params bind, producing TS2729. The fix used throughout this codebase:

```typescript
export class ApproveDialog {
  protected data = inject<TripRequest>(MAT_DIALOG_DATA);
  protected dialogRef = inject(MatDialogRef<ApproveDialog>);
  // ...
}
```

`inject()` works at construction time, so class field initialisers can safely reference `this.data`.

### 5.6 Responsive design

Implemented in two places:

1. **`LayoutShell`** uses CDK's `BreakpointObserver` to flip the sidenav mode between `side` (desktop) and `over` (mobile). A hamburger toggle appears in the toolbar only on small screens.
2. **Global SCSS** in `styles.scss` adds media queries at 768px that stack form rows vertically, shrink card padding, and wrap tables in a horizontally-scrollable container.

---

## 6. Module-by-Module Implementation

### 6.1 Authentication (Phase 3)

**Backend:**
- `AuthController.login` accepts `{username, password}`. Spring's `AuthenticationManager` checks BCrypt match against `users.password`. On success, `JwtUtil.generateToken` issues an HS256 JWT with claims `{sub, role, userId, iat, exp}`.
- `AuthController.signup` is permitAll; creates a new `User` with role `ADMIN` and BCrypt-hashed password.
- `AuthController.forgotPassword` resets a user's password to the temp value `Loadtrack@123` (no email infrastructure required for this project).
- `JwtAuthFilter` extends `OncePerRequestFilter`. Skips public endpoints. Pulls the bearer token, validates, sets `Authentication` on `SecurityContextHolder`.

**Frontend:**
- `AuthService` is a singleton (`providedIn: 'root'`) that owns the token. Exposes signals: `token`, `role`, `user`, `isLoggedIn`.
- `authGuard` is a functional `CanActivateFn` that returns `true` if logged in or a `UrlTree` to `/login` otherwise.
- `roleGuard` reads `route.data.roles` and matches against `auth.role()`.

### 6.2 CRUD Modules (Phases 4–7)

Trucks, drivers, dealers, sand types, and settings all follow the same vertical template:

**Backend per module:**
- Entity (`@Entity`, Lombok)
- Repository (extends `JpaRepository`)
- Service (`@Service`, `@Transactional`, returns DTOs)
- Controller (CRUD endpoints, `@Valid` request bodies)
- Request / Response DTOs

**Frontend per module:**
- Routing module → list page → form dialog (used for both add and edit)
- Service that calls the backend via `HttpClient`
- Material table with `mat-paginator`, `mat-sort`, search input
- `ConfirmDialog` from `shared/` before delete
- `MatSnackBar` for success / error feedback

The repetition is intentional — each module is a near-identical copy with different fields. Trying to abstract this would create premature complexity.

### 6.3 Trip Management (Phase 8)

The most logic-heavy module.

**Trip creation transaction (`TripService.create`):**

```
@Transactional
public TripResponse create(TripRequest req) {
    Truck truck   = truckRepo.findById(req.getTruckId()).orElseThrow(...);
    if (!"AVAILABLE".equals(truck.getStatus())) throw new IllegalStateException(...);
    Driver driver = driverRepo.findById(req.getDriverId()).orElseThrow(...);
    Dealer dealer = dealerRepo.findById(req.getDealerId()).orElseThrow(...);
    SandType st   = sandTypeRepo.findById(req.getSandTypeId()).orElseThrow(...);
    Settings s    = settingsRepo.findFirstByOrderByIdAsc().orElseThrow(...);

    BigDecimal totalAmount = req.getTons().multiply(st.getPricePerTon());

    Trip trip = Trip.builder()
        .truck(truck).driver(driver).dealer(dealer).sandType(st)
        .tons(req.getTons())
        .ratePerTon(st.getPricePerTon())     // snapshot!
        .totalAmount(totalAmount)
        .tripDate(req.getTripDate())
        .status("PENDING")
        .build();
    tripRepo.save(trip);

    Payment payment = Payment.builder()
        .trip(trip)
        .originalAmount(totalAmount)
        .interestAmount(BigDecimal.ZERO)
        .finalAmount(totalAmount)
        .paidAmount(BigDecimal.ZERO)
        .paymentStatus("PENDING")
        .dueDate(req.getTripDate().plusDays(s.getAllowedDays()))
        .build();
    paymentRepo.save(payment);

    truck.setStatus("ON_TRIP");
    truckRepo.save(truck);

    return TripResponse.from(trip);
}
```

All five mutations (validate, save trip, save payment, flip truck, return) succeed or all fail together because they are wrapped in a single `@Transactional` method.

### 6.4 Payments and Interest (Phase 9)

Interest is computed on every read so dealers always see the live amount. The calculation lives in `PaymentService.computeInterest`:

```
private BigDecimal computeInterest(Payment p, Settings settings, LocalDate today) {
    if (!p.getDueDate().isBefore(today)) return BigDecimal.ZERO;
    BigDecimal rate = settings.getInterestRatePercent()
            .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    return p.getOriginalAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
}
```

When admin marks a payment as paid, the current interest is snapshotted into the row and the status becomes `PAID` (locking the numbers) or `PARTIAL` (interest will keep recomputing).

**Money type:** `BigDecimal` throughout — never `double`. PostgreSQL column type is `NUMERIC(12,2)`.

**Filtering:** `Specification<Payment>` builds dynamic predicates from optional query params (`dealerId`, `status`, `overdueOnly`). This is JPA's idiomatic alternative to writing many bespoke query methods.

### 6.5 Receipt PDF (Phase 10)

Implementation:
- `ReceiptService.generate` loads the payment, builds a `PdfTicket` model, calls `ReceiptPdfBuilder` which streams a PDF into a `ByteArrayOutputStream` and returns the bytes.
- Controller wraps the bytes in `ResponseEntity<byte[]>` with `Content-Type: application/pdf` and `Content-Disposition: attachment`.
- **No disk writes** — Render's container filesystem is ephemeral and would lose receipts on every redeploy.

Sections in the PDF: header logo placeholder, dealer block, trip block, payment block (with overdue interest line item if applicable), installment-history table from `payment_transactions`, footer.

### 6.6 Driver and Dealer Portals (Phase 11)

**Auto-login feature** when admin creates a driver / dealer:

```
public DriverResponse createDriver(DriverRequest req) {
    Driver driver = driverRepo.save(buildDriver(req));
    String username = req.getPhone();                    // try phone first
    if (userRepo.existsByUsername(username)) {
        username = "drv_" + driver.getId();              // collision fallback
    }
    userService.createDriverLogin(driver, username, "Loadtrack@123");
    return DriverResponse.fromWithCredentials(driver, username, "Loadtrack@123");
}
```

The frontend reads the temporary credentials from the response and shows them in a `MatSnackBar` for 12 seconds with a "Copy" button. After that, the password is gone from frontend memory and known only to the driver/dealer.

**Portal endpoints** are scoped by `CurrentUserService.getDriverIdOrThrow()` which extracts the driver_id from the JWT principal. Query params cannot override this — preventing horizontal privilege escalation.

### 6.7 Trip Request Workflow (post-Phase 13)

Two new entities/controllers/services:

- `entity.TripRequest` — separate from existing `dto.TripRequest`; the dto class predates this feature
- `TripRequestController` for admin (list, approve, reject)
- A separate dealer endpoint at `/api/me/dealer/trip-requests` for dealers to create and list their own

**Approval transaction:**

```
@Transactional
public TripResponse approve(Long requestId, ApproveTripRequest body) {
    TripRequest req = repo.findById(requestId).orElseThrow(...);
    if (!"PENDING".equals(req.getStatus())) throw new IllegalStateException(...);

    // Re-use TripService.create — single source of truth for trip creation logic
    Trip trip = tripService.createFromRequest(req, body.getTruckId(), body.getDriverId());

    req.setStatus("APPROVED");
    req.setApprovedTripId(trip.getId());
    req.setAdminNotes(body.getNotes());
    return TripResponse.from(trip);
}
```

The sidebar uses an HTTP poll every 60 seconds to update the pending count badge.

### 6.8 Admin Dashboard (Phase 12a)

Single endpoint `/api/dashboard/admin` returns one `AdminDashboardResponse` DTO containing all six KPIs plus the last-6-months earnings array and the trips-by-status map. Avoiding a waterfall of separate endpoints keeps initial dashboard load under one round trip.

Frontend uses `ng2-charts` v10 with `provideCharts(withDefaultRegisterables())` — discovered during deployment that Angular's prod build tree-shakes Chart.js controllers unless they are explicitly registered.

### 6.9 Reports (Phase 12b)

`ReportController` exposes two report endpoints with rich query parameters:

- `GET /api/reports/trips?from=&to=&driverId=&dealerId=&status=`
- `GET /api/reports/payments?from=&to=&dealerId=&status=&overdueOnly=`

For exports, two more endpoints per report: `?format=pdf` or `?format=xlsx`. Same controller, same query parsing, dispatches to either `ReportPdfBuilder` (OpenPDF, landscape A4) or `ExcelUtil` (Apache POI `XSSFWorkbook`).

Excel example:

```
XSSFWorkbook wb = new XSSFWorkbook();
XSSFSheet sheet = wb.createSheet("Trips");
Row header = sheet.createRow(0);
String[] columns = { "Date", "Truck", "Driver", "Dealer", "Tons", "Amount", "Status" };
for (int i = 0; i < columns.length; i++) header.createCell(i).setCellValue(columns[i]);
sheet.createFreezePane(0, 1);
// ... rows ...
wb.write(outputStream);
```

---

## 7. Deployment Topology

### 7.1 Three-service deployment

| Service | URL | Hosted on |
|---------|-----|-----------|
| Frontend | https://loadtrack-gamma.vercel.app | Vercel (global CDN) |
| Backend | https://loadtrack-backend.onrender.com | Render (Docker, Singapore) |
| Database | Neon production branch (ap-southeast-1) | Neon serverless |

### 7.2 Dockerfile (backend)

Multi-stage build keeps the runtime image small (~280 MB):

```
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw . ; COPY .mvn .mvn ; COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT:-8080}"]
```

The shell-form `ENTRYPOINT` is critical: Render assigns the listen port dynamically via `$PORT`. A hardcoded port causes Render to kill the container with "no open ports detected".

### 7.3 Environment configuration

Production overrides are entirely via env vars on Render — no `application-prod.properties` file is needed because Spring Boot's relaxed binding maps `SPRING_DATASOURCE_URL` to `spring.datasource.url`, etc.

Env vars set on Render:

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-pooler-host>/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=<prod password>
SPRING_JPA_HIBERNATE_DDL_AUTO=update
JWT_SECRET=<random 48 chars, distinct from dev>
JWT_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGINS=https://loadtrack-gamma.vercel.app
```

### 7.4 CI/CD

- `git push origin main` triggers **two builds in parallel**:
  - Vercel detects the change, runs `npm run build -- --configuration production`, and atomically promotes the new build
  - Render detects the change, rebuilds the Docker image, runs it
- No manual deploy step. No bespoke CI script.

---

## 8. Notable Design Decisions

### 8.1 Why store interest *and* recompute it?

`payments.interest_amount` is updated only at the moment the payment becomes `PAID`. While the payment is open, `interest_amount` may be stale on the row but the **read path always recomputes** in `PaymentService.buildResponse`. This gives:

- A live, correct "amount due" for every read
- A historically accurate, frozen number once payment is recorded

### 8.2 Why JPA Specifications instead of bespoke query methods?

The Payments and Reports lists support 3–5 optional filters each. Writing every combination as a `findByXAndYAndZ` method explodes. Specifications let the service compose `WHERE` predicates programmatically:

```
Specification<Payment> spec = (root, q, cb) -> {
    List<Predicate> p = new ArrayList<>();
    if (dealerId != null) p.add(cb.equal(root.get("trip").get("dealer").get("id"), dealerId));
    if (status != null)   p.add(cb.equal(root.get("paymentStatus"), status));
    if (Boolean.TRUE.equals(overdueOnly)) {
        p.add(cb.notEqual(root.get("paymentStatus"), "PAID"));
        p.add(cb.lessThan(root.get("dueDate"), today));
    }
    return cb.and(p.toArray(new Predicate[0]));
};
return paymentRepo.findAll(spec, pageable).map(...);
```

### 8.3 Why `@Profile("!prod")` on `AdminUserSeeder`?

Default credentials `admin/admin123` are a security risk in production. By scoping the seeder to non-prod profiles, the production database starts empty and the first admin signs up themselves at `/signup` — choosing their own password.

### 8.4 Why functional interceptors over class-based?

Angular's class-based `HttpInterceptor` is on a soft deprecation path. Functional interceptors (`HttpInterceptorFn`) are simpler — they are plain functions — and compose cleanly via `withInterceptors([...])`.

### 8.5 Why `ddl-auto=update` in production?

The conventional advice is `validate` (strict, no schema changes). In practice `update` is safer for a small project:

- `update` only **adds** missing columns / tables — it never drops or alters destructively
- `validate` fails the entire boot if even a minor mismatch exists (e.g. a missing index)

For LoadTrack's scale, `update` is a reasonable trade-off. A v2 hardening would switch to **Flyway / Liquibase** migrations and pair them with `validate`.

### 8.6 Why no caching layer?

Premature. The Neon free tier's response times are already well under 100ms via the pooler. Caching adds invalidation complexity. If load grew, the obvious caches would be:

- `settings` (read on every payment list) → in-memory cache, invalidated on settings save
- `sand_types` (read often for trip/request forms) → similar

---

## 9. Testing Strategy

What exists today:
- Spring Boot starter test on the classpath
- Manual end-to-end smoke tests at every phase boundary
- Swagger UI for hand-testing every endpoint
- Browser DevTools network tab for verifying request/response shape

What would be added for v2:
- Service-layer unit tests with `@MockBean` repositories
- Slice tests via `@WebMvcTest` for each controller
- Integration tests with Testcontainers Postgres
- Cypress / Playwright tests for the critical user journeys (login → create trip → mark paid → download receipt)

---

## 10. Known Limitations

1. **Cold start on Render free tier** — first request after 15 min idle takes ~30 seconds. A UptimeRobot-style ping every 14 minutes is the cheap workaround.
2. **No real-time updates** — sidebar pending-count polls every 60 seconds; a websocket would be cleaner.
3. **No email infrastructure** — forgot-password resets to a known temp value rather than emailing a magic link. Adding email would require SMTP credentials (Mailgun or Resend free tier).
4. **No audit log** — admin actions are not recorded. A v2 would add an `audit_events` table with INSERTs from a Spring AOP aspect.
5. **No bulk import** — every truck / driver / dealer must be entered manually.

---

## 11. File-and-Class Quick Reference

| What | Where |
|------|-------|
| Spring main class | `backend/src/main/java/com/loadtrack/BackendApplication.java` |
| Security filter chain | `backend/.../config/SecurityConfig.java` |
| CORS | `backend/.../config/CorsConfig.java` |
| JWT issue/verify | `backend/.../security/JwtUtil.java` |
| JWT filter | `backend/.../security/JwtAuthFilter.java` |
| Interest calculation | `backend/.../service/PaymentService.java::computeInterest` |
| Trip + Payment + Truck transaction | `backend/.../service/TripService.java::create` |
| PDF receipt | `backend/.../util/ReceiptPdfBuilder.java` |
| Excel report | `backend/.../util/ExcelUtil.java` |
| Angular app module | `frontend/src/app/app-module.ts` |
| Routing | `frontend/src/app/app-routing-module.ts` |
| Layout shell (responsive) | `frontend/src/app/shared/layout/layout-shell.ts` |
| Auth service | `frontend/src/app/core/services/auth.service.ts` |
| Auth guard | `frontend/src/app/core/guards/auth.guard.ts` |
| HTTP interceptors | `frontend/src/app/core/interceptors/` |
| Dashboard component | `frontend/src/app/features/home/home.ts` |
| Production environment | `frontend/src/environments/environment.production.ts` |
| Vercel config | `frontend/vercel.json` |
| Dockerfile | `backend/Dockerfile` |
| Database schema | `docs/schema.sql` |

---

*End of Technical Design Document — LoadTrack v1.0*
