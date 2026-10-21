# Smart EMI Calculator

Java 21 · Spring Boot 3.3 · Spring MVC + Thymeleaf · Spring Data JPA · Spring Security · MySQL 8 · Flyway · Bootstrap 5 · Chart.js

Calculate EMIs for home, personal, car, education, business and custom loans; view paginated amortization schedules; compare 2-3 offers; save history per user; export PDF/CSV.

> **Status:** the code was generated in an environment without internet/Maven, so it has **not been compiled or test-run yet**. Run `mvn clean verify` first and fix anything it reports.

## Quick start

Prerequisites: JDK 21, Maven 3.9+, MySQL 8.

```sql
CREATE DATABASE smart_emi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'emi_user'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON smart_emi.* TO 'emi_user'@'localhost';
```

```bash
export DB_PASSWORD=change_me        # also DB_HOST, DB_PORT, DB_NAME, DB_USER if different
mvn clean verify                    # unit + integration tests (use in-memory H2, no MySQL needed)
mvn spring-boot:run                 # http://localhost:8080
```

Flyway creates the schema on first start. With the default `dev` profile a demo account is seeded:
`demo@smartemi.local` / `Demo@1234` (3 sample calculations). Set `SPRING_PROFILES_ACTIVE=prod` to disable seeding.

## User interface

- `/calculator` first shows six loan-type boxes (Home, Personal, Car, Education, Business, Custom). Choosing one opens only that loan's form (`/calculator?loanType=HOME`) with sample values and a tip; "Change loan type" goes back.
- After **Calculate EMI** the page shows the EMI, totals, chart, a 7-step **"How this was calculated"** explanation filled with the user's own numbers (`EmiService.explain`), and the paginated schedule. A Guidelines section sits at the bottom.
- Four colour themes (Indigo, Emerald, Sunset, Midnight) switch from the dots in the top bar and are remembered in the browser. Themes are CSS variables in `static/css/app.css`.
- `docs/ui-preview.html` is a standalone static preview of this UI (open it in a browser; it calculates in JavaScript only).

## Architecture

`Controller -> Service -> Repository -> MySQL`, packages under `com.smartemi`:
`config` (security, dev seed), `controller` (page + REST + export), `dto`, `entity`, `repository`, `service` (`EmiService` = all math), `exception` (`@ControllerAdvice` handlers), `security` (UserDetailsService), `validation` (shared limits).

- **Money:** `BigDecimal`; intermediates 30 digits HALF_UP, amounts rounded to 2 decimals HALF_UP; the last installment absorbs rounding so the closing balance is exactly 0.00. Zero interest is handled separately (`P / n`).
- **Schedules are not stored.** They are recomputed on demand from principal/rate/tenure (deterministic, cheap for <= 480 rows). Only the summary is saved.
- **Ownership:** all saved-calculation lookups use `findByIdAndUserId`; other users' records return 404.
- **Passwords:** BCrypt. Sessions: Spring Security session cookie. CSRF is on for pages and saved-calculation endpoints; `/api/emi/**` and `/api/auth/**` are exempt for Postman convenience.
- **Limits:** amount 1,000-1,000,000,000; rate 0-100%; tenure 1-480 months (40 years).

### Page controllers vs REST

| Used by | Operations |
|---|---|
| Page controllers (`PageController`, `ExportController`) - browser | calculator, schedule pagination, compare, register/login (form), dashboard, history, save/open/delete, PDF/CSV download |
| REST (`/api/**`) - Postman / other clients | calculate, schedule, compare, saved calculations CRUD, register/login/logout |

Both call the same services; the pages never call the REST API.

## REST API

Errors: `{"status":400,"error":"Bad Request","message":"Validation failed","fieldErrors":{"loanAmount":"..."},"timestamp":"..."}`

```bash
# Calculate (public)
curl -X POST localhost:8080/api/emi/calculate -H 'Content-Type: application/json' \
  -d '{"loanAmount":100000,"annualRate":12,"tenure":12,"tenureUnit":"MONTHS","loanType":"PERSONAL"}'
# -> 200 {"loanAmount":100000.00,"annualRate":12,"tenureMonths":12,"monthlyEmi":8884.88,"totalInterest":6618.5x,"totalPayment":106618.5x}

# Schedule (public): POST /api/emi/schedule, same body -> 200 [ {installment, date, openingBalance, emi, principalPaid, interestPaid, closingBalance}, ... ]

# Compare (public): POST /api/emi/compare
# {"loans":[{...},{...},{...}]} -> 200 {"loans":[summary...],"lowestEmiIndex":1,"lowestInterestIndex":0,"tenureInsight":"..."}

# Auth
POST /api/auth/register {"fullName":"A","email":"a@x.com","password":"Password1"} -> 201 | 400 | 409
POST /api/auth/login    {"email":"a@x.com","password":"Password1"} -> 200 (+ JSESSIONID cookie) | 401
POST /api/auth/logout   -> 204
```

Saved calculations (login required, `JSESSIONID` cookie; POST/DELETE also need the `X-XSRF`-style CSRF token or use the web UI):

| Method | Path | Result |
|---|---|---|
| POST | `/api/calculations` | 201 + Location, body = saved summary (`loanName` optional) |
| GET | `/api/calculations?type=HOME&from=2026-01-01&to=2026-12-31&page=0&size=10` | 200 paged list |
| GET | `/api/calculations/{id}` | 200, or 404 if missing/not yours |
| DELETE | `/api/calculations/{id}` | 204, or 404 |

## Tests (`mvn test`)

- `EmiServiceTest`: known values - 100,000 @ 12% / 12 months = **8,884.88**; 1,000,000 @ 8.5% / 20 years ~ **8,678**; zero interest 120,000 / 12 = **10,000.00**; 1-month loan = 101,000.00; very low/high rates; 480-month schedule; final installment/rounding; invalid inputs; comparison.
- `LoanCalculationRepositoryTest`: ownership query, filtering.
- `ApplicationIT` (MockMvc + Spring Security Test): validation errors, registration/duplicate, BCrypt hash, login, unauthorized access, cross-user access returns 404.

## Known gaps / next steps

- "Edit" reopens a saved calculation in the calculator; saving creates a new record (no in-place update yet).
- `loan_comparisons` / `loan_comparison_items` tables exist but comparisons are not persisted; the dashboard shows saved calculations only.
- Add rate limiting and login lockout, HTTPS + secure cookies, Dockerfile/CI, and a production profile with secrets from a vault before going live.
