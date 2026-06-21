---
pdf_options:
  format: A4
  margin: 20mm
  printBackground: true
stylesheet:
  - https://cdn.jsdelivr.net/npm/water.css@2/out/water.css
---

# LoadTrack — Interview Questions and Answers

**Purpose:** A complete preparation guide for talking about LoadTrack in technical interviews. Questions are organised from general project overview down to technology-specific deep-dives. Each answer gives a short version first, then a longer, more thorough version you can use if the interviewer asks you to elaborate.

---

## Section A — Opening / "Tell me about this project"

### Q1. Walk me through this project.

**Short answer (30 seconds):**
LoadTrack is a full-stack web application I built to digitise the operations of a small sand-transportation business. It replaces paper-based record keeping with a role-based system where an admin manages trucks, drivers, dealers, trips, and payments; drivers see their own trips and salary; and dealers see their own dues and receipts. It is built with Spring Boot + Angular + PostgreSQL and is deployed live on Vercel, Render and Neon.

**Longer answer (2 minutes):**
The business problem is that small fleet owners typically run their operations on paper diaries and Excel sheets. They lose slips, mis-calculate dues, and have disputes with dealers about overdue interest. LoadTrack solves that by:

1. Giving each user a role-based portal — ADMIN, DRIVER, or DEALER.
2. Automating trip totals, payments, and overdue interest with a clear audit trail.
3. Generating downloadable PDF receipts and Excel reports.
4. Showing the owner a single dashboard with monthly earnings, outstanding dues, and active trips.

Architecturally it is a classic three-tier system. The Angular frontend is a single-page app on Vercel; the Spring Boot REST API runs in a Docker container on Render; data lives in serverless Postgres on Neon. Authentication is stateless via JWT, and authorisation is role-based at both the URL path level (in `SecurityConfig`) and inside services (via `CurrentUserService` for `/me/**` endpoints).

I built it in 14 phases as vertical slices — each phase ships one feature end-to-end before moving on. That kept the project deployable at every stage.

### Q2. Why did you choose this project?

I wanted something with **real business logic**, not just CRUD. LoadTrack has interesting domain rules — interest accruing on overdue payments, atomic trip creation that flips truck status and creates a payment row, role-based portals that filter by JWT principal. Plus I wanted to ship something live so I could point to a URL.

### Q3. How long did it take?

About three weeks of focused work. The plan was 14 phases of 1–2 days each. The trickiest phases were Phase 8 (trip creation with its multi-table transaction) and Phase 14 (deployment).

### Q4. What is the live URL?

- Frontend: https://loadtrack-gamma.vercel.app
- API + Swagger docs: https://loadtrack-backend.onrender.com/swagger-ui.html

The first request after 15 minutes of inactivity takes about 30 seconds because Render's free tier spins down idle containers. Subsequent requests are fast.

### Q5. Who would use this in real life?

Small to medium fleet owners running 1–50 trucks for sand or general aggregate transportation. The owner is the ADMIN; their dispatchers are also ADMINs; the drivers and dealer companies are DRIVERs and DEALERs.

---

## Section B — Architecture and Design

### Q6. Why three tiers? Why not a monolith with server-side rendering?

Three tiers gives **independent scaling** of frontend and backend, and **independent free-tier hosting**. The frontend is just static files behind a CDN, which costs nothing to serve. A monolith would tie the two together. Also, a SPA is the right UX for a dashboard-heavy app — fewer full-page reloads.

### Q7. Why did you pick Spring Boot over Node / Django / .NET?

I wanted **Java in production** on my resume, and Spring Boot is the de facto Java web framework. The ecosystem has best-in-class libraries for everything I needed: Spring Security for auth, Spring Data JPA for the ORM, springdoc for OpenAPI. Build with Maven and ship as a single fat JAR.

### Q8. Why Angular over React?

Three reasons:
1. **Opinionated structure.** Angular forces a consistent module/component/service layout, which keeps a multi-module app like this maintainable.
2. **Dependency injection** built in — same mental model as Spring on the backend.
3. **Angular Material** is mature and accessible. With React I would have spent more time wiring up Tailwind / shadcn equivalents.

### Q9. Walk me through the request lifecycle when a dealer marks a payment.

This is a good integration example. Step by step:

1. **Browser** — DEALER clicks "Pay" on a payment row. Angular's `PaymentService.markPaid(id, amount)` fires `POST /api/payments/{id}/pay`.
2. **Interceptors** — `loadingInterceptor` shows a progress bar; `jwtInterceptor` attaches `Authorization: Bearer <token>`.
3. **Vercel CDN** — passes through to the Render backend over HTTPS.
4. **JwtAuthFilter** — validates the HS256 signature, expiry, and loads `UserDetails`. Sets the `Authentication` on `SecurityContext`.
5. **SecurityConfig** — checks `/api/payments/**` requires `hasRole('ADMIN')`. (Side note: in v1 the dealer cannot mark their own payment as paid; only admin can. Dealer pays in person and admin records it.)
6. **PaymentController** — receives `{paidAmount}`, calls `PaymentService.markAsPaid(id, paidAmount)`.
7. **PaymentService** — inside `@Transactional`:
   - Loads the payment, settings
   - Computes current interest
   - Validates not overpaying
   - Updates the payment row (paidAmount, status, paymentDate)
   - Inserts a new `payment_transactions` row
8. **Hibernate** — flushes the changes; PostgreSQL commits the transaction.
9. **Response** — DTO returned, JSON serialised back through the chain.
10. **Frontend** — `subscribe` callback updates the row, snackbar shows success.

### Q10. How is the code organised on the backend?

Classic Spring layered architecture:

```
controller → service → repository → entity
```

Each layer has a single responsibility. Controllers are thin — they handle HTTP plumbing and delegate to services. Services contain the business logic and manage transactions. Repositories are Spring Data interfaces. Entities are JPA-mapped POJOs. DTOs sit alongside to decouple the wire format from the persistence model.

### Q11. How is the code organised on the frontend?

```
core/      — singletons (auth service, guards, interceptors)
shared/    — reusable across features (layout, dialogs)
features/  — one folder per business module, lazy-loaded
```

Each feature is its own NgModule with its own routing. The router lazy-loads the module the first time you navigate into it, so the initial bundle stays small.

---

## Section C — Database Design

### Q12. How many tables do you have? Walk me through the schema.

Ten tables. Three "lookup-ish" (`roles`, `sand_types`, `settings`) and seven business tables (`users`, `trucks`, `drivers`, `dealers`, `trips`, `payments`, `payment_transactions`, `receipts`, `trip_requests`).

The interesting relationships:

- `users` has nullable FKs to **both** `drivers` and `dealers`. This is the cleanest way to link a login to a person-record without duplicating tables.
- `trips` is the hub — it has FKs to truck, driver, dealer, sand-type and is referenced one-to-one by `payments`.
- `payments` is referenced by `payment_transactions` (installments) and `receipts`.
- `trip_requests` references `dealers` and `sand_types`, and optionally references the resulting `trip` after approval.

### Q13. Why did you snapshot the rate per ton on the trip row?

If a sand price changes next month, historical trip totals must not change retroactively. By copying `pricePerTon` from `sand_types` into `trips.rate_per_ton` at creation time, the trip becomes immutable for billing. This is sometimes called the **frozen-data pattern** for invoicing.

### Q14. Why is `total_amount` stored on the trip if you can compute it from `tons × rate_per_ton`?

Three reasons:

1. **Reports** — `SELECT SUM(total_amount)` is fast and obvious. Computing on the fly is fine for one row but slow for thousands.
2. **Single source of truth** — once written, the number cannot drift from what was originally invoiced.
3. **Defence against bugs** — even if my rounding logic changes, historical totals stay stable.

The cost is duplication, but the duplication is a feature here.

### Q15. Why do you recompute interest on every read?

Because the interest is **live**. A dealer logs in today and expects to see what they owe **today**, not what they owed last time someone ran a job. Storing a stale "interest as of yesterday" creates support tickets.

The compromise: once the payment becomes `PAID`, the interest is snapshotted and the row becomes immutable. So the live recomputation only applies while a payment is open.

### Q16. What's the foreign-key behaviour on deletes?

I use **explicit `ON DELETE` clauses** in `schema.sql`:

- `payment_transactions.payment_id` → `ON DELETE CASCADE` (deleting a payment wipes its installments)
- `users.linked_driver_id` → `ON DELETE SET NULL` (deleting a driver detaches the login but doesn't kill it)
- `drivers.assigned_truck_id` → `ON DELETE SET NULL`
- `trips.*` → `RESTRICT` (cannot delete a trip if it has a payment — by design)

I also added a manual `ALTER TABLE` for `payment_transactions` after the initial table was created — Hibernate's `@OnDelete` annotation doesn't retroactively change an existing table.

### Q17. Why PostgreSQL and not MySQL?

The Neon serverless free tier is PostgreSQL only, and it has the best free-tier story (5 GB, no card, branching). PostgreSQL also has better support for `JSONB`, partial indexes, and `NUMERIC` for money — all features I might want as the project grows. JPA / Spring Data abstracts the differences anyway, so swapping engines later is mostly an env var change.

### Q18. How did you handle migrations?

For now, a hand-written `docs/schema.sql` and `ddl-auto=update` in Hibernate. That's pragmatic for a single-developer project. In a team setting I would use **Flyway** or **Liquibase** with versioned migrations under source control.

---

## Section D — Security

### Q19. How does authentication work?

**Stateless JWT.** On login, the backend issues an HS256-signed token containing the username, role, and userId in the claims. The token is valid for 24 hours. The frontend stores it in `localStorage` and attaches it as `Authorization: Bearer <token>` to every subsequent request via an HTTP interceptor.

On the backend, a custom `JwtAuthFilter` extends `OncePerRequestFilter`, extracts and validates the token, loads the `UserDetails`, and sets the `SecurityContext` for the request. After that, the standard Spring Security filter chain handles authorisation.

### Q20. Why JWT and not session cookies?

Three reasons:

1. **Stateless.** The backend keeps no session memory. It can be scaled horizontally or restarted without logging users out.
2. **SPA-friendly.** The Angular app can read the role from the token claims and adapt the UI accordingly.
3. **Cleaner cross-origin.** With JWT in `Authorization` headers, CORS is straightforward; cookies require `withCredentials` + `SameSite` configuration.

The trade-off is that you cannot revoke a JWT before it expires. For LoadTrack v1, 24-hour validity is acceptable. A v2 hardening would add a token blacklist or shorter expiry + refresh tokens.

### Q21. Where is the JWT stored on the client? Isn't `localStorage` insecure?

It is in `localStorage`. The criticism is that `localStorage` is reachable from JavaScript, so a successful XSS attack can steal the token. The alternative is an **HttpOnly cookie** which JS cannot read.

I chose `localStorage` because:
- The app does not render user-supplied HTML, so the XSS surface is small.
- Cookies require dealing with CSRF, which adds complexity for a SPA.
- This is a portfolio project, not a financial system handling card data.

For a production-grade banking app I would switch to HttpOnly cookies with CSRF tokens.

### Q22. How are passwords stored?

**BCrypt-hashed with a per-password salt.** Spring's `BCryptPasswordEncoder` generates the salt automatically and includes it in the hash output. Verification happens via `passwordEncoder.matches(rawPassword, storedHash)`. The plaintext password never leaves the request.

### Q23. How do you prevent role escalation? What stops a DRIVER from hitting an admin endpoint?

Three layers:

1. **URL-pattern matching in `SecurityConfig`**: `requestMatchers("/api/trucks/**").hasRole("ADMIN")`. A DRIVER token will be rejected with 403 at the filter chain — before reaching the controller.
2. **Path namespace separation**: `/api/me/driver/**` is restricted to DRIVER, `/api/me/dealer/**` to DEALER. No shared paths.
3. **JWT-derived scoping** inside the `/me/**` services: `CurrentUserService.getDriverIdOrThrow()` returns the driver_id from the token. Query parameters cannot override it. So a driver cannot pass `?driverId=999` and see someone else's trips.

### Q24. Could an attacker forge a JWT?

Only by knowing the `JWT_SECRET`. The secret is a 48-character random string injected as an environment variable in production. It never leaves the server. HS256 uses HMAC-SHA256, which is computationally infeasible to forge without the key.

### Q25. How is CORS configured?

`CorsConfig` reads `cors.allowed-origins` (env var `CORS_ALLOWED_ORIGINS` in production) and registers a `CorsConfigurationSource` bean. Only the exact Vercel domain is whitelisted. Methods: GET / POST / PUT / DELETE / PATCH / OPTIONS. Allow-credentials enabled because the frontend may send the Authorization header.

### Q26. What about SQL injection?

All database access is through **JPA / Hibernate**, which uses parameterised queries. No string concatenation builds SQL. JPA Specifications use the `CriteriaBuilder` API which produces parameterised queries too.

### Q27. Have you done a threat model?

The main threats and mitigations:

| Threat | Mitigation |
|--------|-----------|
| Stolen JWT | 24-hour expiry; HTTPS-only; localStorage not exposed across origins |
| Brute-force login | Not addressed in v1; a v2 would add rate-limiting (bucket4j or nginx) |
| XSS | Angular's template binding auto-escapes; no `innerHTML` with user data |
| CSRF | Not applicable — JWT in Authorization header, not cookies |
| SQL injection | Parameterised queries everywhere |
| Sensitive data leakage in logs | SQL logs silenced in prod; passwords never logged |
| Mass assignment | DTOs separate from entities — controllers bind to DTOs, services map to entities |

---

## Section E — Spring Boot / Java specifics

### Q28. What is `@Transactional` and where do you use it?

`@Transactional` defines a transactional boundary. Spring wraps the method in a database transaction so that all changes either commit together or roll back together.

I use it at the **service-method level**, not at the repository level. So `TripService.create` is `@Transactional` and inside it three repositories are called; if any throws, all three changes roll back. Reads are marked `@Transactional(readOnly = true)` for an optimisation hint.

### Q29. What does `@RequiredArgsConstructor` do?

It is a **Lombok** annotation that generates a constructor taking all `final` fields. Combined with `private final` field injection (Spring's recommended style), it eliminates boilerplate. The bean is constructed with its dependencies, fields are final so they cannot be mutated, and unit tests can supply mocks directly.

### Q30. What is a JPA Specification?

A way to build dynamic queries programmatically. The `Specification<T>` interface takes a `Root`, a `CriteriaQuery`, and a `CriteriaBuilder` and returns a `Predicate`. The service composes predicates based on which query params are present:

```java
Specification<Payment> spec = (root, q, cb) -> {
    List<Predicate> p = new ArrayList<>();
    if (dealerId != null) p.add(cb.equal(root.get("trip").get("dealer").get("id"), dealerId));
    if (status != null)   p.add(cb.equal(root.get("paymentStatus"), status));
    return cb.and(p.toArray(new Predicate[0]));
};
return paymentRepo.findAll(spec, pageable);
```

The alternative is writing one method per filter combination, which doesn't scale.

### Q31. How are you handling exceptions?

A `@RestControllerAdvice` class called `GlobalExceptionHandler` catches:

- `MethodArgumentNotValidException` → 400 with field-level errors
- `DataIntegrityViolationException` → 409 with a friendly message ("Cannot delete driver — they have existing trips")
- `ResourceNotFoundException` (custom) → 404
- `IllegalArgumentException` and `IllegalStateException` → 400
- `NoResourceFoundException` (Spring's, for unknown URLs) → 404
- Generic `Exception` → 500 with a generic message (and the real one logged)

Controllers don't `try/catch` anything routine — they let exceptions propagate.

### Q32. What is the `@RestControllerAdvice` vs `@ControllerAdvice`?

`@RestControllerAdvice = @ControllerAdvice + @ResponseBody`. So my handler methods return DTOs (like `ErrorResponse`) directly, and Spring serialises them to JSON automatically.

### Q33. How does Spring Security's filter chain work?

A chain of `Filter` objects, each with a chance to process or short-circuit the request. The relevant filters here:

1. **CorsFilter** — handles the preflight OPTIONS
2. **CsrfFilter** — disabled because we are stateless / JWT
3. **JwtAuthFilter** — my custom filter; extends `OncePerRequestFilter` so it runs exactly once per request even on internal forwards
4. **UsernamePasswordAuthenticationFilter** — the position where `addFilterBefore(...)` registers mine, ahead of it
5. **ExceptionTranslationFilter** — converts security exceptions to HTTP responses
6. **AuthorizationFilter** — enforces the `authorizeHttpRequests(...)` rules

Once those pass, Spring MVC's `DispatcherServlet` takes over and routes to controllers.

### Q34. How does Hibernate know which table to use?

Either via the `@Table(name = "...")` annotation or by lowercasing the class name. For my entities I rely on the lowercasing default plus an explicit `@Table` for plural forms. JPA also figures out columns from `@Column` or by lowercasing field names.

### Q35. What is the N+1 problem and where might it bite you?

The N+1 problem: fetching a list of N parent records, then issuing N separate queries to fetch their children. For instance, listing 100 payments and then loading each payment's trip would be 1 + 100 queries.

In LoadTrack the `Payment → Trip → Truck/Driver/Dealer/SandType` chain is the area to watch. My DTOs flatten that chain, and the relationships are eager (`@ManyToOne` default) — which means Hibernate often joins them in one query, but it depends. For production scale I would explicitly add `@EntityGraph` or JPQL fetch joins.

---

## Section F — Angular specifics

### Q36. Why NgModule mode and not standalone components?

Two reasons. First, when I started this project I was more comfortable with NgModules. Second, the project has 17 feature modules and lazy-loading via `loadChildren: () => import(...).then(m => m.SomeModule)` is the most established pattern. Standalone components support lazy-loading too, but the NgModule path is well-trodden.

For a greenfield project today I would probably use standalone components — that is the Angular team's recommended direction.

### Q37. How does lazy loading work?

When the router hits a route with `loadChildren`, it dynamically imports the module's JS chunk. Each feature module is compiled into its own bundle by Webpack / esbuild, so the initial main bundle stays small. The first navigation to `/app/trips` downloads the trips bundle, parses it, and renders.

### Q38. What is a Signal and why do you use it?

A **Signal** is Angular's reactive primitive — a wrapped value with read / set / update APIs that automatically tracks consumers. When the value changes, anything reading it re-renders.

I use Signals for component-local state:

```typescript
loading = signal(true);
data = signal<AdminDashboard | null>(null);

ngOnInit() {
  this.dashboard.adminSummary().subscribe({
    next: d => { this.data.set(d); this.loading.set(false); },
    error: e => { this.loading.set(false); /* show toast */ }
  });
}
```

In the template, `{{ loading() }}` reads the signal. Cleaner than `BehaviorSubject` for simple cases.

### Q39. What are functional HTTP interceptors?

Modern Angular interceptors are plain functions matching `HttpInterceptorFn`:

```typescript
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).token();
  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
```

They are registered globally with `provideHttpClient(withInterceptors([...]))`. Cleaner than the older class-based `HttpInterceptor`.

### Q40. How do guards work?

Guards are functions returning `boolean | UrlTree | Observable<boolean>`. For each route, the router awaits the guards before activating.

```typescript
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn() ? true : router.createUrlTree(['/login']);
};
```

The `roleGuard` additionally checks `route.data.roles` against the current user's role.

### Q41. How did you implement the responsive sidebar?

Two pieces:

1. **CDK BreakpointObserver** observes the `Handset` and `TabletPortrait` breakpoints. The result drives an `isHandset` signal.
2. **Template** binds `[mode]="isHandset() ? 'over' : 'side'"` and `[opened]="!isHandset()"`. On mobile the sidenav slides over content; on desktop it sits beside it. A hamburger button is only rendered when `isHandset()` is true.

Combined with global SCSS media queries at 768px for content padding, table overflow, and dialog width.

### Q42. How are forms validated?

**Reactive Forms** with `FormBuilder`. Each form is a `FormGroup` with validators per `FormControl`. Cross-field validation (e.g. password confirmation matches) is done with a `FormGroup`-level validator. Material's `<mat-error>` reads the form's error state and shows messages.

Backend re-validates the same fields with `jakarta.validation` — never trust the client.

---

## Section G — Deployment and DevOps

### Q43. Walk me through how you deployed this.

Three services, all on free tiers, all wired via env vars and CORS:

1. **Database** — Created a Neon project; production branch is `ap-southeast-1`. Applied `docs/schema.sql` against it using DBeaver. Got the JDBC URL and credentials.
2. **Backend** — Pushed the repo to GitHub. On Render: New Web Service → Docker runtime → root `backend/` → free instance type. Added 8 environment variables (DB connection, JWT secret, JWT expiry, CORS origin, profile). Render builds the Docker image and runs it. After fixing two issues (port binding, missing JWT expiry env var), the service went Live at `loadtrack-backend.onrender.com`.
3. **Frontend** — Updated `environment.production.ts` with the Render URL. On Vercel: Import Project → root `frontend/` → Angular preset → Deploy. Vercel handled the rest. URL: `loadtrack-gamma.vercel.app`.
4. **CORS** — Set `CORS_ALLOWED_ORIGINS` on Render to the Vercel domain. Render auto-redeployed.

End to end, this took most of an afternoon and the issues I hit were instructive — see Q44.

### Q44. What were the deployment gotchas you hit?

Four worth knowing:

1. **Render assigns a dynamic `$PORT`** (10000) — the Spring Boot Dockerfile must read it. The fix was a shell-form `ENTRYPOINT`:
   ```dockerfile
   ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT:-8080}"]
   ```
   Exec form (`["java", "-jar", ...]`) doesn't run a shell, so `$PORT` is never expanded.

2. **`application.properties` is gitignored** because it contains the dev DB password. So in production every Spring property has to come from environment variables. Missed `JWT_EXPIRATION_MS` initially; `JwtUtil`'s `@Value("${jwt.expiration-ms}")` couldn't resolve, and the bean failed to construct. Fix: add the env var.

3. **The CORS env var has to match the property name** — `cors.allowed-origins`, not `frontend.url`. Spring's relaxed binding maps `CORS_ALLOWED_ORIGINS` env var to that property. Setting `FRONTEND_URL` was a no-op.

4. **Hibernate `ddl-auto=validate` was too strict** for the manually-applied `schema.sql` (a missing index made it fail). Switching to `update` is safe — `update` only adds, never drops.

### Q45. Why Docker for the backend?

Two reasons:
1. **Reproducible builds** — the same Dockerfile that runs on my laptop runs on Render. No "works on my machine" surprises.
2. **Pinned JDK version** — `eclipse-temurin:21-jdk` for build, `eclipse-temurin:21-jre` for runtime. Render's native Java buildpacks can lag on versions.

Multi-stage build keeps the runtime image small (~280 MB instead of 1 GB+).

### Q46. How is CI/CD wired up?

There's no bespoke CI script. The setup is:

- `git push origin main` → both Vercel and Render watch the branch
- Vercel runs `npm run build -- --configuration production`, deploys to its CDN
- Render rebuilds the Docker image, pushes to its registry, starts a new container

Builds run in parallel. Both auto-rollback if the build fails. Total deploy time on a typical change: ~3 min for Vercel, ~6 min for Render (image rebuild).

### Q47. What does the free tier cost you?

**Nothing.** All three services have permanent free tiers with no credit card. Limits:

- **Vercel Hobby:** 100 GB bandwidth/month — way more than I need.
- **Render free Web Service:** 512 MB RAM, spins down after 15 min idle.
- **Neon Serverless Free:** 5 GB storage, 50M request units / month.

The trade-off is the Render cold start — about 30 seconds when waking up. For a portfolio project that is acceptable.

### Q48. How would you keep Render warm?

Pin an external uptime monitor (UptimeRobot has a generous free tier) to hit `/swagger-ui.html` every 14 minutes. That keeps the container above the idle threshold without paying for an always-on plan.

### Q49. How would you scale this if traffic grew 100x?

Step by step:

1. Upgrade Render to a paid plan with autoscale, or move to AWS ECS Fargate.
2. Move the database to a paid Neon plan or AWS RDS Postgres with read replicas.
3. Add a Redis cache (managed via Upstash) for `settings`, `sand_types`, and dashboard summary.
4. Put a CDN in front of static assets — already there with Vercel.
5. Add pagination ceilings (max 1000 records per page) and rate limiting on auth endpoints.
6. Move PDF generation off the request thread to a worker queue if PDFs become expensive.

The backend is already stateless, so horizontal scaling is just a matter of spawning more instances behind a load balancer.

---

## Section H — Domain / business-logic deep dives

### Q50. How is overdue interest calculated?

A single line in `PaymentService.computeInterest`:

```java
if (!p.getDueDate().isBefore(today)) return BigDecimal.ZERO;
BigDecimal rate = settings.getInterestRatePercent()
        .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
return p.getOriginalAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
```

It is a flat-percentage one-time charge applied to overdue payments — not compounding. Settings.interestRatePercent and Settings.allowedDays are global. If a payment with original 10,000 is overdue and the rate is 5%, interest is 500.

For v2 I would make this configurable per dealer or per sand type, and support per-day compounding.

### Q51. Why use `BigDecimal` for money?

Because `double` and `float` have rounding errors. The classic example: `0.1 + 0.2 = 0.30000000000000004`. For money you cannot afford that. `BigDecimal` represents exact decimal values. Combined with explicit `RoundingMode.HALF_UP` and `setScale(2, ...)`, every monetary value is to two decimals.

### Q52. Walk me through the trip creation transaction.

A single `@Transactional` method does five things atomically:

1. Validate the chosen truck is `AVAILABLE`; throw if not.
2. Resolve driver, dealer, sand type from their IDs.
3. Snapshot `pricePerTon` from the sand type, compute `totalAmount`, save the new `Trip` row.
4. Save a corresponding `Payment` row with `dueDate = tripDate + settings.allowedDays`, status `PENDING`.
5. Flip the truck status to `ON_TRIP`.

If any step throws, the transaction rolls back. Either the trip exists with everything wired up correctly, or it does not exist at all.

### Q53. How do you generate the PDF receipt?

Using **OpenPDF** — an LGPL fork of iText 5 that is free for commercial use without copyleft contagion. The flow:

1. `ReceiptService.generate(paymentId)` loads the payment + trip + installments.
2. `ReceiptPdfBuilder` constructs a `Document`, adds paragraph and table elements section by section (header → dealer → trip → payment → installments → footer).
3. The document is written into a `ByteArrayOutputStream` — **not to disk**. Render's filesystem is ephemeral.
4. The controller returns the bytes as `ResponseEntity<byte[]>` with `Content-Type: application/pdf` and `Content-Disposition: attachment; filename=...`.

### Q54. Why OpenPDF instead of iText?

iText 7 is AGPL-licensed, which is copyleft and risky for closed-source commercial use. **OpenPDF** is the community-maintained LGPL fork of iText 5 — same API, no licence risk.

### Q55. What's interesting about the trip-request workflow?

Two things:

1. **Atomic approval.** When admin approves a request, three things happen in one transaction: a new `Trip` is created (re-using the same `TripService` logic), a `Payment` is created, the truck flips to `ON_TRIP`, and the `TripRequest` is updated with the resulting trip ID and `APPROVED` status. Either all succeed or none do.
2. **Re-using `TripService.create` logic** — I extracted the core "make a trip" sequence into a method that both manual creation and approval call. This avoids two divergent code paths for the same operation.

### Q56. Why is the AdminUserSeeder profile-scoped?

It is annotated `@Profile("!prod")`. In dev, the seeder creates `admin / admin123` if no users exist, so I can log in immediately after wiping data. In production that would be a glaring security hole — anyone who knew the default credentials would have admin access. By disabling the seeder in prod, the production database starts empty and the first admin signs up at `/signup`, choosing their own password.

---

## Section I — Behavioural / process

### Q57. Walk me through your development process.

I planned the project up-front as 14 phases, each phase being a vertical slice that ships one feature end-to-end. The order:

- 0–2: Tools, scaffolding, schema
- 3: Authentication
- 4–7: Master-data CRUD (trucks, drivers, dealers, sand types, settings)
- 8: Trip management
- 9: Payments + interest
- 10: Receipt PDF
- 11: Driver and dealer portals
- 12a: Dashboard
- 12b: Reports + Excel/PDF export
- 13: Polish (Swagger, loading bar, quiet logs)
- 14: Deployment

Each phase ends with a manual smoke test on `localhost` before I move to the next. The frontend is built second within each phase, after the backend endpoint is ready and Swagger-tested.

### Q58. What went wrong, and what did you learn?

A few:

1. **The first Render deploy failed three times** before going live. First port binding, then missing JWT_EXPIRATION_MS, then Hibernate validation. Each one taught me something about Spring's relaxed binding and Docker's exec vs. shell form.
2. **A `TRUNCATE drivers CASCADE` once wiped my users table** in dev because of a nullable FK from `users` to `drivers`. The seeder recreated the admin, but it was an unpleasant surprise. Now I use targeted `DELETE` statements instead.
3. **The Chart.js library failed in production** with "linear is not a registered scale" because Angular's prod build tree-shakes more aggressively than dev. The fix was `provideCharts(withDefaultRegisterables())` in `HomeModule`.

The bigger lesson: **dev-prod parity matters**. Things that work locally on `ng serve` and `mvn spring-boot:run` are not guaranteed to work on a CDN + container.

### Q59. What would you do differently next time?

1. **Add tests earlier.** I have a Spring Boot starter test on the classpath but no real test suite. For the next project I would TDD the service layer.
2. **Use Flyway from day one.** Hand-managing `schema.sql` and `ddl-auto=update` works for a one-person project; in a team it falls apart.
3. **Pick one auth pattern and stick to it.** I started with JWT in localStorage; for a banking-grade app I would default to HttpOnly cookies and CSRF tokens.
4. **Use standalone components.** Angular has been pushing this direction; NgModules feel heavier in retrospect.

### Q60. What is the next feature you'd build?

A few candidates:

1. **Email notifications** — when a request is approved/rejected, when a payment is overdue. Resend or Mailgun free tier.
2. **Audit log** — every admin write captured to an `audit_events` table via Spring AOP. Useful for disputes.
3. **Bulk import** — CSV or Excel upload to seed trucks, drivers, dealers when onboarding a new fleet.
4. **Mobile app** — Capacitor or Flutter wrapping the SPA, with offline support for drivers in poor-signal areas.

I would prioritise the audit log because it's low-effort and addresses a real business risk.

### Q61. How would you onboard a junior developer to this codebase?

I would point them at three files in order:

1. **`docs/IMPLEMENTATION_PLAN.md`** — the why and what
2. **`backend/src/main/java/com/loadtrack/config/SecurityConfig.java`** — understand the security model
3. **`backend/src/main/java/com/loadtrack/service/TripService.java`** — see a representative transactional service

Then have them read one feature folder end-to-end (e.g. `trucks`) on both backend and frontend. That establishes the layered pattern they will see in every other module.

### Q62. Were there any features you cut?

Yes — every "v2" idea in the FRS:

- Multi-currency
- Per-dealer interest rates
- GPS tracking
- SMS / email
- Two-factor auth
- Audit log
- Bulk import
- Multi-language

The reason for cutting: each was either external-dependency-heavy (email infrastructure) or scope-creep risk (multi-currency touches every monetary calculation).

### Q63. How would you write a unit test for `PaymentService.markAsPaid`?

```java
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepo;
    @Mock PaymentTransactionRepository txRepo;
    @Mock SettingsRepository settingsRepo;
    @InjectMocks PaymentService service;

    @Test
    void markAsPaid_partial_setsStatusPartialAndRecordsTransaction() {
        Payment p = Payment.builder()
            .id(1L).originalAmount(new BigDecimal("10000"))
            .paidAmount(BigDecimal.ZERO).paymentStatus("PENDING")
            .dueDate(LocalDate.now().plusDays(7))
            .build();
        when(paymentRepo.findById(1L)).thenReturn(Optional.of(p));
        when(settingsRepo.findFirstByOrderByIdAsc())
            .thenReturn(Optional.of(Settings.builder()
                .interestRatePercent(BigDecimal.ZERO).allowedDays(7).build()));

        service.markAsPaid(1L, new BigDecimal("5000"));

        assertThat(p.getPaymentStatus()).isEqualTo("PARTIAL");
        assertThat(p.getPaidAmount()).isEqualByComparingTo("5000");
        verify(txRepo).save(argThat(tx -> tx.getAmount().compareTo(new BigDecimal("5000")) == 0));
    }
}
```

I would write similar cases for full payment, overpayment (throws), and overdue with non-zero interest.

---

## Section J — Curveball / "What if" questions

### Q64. What if two admins try to create trips for the same truck at the same time?

Both transactions enter `TripService.create`, both see the truck as `AVAILABLE`, both proceed. The second commit succeeds and the truck row is `ON_TRIP` with two trips referencing it. The current code does not prevent this.

The fix is **pessimistic locking** — `entityManager.find(Truck.class, id, LockModeType.PESSIMISTIC_WRITE)` so the second transaction blocks until the first commits. Or **optimistic locking** with a `@Version` column on `Truck`; the second commit throws and is retried.

I would add optimistic locking — it scales better.

### Q65. What if the interest calculation is wrong?

That's a real risk because it directly affects revenue. Mitigations I have:

1. **`BigDecimal` with explicit `HALF_UP`** — no double-precision drift.
2. **Live computation on every read** — no stale snapshots.
3. **Frozen snapshot at payment time** — once `PAID`, the numbers are locked in the row.

Mitigations I would add for v2:

1. **A regression test suite** covering edge cases (zero rate, zero days, exactly on due date, day after, fractional tons).
2. **An admin-only "preview" endpoint** that returns the formula breakdown for any payment, so disputes can be debugged.

### Q66. What if Render goes down?

The frontend on Vercel is still up but its API calls fail. The Angular app would show error toasts ("Network error — please try again"). Users would see the login page but cannot log in.

For high availability I would:
1. Deploy the backend to a second region (Koyeb has a free always-on tier).
2. Put a DNS-level health check (Cloudflare) routing to whichever is healthy.

But for this project's scope, Render's uptime is good enough.

### Q67. What if Neon goes down?

The backend stays up but every request returns 500 because Hikari cannot get a connection. Realistically I would:
1. Run periodic `pg_dump` backups to S3 (or any cheap blob store).
2. For real production, use Neon's branching to keep a hot standby in a different region.

### Q68. The interviewer says "I don't believe this is your code — explain `JwtAuthFilter` line by line."

(Open the file with them.) The class extends `OncePerRequestFilter` so it runs once per request. Inside `doFilterInternal`:

1. Extract the `Authorization` header. If absent or doesn't start with `Bearer `, skip the filter — let downstream rules decide whether to allow the unauthenticated request.
2. Strip the `Bearer ` prefix to get the raw token.
3. Pass the token to `JwtUtil.isValid` — that calls `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)`. If the signature is invalid or it is expired, an exception is thrown and caught, returning false.
4. If valid, call `JwtUtil.extractUsername` to read the `sub` claim.
5. Use `UserDetailsService.loadUserByUsername` to load the user from the database. This gives us the role authorities.
6. Construct a `UsernamePasswordAuthenticationToken` with the user and authorities, set it on `SecurityContextHolder`. This is what makes the user "logged in" for the rest of the chain.
7. Call `filterChain.doFilter(req, res)` to continue.

### Q69. Why didn't you implement refresh tokens?

Three reasons:
1. **24-hour token validity is fine for v1.** Users log in once a day.
2. **Refresh tokens add complexity** — separate endpoint, separate storage strategy, race conditions on simultaneous refreshes.
3. **For a real app, I would use a short-lived access token (15 min) + a refresh token in an HttpOnly cookie.** That's an explicit v2 hardening, not a v1 must-have.

### Q70. What if I asked you to add a new entity, say "Maintenance Records" for trucks?

Step by step:

1. **Schema** — add a `maintenance_records` table to `docs/schema.sql` with FK to trucks; commit.
2. **Backend** — add `MaintenanceRecord` entity, `MaintenanceRecordRepository` interface, `MaintenanceRecordService`, `MaintenanceRecordController`. Three DTOs (request, response, optional list-item).
3. **SecurityConfig** — add path matcher `requestMatchers("/api/maintenance-records/**").hasRole("ADMIN")`.
4. **Frontend** — generate a new feature folder `features/maintenance-records/` with module, list component, form dialog, service, model. Add a sidebar link in `LayoutShell.allLinks`. Add a lazy-loaded route.
5. **Test** — start dev backend + frontend, smoke test create/list/edit/delete.
6. **Deploy** — push to GitHub; Render and Vercel auto-rebuild.

Total time: 2–3 hours for the basic CRUD because the pattern is established. Adding interesting logic (e.g. "auto-flip truck to `MAINTENANCE` status when a record is open") is another hour.

---

## Final tips for the interview

1. **Open the live URL** in the first 30 seconds — you want the interviewer to see a working app, not just slides.
2. **Have Swagger open** — interviewers love poking endpoints.
3. **Lead with the business problem**, not the tech stack. "Sand fleet operators lose money to manual record-keeping" lands better than "Spring Boot 3.5 with JJWT 0.12.6".
4. **Be honest about trade-offs** — you stored money as `BigDecimal` (good) but kept JWT in `localStorage` (deliberate, with reasoning).
5. **Have one diagram ready** — three-tier topology, or the request lifecycle. Whiteboards turn vague questions into concrete conversations.
6. **Volunteer the gotchas** — interviewers know nothing ever goes smoothly. The port-binding story (Q44) makes you sound real, not rehearsed.
7. **End every answer with a v2 hook** — "...for production I would add X." Shows you know the limits.

Good luck.

---

*End of Interview Q&A — LoadTrack v1.0*
