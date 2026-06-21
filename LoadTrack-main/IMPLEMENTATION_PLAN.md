# LoadTrack — Truck Operations Management System: Full Implementation Plan

## Context

**What:** A full-stack web application for sand transportation businesses to digitally manage trucks, drivers, dealers, trips, payments, dues, interest, receipts, and reports.

**Why:** Replace manual record-keeping for truck-owner businesses with a role-based digital system (Admin / Driver / Dealer) that auto-calculates trip amounts, tracks payments, applies interest on overdue dues, and generates downloadable receipts/reports.

**Starting point:** The repo at [c:\LoadTrack\LoadTrack](c:/LoadTrack/LoadTrack) is empty — only `.git/` and a 1-line `README.md` exist. We are scaffolding from zero.

**User preferences (confirmed):**
- Native installs on Windows (no Docker)
- Phased vertical slices — each phase ships one working feature end-to-end
- VS Code as the single IDE (full stack)
- JWT access-token-only (no refresh token)
- **All-free tooling only — zero paid tiers, no credit card required**
- **Hosting target: Vercel for the Angular frontend** (Spring Boot + PostgreSQL must be hosted elsewhere — see Phase 14)

**Tech stack (locked):** Angular (latest LTS) + Angular Material · Spring Boot 3.x + Spring Security + Spring Data JPA · **PostgreSQL 16** · JWT · Apache POI (Excel) · iText (PDF) · Maven.

> **DB choice update:** This plan uses PostgreSQL (local) + **Neon** (cloud) for true dev/prod parity. The original spec said "MySQL"; for a CRUD app with JPA, the engine swap is invisible to the application code, and Neon's free tier is the cleanest no-card option for cloud Postgres.

---

## All-Free Tooling Map (no credit card required)

This is the executive summary. Every tool below is permanently free for an academic / portfolio-scale project. **No tool here requires a credit card to use.** Detailed install steps follow in Phase 0.

| Need | Recommended FREE option | Account needed? | Other FREE alternatives (also no card) |
|------|-------------------------|------------------|----------------------------------------|
| **JDK 21** | Eclipse Temurin | No | Amazon Corretto · Microsoft OpenJDK · Zulu OpenJDK |
| **Node.js (current LTS)** | Official `.msi` from nodejs.org — click the "LTS" button | No | nvm-windows · fnm · Volta |
| **Angular CLI** | `npm install -g @angular/cli` | No | (no alternative — official tool) |
| **PostgreSQL (local)** | PostgreSQL 16 (EnterpriseDB installer) | **No** | PostgreSQL via Scoop / Chocolatey · pgAdmin bundle |
| **DB GUI client** | DBeaver Community | No | pgAdmin 4 (bundled with PostgreSQL installer) · VS Code PostgreSQL extension |
| **Build tool** | Maven Wrapper (`mvnw`, ships in repo) | No | Standalone Maven 3.9 |
| **API testing** | Bruno (local files, fully open source) | **No** | Thunder Client (VS Code ext) · Hoppscotch (web, no account) · Postman (free tier needs account) |
| **IDE** | VS Code | No | (per your preference, VS Code only) |
| **Git** | Git for Windows | No | GitHub Desktop |
| **Source hosting** | GitHub | Yes (free) | GitLab · Bitbucket · Codeberg |
| **Frontend hosting** | **Vercel (your choice)** | Yes (GitHub login, no card) | Netlify · Cloudflare Pages · GitHub Pages · Firebase Hosting |
| **Backend hosting (Spring Boot)** | **Render Web Service free tier** | Yes (GitHub login, no card) | Koyeb free tier · Railway ($5/mo trial credit) · Northflank · Adaptable.io |
| **Cloud PostgreSQL (free, no card)** | **Neon** (0.5 GB, ~500 ms cold start) | Yes (GitHub/Google login, no card) | Supabase (500 MB) · Aiven for PostgreSQL (1 month trial — avoid) · Render PostgreSQL (90-day expiry — avoid) |
| **PDF generation library** | iText 7 Community (AGPL) | No | OpenPDF (LGPL fork of iText 5) · Apache PDFBox |
| **Excel generation library** | Apache POI | No | (POI is the standard) |

### Free-tier limits to be aware of

- **Vercel free tier (Hobby):** 100 GB bandwidth/month, unlimited static deploys, custom domains. Plenty for an academic project.
- **Render free Web Service:** 512 MB RAM, **spins down after 15 min idle** (cold start ~30 s on first request). Acceptable for a demo. No card required.
- **Koyeb free tier:** 1 always-on small service, 512 MB RAM. No card required. Good Render alternative if Render's cold start bothers you.
- **Neon free plan:** 0.5 GB storage, 1 always-available project, ~500 ms cold start (auto-suspend after 5 min idle, wakes nearly instantly). PostgreSQL 16 wire-compatible. Branching + point-in-time restore included. **No card required at signup.**
- **GitHub free:** unlimited public + private repos, 2000 CI minutes/month — more than enough.

> **Why PostgreSQL + Neon over MySQL + TiDB?** Three reasons: (1) Neon's cold start is ~60× faster than Render's backend cold start, so Neon is effectively never the bottleneck; (2) running PostgreSQL both locally and in production gives true dev/prod parity (no JPA dialect surprises in prod); (3) Neon's free tier signup is the cleanest of any cloud DB — GitHub/Google login, zero card prompt.

---

## Phase 0 — Accounts & Tool Installation

This phase has zero code. The goal: have every tool installed, an account created where needed, and "hello world" verified for each before writing a single line of LoadTrack code.

### 0.1 Required accounts to create

| # | Account | Required? | Purpose | Free tier? |
|---|---------|-----------|---------|------------|
| 1 | **GitHub** | Strongly recommended | Source control, free private repos | Yes — sign up at github.com |
| 2 | **Neon** | **Required from day one (Neon-only mode)** | Cloud PostgreSQL — used for both development and production | Yes — login with GitHub at neon.tech |
| 3 | **Spring Initializr** | No account needed | Bootstrap Spring Boot project at `start.spring.io` | n/a |
| 4 | **Render** | Optional — only for Phase 14 | Spring Boot hosting | Yes — login with GitHub at render.com |
| 5 | **Vercel** | Optional — only for Phase 14 | Angular hosting | Yes — login with GitHub at vercel.com |

> **Neon-only mode:** This plan does NOT install PostgreSQL locally. All development uses Neon's free cloud Postgres. Trade-off: you need internet to code, and you'll see ~500 ms cold starts after the DB auto-suspends. Use a Neon **`dev` branch** for development and the **`main` branch** for production deployment — branching is free and instant on Neon.

### 0.2 Tools to install — with alternatives

For every tool, the **first option is the recommendation** (most beginner-friendly, free, no account hassle on Windows).

#### A. Java Development Kit (JDK 21 LTS)
| Option | Vendor | Why pick it | Download |
|--------|--------|-------------|----------|
| **Eclipse Temurin 21 (Recommended)** | Adoptium | Free, no account, OpenJDK build trusted by industry | adoptium.net |
| Amazon Corretto 21 | Amazon | Free, long-term support, no account | aws.amazon.com/corretto |
| Microsoft Build of OpenJDK 21 | Microsoft | Free, integrates well with VS Code | microsoft.com/openjdk |
| Oracle JDK 21 | Oracle | Official, but commercial use needs a license | oracle.com/java |

**Setup steps (Temurin):**
1. Download the `.msi` for Windows x64 at adoptium.net
2. Run installer → check "Set JAVA_HOME variable" and "Add to PATH"
3. Verify in PowerShell: `java -version` → should show `openjdk version "21..."`
4. Verify: `javac -version`

#### B. Node.js + npm (for Angular)
| Option | Why pick it | Download |
|--------|-------------|----------|
| **Node.js current LTS — official installer (Recommended)** | Simplest, includes npm. Click the "LTS" button — currently Node 22 / 24 | nodejs.org |
| nvm-windows | Lets you switch Node versions per project | github.com/coreybutler/nvm-windows |
| fnm | Fast Node version manager, cross-platform | github.com/Schniz/fnm |

> **Don't use Node 20** — it reached end-of-life in April 2026. Always grab whatever version nodejs.org currently labels "LTS / Recommended For Most Users."

**Setup steps (official installer):**
1. Go to nodejs.org → click the **LTS** button → download the Windows `.msi`
2. Install with default options (includes npm)
3. Verify: `node -v` → `v22.x.x` or `v24.x.x`, `npm -v` → `10.x.x` or `11.x.x`

#### C. Angular CLI
Once Node is installed, run in PowerShell:
```
npm install -g @angular/cli@latest
ng version
```
No alternative needed — Angular CLI is the official tool.

#### D. Neon (cloud PostgreSQL — replaces local install)

In Neon-only mode, you don't install PostgreSQL on your laptop. You use Neon's free cloud database for both development and production. Here's how to set it up:

**Setup steps (Neon):**
1. Go to **neon.tech** → click **Sign Up** → log in with **GitHub** (no card)
2. Create a new project:
   - **Project name:** `loadtrack`
   - **Postgres version:** 16 (default)
   - **Region:** choose the one closest to you (e.g. `AWS / US East (Ohio)` or `AWS / EU Central (Frankfurt)`)
   - Click **Create project**
3. Neon shows your connection details. Copy the **connection string** — it looks like:
   ```
   postgresql://<user>:<password>@ep-xxxxx-xxxxx.us-east-2.aws.neon.tech/neondb?sslmode=require
   ```
4. Rename the default database `neondb` → `loadtrack` for clarity:
   - Go to **Branches → main → Databases** → click `neondb` → rename to `loadtrack`
   - Or just keep `neondb` and use that name in your config — your call
5. **Create a `dev` branch** (so dev and prod don't share data):
   - Go to **Branches** → click **Create branch** → name it `dev` → parent: `main` → Create
   - Click into the `dev` branch → copy ITS connection string (different host than `main`)
   - You'll use the **`dev` branch URL during development**, and the **`main` branch URL when deploying** to Render in Phase 14
6. Save both connection strings somewhere safe (e.g. a `.env` file you DON'T commit) — you'll need them in Phase 1 when configuring `application.properties`

> **Why two branches?** Branches in Neon are free, instant copy-on-write copies. Keeping `dev` separate from `main` means you can wreck/reset your dev DB anytime without touching the data you'll demo from `main` in production.

#### D-alt. (Skipped — local install)

You're in Neon-only mode, so there is no local PostgreSQL install. If you change your mind later, see the previous plan revision for local install steps via `enterprisedb.com/downloads/postgres-postgresql-downloads`.

#### E. Database GUI client
| Option | Why pick it | Download |
|--------|-------------|----------|
| **DBeaver Community (Recommended)** | Free, supports PostgreSQL + many other DBs, lightweight | dbeaver.io |
| pgAdmin 4 | Official PostgreSQL GUI, bundled with the installer above | already installed |
| HeidiSQL | Lightweight, Windows-native, Postgres support | heidisql.com |
| VS Code PostgreSQL extension (cweijan) | Run queries inside VS Code | VS Code marketplace |

**DBeaver setup (pointed at Neon `dev` branch):**
1. Download DBeaver Community → install with defaults
2. Open → New Connection → **PostgreSQL**
3. Fill the fields by parsing your Neon `dev` connection string `postgresql://<user>:<password>@<host>/<dbname>?sslmode=require`:
   - **Host:** `ep-xxxxx-xxxxx.us-east-2.aws.neon.tech` (the `<host>` part)
   - **Port:** `5432`
   - **Database:** `loadtrack` (or `neondb` if you didn't rename)
   - **Username:** the `<user>` part
   - **Password:** the `<password>` part
4. **Driver properties tab** → add `sslmode=require` (Neon enforces SSL)
5. Click **Test Connection** → Finish
6. You should see your Neon database in the navigator (empty tables — that's expected)

> Neon also has a built-in SQL Editor in its dashboard if you don't want to install DBeaver. DBeaver is still recommended for richer query/schema work and ER-diagram generation.

#### F. Build tool (Maven)
| Option | Why pick it | Notes |
|--------|-------------|-------|
| **Maven Wrapper (mvnw) bundled by Spring Initializr (Recommended)** | Zero install — committed in repo | Auto-downloads correct Maven version |
| Standalone Maven 3.9 | System-wide install | maven.apache.org |
| Gradle | Alternative build tool | We'll stick with Maven per spec |

> The Spring Initializr-generated project ships with `mvnw.cmd` — you don't need to install Maven separately.

#### G. API testing client
| Option | Why pick it | Download |
|--------|-------------|----------|
| **Thunder Client (Recommended)** | VS Code extension, no separate app, no account | VS Code marketplace |
| Bruno | Open-source, no account, files stored locally in repo | usebruno.com |
| Hoppscotch | Web-based, no account | hoppscotch.io |
| Postman | Industry standard but signup required | postman.com |

#### H. VS Code + extensions
1. Install VS Code from `code.visualstudio.com`
2. Install these extensions:
   - **Extension Pack for Java** (Microsoft) — bundles Java debugger, Maven, test runner
   - **Spring Boot Extension Pack** (VMware)
   - **Angular Language Service** (Angular)
   - **Angular Snippets** (John Papa)
   - **ESLint**
   - **Prettier**
   - **Material Icon Theme** (optional, nicer file icons)
   - **Thunder Client** (API testing)
   - **PostgreSQL** (cweijan) — query Postgres from VS Code
   - **GitLens**

#### I. Git
| Option | Notes |
|--------|-------|
| **Git for Windows (Recommended)** | Official, includes Git Bash | git-scm.com |
| GitHub Desktop | GUI on top of Git | desktop.github.com |

After install: `git config --global user.name "Your Name"` and `git config --global user.email "you@example.com"`.

### 0.3 Verification checklist (do not proceed until all pass)
```
java -version          # OpenJDK 21
javac -version         # 21.x
node -v                # v22.x or v24.x (current LTS)
npm -v                 # 10.x or 11.x
ng version             # Angular CLI 17+ / 18+
git --version
code --version
```
Plus: open DBeaver, connect to your **Neon `dev` branch** URL, confirm you can reach the database (it'll be empty until Phase 2).

> No `psql --version` check — there's no local Postgres install in Neon-only mode.

---

## Phase 1 — Project Scaffolding & Repo Layout

**Goal:** Two empty-but-runnable projects (Angular shell + Spring Boot shell) committed to git. No business code yet.

### 1.1 Folder structure
```
c:/LoadTrack/LoadTrack/
├── backend/                      # Spring Boot project
├── frontend/                     # Angular project
├── docs/
│   ├── ER-diagram.png
│   ├── api-contracts.md
│   └── schema.sql
├── .gitignore
└── README.md
```

### 1.2 Backend scaffold (via Spring Initializr)
At `start.spring.io`, generate with:
- Project: **Maven**, Language: **Java**, Spring Boot: **3.3.x**
- Group: `com.loadtrack` · Artifact: `backend` · Java: **21**
- **Dependencies to add:** Spring Web, Spring Security, Spring Data JPA, **PostgreSQL Driver**, Lombok, Validation, Spring Boot DevTools

Unzip into `backend/`. Run `./mvnw spring-boot:run` — should start on port 8080 and respond with the default Spring Security login (we'll replace this in Phase 3).

**Initial `application.properties` (Neon `dev` branch — for development):**
```properties
spring.datasource.url=jdbc:postgresql://ep-withered-smoke-aomxq9sk-pooler.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
spring.datasource.username=neondb_owner
spring.datasource.password=<paste-from-neon-dashboard-DO-NOT-COMMIT>
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
jwt.secret=change-me-to-a-32-char-random-string
jwt.expiration-ms=86400000
```

> **Use the pooler host (with `-pooler` in the name).** Neon's newer projects only provision DNS for the pooler endpoint — stripping `-pooler` gives an "Unknown host" error. The pooler works fine with Spring Boot's HikariCP.
>
> **Branch usage:**
> - **`dev` branch host** (`ep-withered-smoke-aomxq9sk-pooler...`) → used for local development (Phases 1–13).
> - **`production` branch host** (`ep-polished-sky-ao6icrsg-pooler...`) → used for Phase 14 production deployment to Render. (Neon names the default branch `production`, not `main`.)

> **CRITICAL — protect the password.** The host and username above are safe to commit (anyone seeing them still cannot connect). The **password must never** be in this file or any committed file. Pick one of these patterns:
>
> **Pattern A (simple):** Paste the real password into `application.properties`, then add `application.properties` to `.gitignore`. Commit a `application.properties.example` template instead (with the placeholder).
>
> **Pattern B (cleaner):** Use environment variables. Replace the `password` line with `spring.datasource.password=${DB_PASSWORD}` and set `DB_PASSWORD` in your VS Code `launch.json` or the OS environment. Then `application.properties` itself is safe to commit.
>
> **Either way: before your first commit, double-check `git status` does NOT include any file containing your real Neon password.**

**Backend package structure to create:**
```
backend/src/main/java/com/loadtrack/
├── BackendApplication.java
├── config/                       # SecurityConfig, CorsConfig, JwtConfig
├── controller/                   # REST controllers
├── service/                      # Business logic
├── repository/                   # Spring Data JPA repos
├── entity/                       # JPA @Entity classes
├── dto/                          # Request / response DTOs
├── security/                     # JwtFilter, JwtUtil, UserDetailsServiceImpl
├── exception/                    # GlobalExceptionHandler, custom exceptions
└── util/                         # PdfUtil, ExcelUtil, InterestCalculator
```

### 1.3 Frontend scaffold
```
ng new frontend --routing --style=scss --standalone=false --skip-git
cd frontend
ng add @angular/material        # Pick Indigo/Pink + Material typography + animations
npm install jwt-decode
```

**Angular structure to create under `src/app/`:**
```
core/
  guards/        auth.guard.ts, role.guard.ts
  interceptors/  jwt.interceptor.ts, error.interceptor.ts
  services/      auth.service.ts (and one service per module later)
  models/        user.model.ts, truck.model.ts, ...
shared/
  components/    confirm-dialog, toast, layout-shell (sidebar+navbar)
  pipes/
features/
  auth/          login.component, login.module
  trucks/
  drivers/
  dealers/
  sand-types/
  trips/
  payments/
  receipts/
  settings/
  dashboard/
  reports/
app-routing.module.ts
app.module.ts
```

### 1.4 Initial commit
- Add a root `.gitignore` covering `node_modules/`, `target/`, `*.iml`, `.idea/`, `.vscode/settings.json`, `.env`, `.mvn/wrapper/maven-wrapper.jar` etc.
- Commit: `chore: scaffold backend (Spring Boot) and frontend (Angular) projects`

---

## Phase 2 — Database Schema & ER Diagram

**Goal:** A normalized PostgreSQL schema, drawn as an ER diagram, and a `schema.sql` checked into `docs/`.

### 2.1 Tables (10)
1. `roles` — `id`, `name` (ADMIN / DRIVER / DEALER)
2. `users` — `id`, `username`, `password` (BCrypt), `role_id` (FK), `status`, `linked_driver_id` (nullable FK), `linked_dealer_id` (nullable FK)
3. `trucks` — `id`, `truck_number` (unique), `model`, `capacity_tons`, `insurance_number`, `rc_number`, `status` (AVAILABLE/ON_TRIP/MAINTENANCE)
4. `drivers` — `id`, `name`, `phone`, `license_number`, `address`, `salary_per_trip`, `assigned_truck_id` (nullable FK to trucks)
5. `dealers` — `id`, `name`, `phone`, `address`
6. `sand_types` — `id`, `name`, `price_per_ton`
7. `trips` — `id`, `truck_id` (FK), `driver_id` (FK), `dealer_id` (FK), `sand_type_id` (FK), `tons`, `source_location`, `destination_location`, `trip_date`, `rate_per_ton` (snapshotted at creation), `total_amount`, `status` (PENDING/STARTED/COMPLETED)
8. `payments` — `id`, `trip_id` (FK, unique), `original_amount`, `interest_amount`, `final_amount`, `payment_status` (PAID/PENDING/PARTIAL), `paid_amount`, `payment_date`, `due_date`
9. `receipts` — `id`, `payment_id` (FK), `receipt_number` (unique), `generated_at`, `pdf_path`
10. `settings` — `id` (singleton row), `interest_rate_percent`, `allowed_days`

### 2.2 Key design decisions (call out in `docs/schema.sql`)
- **Use `BIGSERIAL` (or `GENERATED ALWAYS AS IDENTITY`) for `id` columns** — PostgreSQL's auto-increment.
- **Use `TIMESTAMP` for date+time, `DATE` for date-only** (`trip_date`, `due_date`) — Postgres has stricter types than MySQL.
- **Snapshot `rate_per_ton` on the trip row** so price changes in `sand_types` don't retroactively change historical trip totals.
- `total_amount` is computed and stored (not derived at read time) so reports can SUM efficiently — use `NUMERIC(12,2)` for money.
- `users` has nullable FKs to `drivers` / `dealers` to link a login to a profile (cleaner than two separate tables).
- `payments.due_date = trip.trip_date + settings.allowed_days` — computed when the payment row is created.
- `settings` is a single-row table — interest config is global, not per-dealer.
- For enum-like columns (`status`, `payment_status`, `role.name`) use `VARCHAR` with a `CHECK` constraint — easier than Postgres `ENUM` types when evolving.

### 2.3 Deliverables
- `docs/schema.sql` — `CREATE TABLE` statements + seed for `roles` and one `settings` row + 3 default `sand_types` (Fine/Medium/Rough).
- `docs/ER-diagram.png` — drawn in DBeaver (`Database → ER Diagram`) or dbdiagram.io.
- Apply schema to Neon `dev` branch: in DBeaver, right-click the connection → SQL Editor → paste `docs/schema.sql` contents → Execute. **Or** use Neon's built-in **SQL Editor** in the dashboard.

---

## Phase 3 — Vertical Slice 1: Authentication (login → JWT → protected route)

**Goal:** A user can log in from the Angular UI, receive a JWT, and hit a protected backend endpoint that returns 401 without a token.

### 3.1 Backend
- `entity/`: `User`, `Role`
- `repository/`: `UserRepository`, `RoleRepository`
- `security/`: `JwtUtil` (generate + validate, HS256, configurable secret in `application.properties`), `JwtAuthFilter` (extends `OncePerRequestFilter`), `UserDetailsServiceImpl`
- `config/SecurityConfig`: stateless session, `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`, `BCryptPasswordEncoder`, public `POST /api/auth/login`, all others authenticated
- `controller/AuthController`: `POST /api/auth/login` → `{username, password}` → returns `{token, role, username, userId}`
- `exception/GlobalExceptionHandler` with `@RestControllerAdvice` — bad creds → 401, validation → 400
- Seed an admin user via a `CommandLineRunner` (username `admin`, password `admin123`) **only if no users exist**

### 3.2 Frontend
- `features/auth/login.component.ts` — Material `mat-form-field` form, calls `AuthService.login()`
- `core/services/auth.service.ts` — `login()`, `logout()`, `getToken()`, `getRole()`, `isLoggedIn()`; persists token in `localStorage`
- `core/interceptors/jwt.interceptor.ts` — adds `Authorization: Bearer <token>` to outgoing requests
- `core/interceptors/error.interceptor.ts` — on 401, clears token and routes to `/login`
- `core/guards/auth.guard.ts` and `role.guard.ts` (checks `data.roles` on the route)
- Routing: `/login` (public), `/admin/**` (auth + role=ADMIN), `/driver/**` (auth + role=DRIVER), `/dealer/**` (auth + role=DEALER)

### 3.3 Verification
- Neon is always available (no local service to start) → `./mvnw spring-boot:run` → `ng serve`
- Browser → `http://localhost:4200/login` → log in as `admin/admin123`
- DevTools Network → see `Authorization: Bearer ...` header on next request
- Manually call `GET /api/trucks` in Thunder Client without token → 401; with token → 200 (empty list, since Phase 4 not done)
- Log out → token cleared, redirected to `/login`

---

## Phase 4 — Vertical Slice 2: Truck Management (CRUD)

**Goal:** Admin logs in → sees Truck list → adds/edits/deletes a truck → list refreshes.

### Backend
- `entity/Truck`, `repository/TruckRepository`, `service/TruckService`, `controller/TruckController`
- `dto/TruckRequest`, `dto/TruckResponse`
- Endpoints (all `ADMIN` only): `GET /api/trucks` (paginated), `GET /api/trucks/{id}`, `POST /api/trucks`, `PUT /api/trucks/{id}`, `DELETE /api/trucks/{id}`
- Validation: `truck_number` unique (DB constraint + service-layer pre-check), capacity > 0, `@NotBlank` on text fields
- Custom exceptions: `DuplicateResourceException`, `ResourceNotFoundException` mapped in `GlobalExceptionHandler`

### Frontend
- `features/trucks/`: `truck-list.component`, `truck-form.component` (used in a `MatDialog` for both add/edit), `truck.service.ts`, `truck.model.ts`
- Material table with `mat-paginator` + `mat-sort` + search input
- Confirmation dialog (reusable from `shared/`) before delete
- Snackbar notifications for success/error

**Pattern set here is reused for Drivers, Dealers, Sand Types, Settings.** From this phase onward, each module is the same template with different fields.

---

## Phase 5 — Vertical Slice 3: Driver Management (CRUD)

Same shape as Phase 4. Adds:
- Foreign-key dropdown for `assigned_truck` (fetched from `/api/trucks?status=AVAILABLE`)
- Phone number regex validation
- License number validation (state-specific format optional)

---

## Phase 6 — Vertical Slice 4: Dealer Management (CRUD)

Same template. Simpler — no FKs.

---

## Phase 7 — Vertical Slice 5: Sand Types (CRUD) + Settings (singleton)

- Sand Types: simple CRUD (Fine / Medium / Rough seeded; admin can change `price_per_ton`).
- Settings: a single-row form (`interest_rate_percent`, `allowed_days`). Backend uses `findFirstBy...` and treats POST as upsert.

---

## Phase 8 — Vertical Slice 6: Trip Management (the core)

**This is the largest single phase.** It depends on Phases 4–7.

### Backend
- `entity/Trip` with FKs to Truck, Driver, Dealer, SandType
- On `POST /api/trips`:
  - Validate truck status = AVAILABLE; flip to ON_TRIP
  - Snapshot `rate_per_ton` from the chosen sand type
  - Compute `total_amount = tons * rate_per_ton`
  - Auto-create a `payments` row with status=PENDING, `due_date = trip_date + settings.allowed_days`
- `PUT /api/trips/{id}/status` — transitions PENDING → STARTED → COMPLETED; on COMPLETED, flip truck back to AVAILABLE
- Endpoints: full CRUD plus `GET /api/trips?driverId=&dealerId=&from=&to=&status=`

### Frontend
- Trip create form: cascading dropdowns (Truck → Driver auto-suggested if assigned, Dealer, Sand Type)
- **Live calculation** — total_amount field is `[disabled]` and updates via `valueChanges` on the `tons` and `sandType` controls
- Trip list with filter chips (by status / date range / driver / dealer)
- Status-change action buttons inline in the list

---

## Phase 9 — Vertical Slice 7: Payments + Interest Logic

### Backend
- `service/PaymentService.applyInterestIfOverdue(payment)` — pure function:
  - If `payment_date == null && today > due_date`: `interest = original * (rate/100)`, `final = original + interest`
  - Else: `final = original`
- Called on every read of payment data so dealers always see the *current* delayed amount.
- `POST /api/payments/{id}/pay` body `{paidAmount}` → updates status (PARTIAL if `< final`, PAID if `>= final`), sets `payment_date`.
- `GET /api/payments?status=PENDING&dealerId=` — for the dealer portal and admin pending-payments view.

### Frontend
- Payment list under Admin (filter by dealer, by status)
- "Mark as Paid" action → opens dialog → enter amount → posts to backend
- Pending dues badge on dashboard

---

## Phase 10 — Vertical Slice 8: Receipt Generation (PDF)

- Backend `util/PdfUtil` using **iText 7** — renders a receipt template (header logo placeholder, dealer block, trip block, payment block with original/interest/final, footer)
- `POST /api/receipts/generate/{paymentId}` → creates receipt row, **streams PDF as `ByteArrayOutputStream` in the response** (do NOT write to disk — won't survive Render's ephemeral filesystem)
- `GET /api/receipts/{id}/download` — re-generates and streams the PDF on demand (idempotent — receipt data is stored, PDF is regenerated)
- Frontend: "Generate Receipt" button on a paid payment → triggers download via `Blob` + `URL.createObjectURL`

---

## Phase 11 — Driver & Dealer Portals (read-only views)

Both reuse existing endpoints with role-scoped filters baked in at the service layer:
- `GET /api/driver/my-trips` — JWT principal → driver_id → trips where `driver_id = me`
- `GET /api/driver/my-salary` — sum of `salary_per_trip` over completed trips this month
- `GET /api/dealer/my-payments` — payments where `trip.dealer_id = me`
- `GET /api/dealer/my-receipts`

Frontend: dedicated dashboards under `/driver` and `/dealer` route trees, sidebar tailored per role.

---

## Phase 12 — Admin Dashboard + Reports + Excel Export

### Dashboard
- 6 summary cards (Trucks, Drivers, Trips, Pending Payments, Monthly Earnings, Active Trips) — single endpoint `GET /api/dashboard/admin/summary`
- Charts: monthly earnings line chart + trips-by-status pie chart using `ng2-charts` (Chart.js wrapper) — alternative: ApexCharts

### Reports
- 5 report endpoints with date-range params
- Export each via `?format=pdf` (iText) or `?format=xlsx` (Apache POI `XSSFWorkbook`)
- `util/ExcelUtil` — generic helper that takes a list + column-mapper

---

## Phase 13 — Cross-Cutting Polish

- Global error handling: `GlobalExceptionHandler` covers `MethodArgumentNotValidException`, `DataIntegrityViolationException`, custom exceptions
- CORS config (`config/CorsConfig`) — allow `http://localhost:4200` in dev, configurable in prod
- API docs: add `springdoc-openapi-starter-webmvc-ui` dependency → Swagger UI at `/swagger-ui.html`
- Request logging filter for debugging
- Frontend loading spinner via an HTTP interceptor + `MatProgressBar`
- Form-level validation messages standardized

---

## Phase 14 — Deployment (Vercel + Render + Neon)

> **Critical fact:** Vercel does **not** run Java / Spring Boot. Vercel only serves static frontends + Node serverless functions. So our deployment is split across **three free services**:
>
> 1. **Vercel** → Angular frontend
> 2. **Render** (or Koyeb) → Spring Boot backend
> 3. **Neon** → PostgreSQL database

All three are free, none require a credit card.

### 14.1 Database: Neon (already provisioned in Phase 0)

In Neon-only mode, your Neon project is already set up from Phase 0 — no new signup needed here. The only deployment-time action: **switch from the `dev` branch to the `main` branch for production**.

1. In the Neon dashboard, go to **Branches → main** → copy the connection string for `main`
2. Convert to JDBC format: prepend `jdbc:` and strip the `<user>:<password>@` part:
   ```
   jdbc:postgresql://ep-yyy-yyy.us-east-2.aws.neon.tech/loadtrack?sslmode=require
   ```
3. Apply your final `docs/schema.sql` against the `main` branch (DBeaver or Neon SQL Editor) — the `dev` branch already has it from Phase 2
4. Save the `main` branch's JDBC URL + username + password — these go into Render env vars in step 14.2

> **Tip:** Keep `dev` for ongoing work after deployment. Promoting schema changes from `dev` → `main` is a manual step (or use Neon's branching CLI if you want it automated).

### 14.2 Backend: Render Web Service (free tier)

1. Push the repo to GitHub (`backend/` and `frontend/` as siblings — monorepo is fine)
2. In `backend/`, create [backend/src/main/resources/application-prod.properties](backend/src/main/resources/application-prod.properties):
   ```properties
   spring.datasource.url=${DB_URL}
   spring.datasource.username=${DB_USER}
   spring.datasource.password=${DB_PASSWORD}
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false
   server.port=${PORT:8080}
   jwt.secret=${JWT_SECRET}
   cors.allowed-origins=${FRONTEND_URL}
   ```
3. Add a [backend/Dockerfile](backend/Dockerfile) (Render auto-detects Java but Dockerfile is more reliable):
   ```dockerfile
   FROM eclipse-temurin:21-jdk AS build
   WORKDIR /app
   COPY . .
   RUN ./mvnw clean package -DskipTests
   FROM eclipse-temurin:21-jre
   COPY --from=build /app/target/*.jar /app/app.jar
   EXPOSE 8080
   ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=prod"]
   ```
4. On Render → **New → Web Service** → connect GitHub repo → root directory `backend/` → runtime: Docker → free plan
5. Add environment variables in Render dashboard:
   - `DB_URL` = `jdbc:postgresql://ep-xxx-xxx.us-east-2.aws.neon.tech/loadtrack?sslmode=require`
   - `DB_USER` = (Neon username)
   - `DB_PASSWORD` = (Neon password)
   - `JWT_SECRET` = a 32-char random string
   - `FRONTEND_URL` = (fill after Vercel deploy in 14.3)
6. Deploy → Render gives you a URL like `https://loadtrack-backend.onrender.com`
7. **Cold-start caveat:** free Render web services spin down after 15 min idle and take ~30 s to wake. Hit a `/api/health` endpoint to wake it before demos.

> Alternative: **Koyeb** has an always-on free instance (no spin-down) with similar setup.

### 14.3 Frontend: Vercel (Angular)

1. In the Angular project, set the production API base URL via [frontend/src/environments/environment.prod.ts](frontend/src/environments/environment.prod.ts):
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://loadtrack-backend.onrender.com/api'  // your Render URL
   };
   ```
2. Sign up at `vercel.com` with GitHub login (no card)
3. **Import Project** → select your GitHub repo
4. Configure:
   - **Root Directory:** `frontend`
   - **Framework Preset:** `Angular`
   - **Build Command:** `npm run build -- --configuration production`
   - **Output Directory:** `dist/frontend/browser` (Angular 17+) or `dist/frontend` (older)
5. Deploy → Vercel gives you `https://loadtrack-<random>.vercel.app`
6. Add a [frontend/vercel.json](frontend/vercel.json) for Angular SPA routing (so deep links don't 404):
   ```json
   {
     "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
   }
   ```
7. **Go back to Render** and set `FRONTEND_URL=https://loadtrack-<random>.vercel.app` so CORS allows requests
8. Trigger a Render redeploy so the new CORS env var takes effect

### 14.4 End-to-end smoke test

- Open the Vercel URL
- Log in as `admin` / `admin123`
- Verify a request goes to the Render URL (DevTools → Network tab)
- Verify data persists across reloads (it's hitting Neon)
- Generate a receipt PDF — confirm download works

### 14.5 Things that often break in this free-tier topology

| Symptom | Likely cause |
|---------|-------------|
| Frontend loads but login hangs / fails with CORS error | `cors.allowed-origins` on backend doesn't match the actual Vercel URL exactly (check protocol + trailing slash) |
| First request after idle takes 30+ seconds | Render free-tier cold start — normal; consider Koyeb if it bothers you |
| Spring Boot fails to start on Render with "FATAL: SSL connection required" | Neon enforces SSL — make sure your `DB_URL` ends with `?sslmode=require` |
| First DB query takes ~500 ms after Neon was idle | Neon auto-suspended; it wakes on next query — normal, no fix needed |
| 404 on page refresh in deployed frontend | Missing `vercel.json` SPA rewrite rule |
| PDFs fail to generate in production | iText was writing to a relative folder — switch to in-memory `ByteArrayOutputStream` + return as response stream, don't write to disk on Render (already covered in Phase 10) |

---

## Critical files to be created

**Backend** ([backend/src/main/java/com/loadtrack/](backend/src/main/java/com/loadtrack/)):
- [BackendApplication.java](backend/src/main/java/com/loadtrack/BackendApplication.java)
- [config/SecurityConfig.java](backend/src/main/java/com/loadtrack/config/SecurityConfig.java) — JWT filter chain, BCrypt, role-based authorization
- [security/JwtUtil.java](backend/src/main/java/com/loadtrack/security/JwtUtil.java), [security/JwtAuthFilter.java](backend/src/main/java/com/loadtrack/security/JwtAuthFilter.java)
- [exception/GlobalExceptionHandler.java](backend/src/main/java/com/loadtrack/exception/GlobalExceptionHandler.java)
- One `Entity + Repository + Service + Controller + DTO` quintet per module (Truck, Driver, Dealer, SandType, Trip, Payment, Receipt, Settings, User)
- [util/PdfUtil.java](backend/src/main/java/com/loadtrack/util/PdfUtil.java), [util/ExcelUtil.java](backend/src/main/java/com/loadtrack/util/ExcelUtil.java), [util/InterestCalculator.java](backend/src/main/java/com/loadtrack/util/InterestCalculator.java)
- [src/main/resources/application.properties](backend/src/main/resources/application.properties) — local Postgres URL, JWT secret, JWT expiry, server port
- [src/main/resources/application-prod.properties](backend/src/main/resources/application-prod.properties) — env-driven Neon config for Render

**Frontend** ([frontend/src/app/](frontend/src/app/)):
- [core/services/auth.service.ts](frontend/src/app/core/services/auth.service.ts) and one service per feature
- [core/interceptors/jwt.interceptor.ts](frontend/src/app/core/interceptors/jwt.interceptor.ts)
- [core/guards/auth.guard.ts](frontend/src/app/core/guards/auth.guard.ts), [core/guards/role.guard.ts](frontend/src/app/core/guards/role.guard.ts)
- [shared/components/layout-shell/](frontend/src/app/shared/components/layout-shell/) — sidebar + navbar
- One `feature/<module>/` folder per module with list + form components
- [app-routing.module.ts](frontend/src/app/app-routing.module.ts) — lazy-loaded feature modules with role guards

**Docs:**
- [docs/schema.sql](docs/schema.sql)
- [docs/ER-diagram.png](docs/ER-diagram.png)
- [docs/api-contracts.md](docs/api-contracts.md) — request/response shapes per endpoint

---

## Verification (per-phase end-to-end checks)

Each phase ends with a manual run-through. Don't move to the next phase until:

1. **Phase 0:** all 8 CLI version checks pass; DBeaver can connect to local Postgres; `loadtrack` DB exists.
2. **Phase 1:** `./mvnw spring-boot:run` starts on `:8080`; `ng serve` starts on `:4200`; both render their default page.
3. **Phase 2:** Running `psql -U postgres -d loadtrack -f docs/schema.sql` succeeds; all 10 tables visible in DBeaver; ER diagram exported to `docs/`.
4. **Phase 3:** Login → JWT in localStorage → calling a protected endpoint without token returns 401, with token returns 200.
5. **Phases 4–7 (master data):** add/edit/delete works for each module; refresh persists; unique-constraint violations show a friendly toast (not a 500).
6. **Phase 8 (Trip):** creating a trip auto-generates a `payments` row visible via DBeaver; total_amount = tons × rate_per_ton; truck status flips correctly.
7. **Phase 9 (Payment):** mark a payment as paid → status updates; backdating `due_date` to yesterday → list shows interest applied automatically on next refresh.
8. **Phase 10 (Receipt):** generated PDF opens, contains correct dealer/trip/amount; downloads from the browser.
9. **Phase 11 (Portals):** log in as a driver → only see your trips; log in as a dealer → only see your payments.
10. **Phase 12 (Dashboard/Reports):** numbers on dashboard match raw SQL counts; PDF & Excel exports download and open correctly.
11. **Phase 13 (Polish):** Swagger UI lists every endpoint; invalid inputs produce clean error messages.


## Recommended pace

- Phase 0–1: 1 day (mostly downloads + verifying installs)
- Phase 2: half a day (schema + ER diagram)
- Phase 3 (Auth): 2 days — most concept-dense phase
- Phase 4 (first CRUD): 1.5 days — sets template
- Phase 5–7 (next 3 CRUDs): 1 day each (template now established)
- Phase 8 (Trip): 2 days
- Phase 9 (Payment + interest): 1 day
- Phase 10 (Receipt PDF): 1 day
- Phase 11 (Portals): 1 day
- Phase 12 (Dashboard + Reports + Excel): 2 days
- Phase 13 (Polish): 1 day

**Total: ~14–16 working days** for a focused academic build.
