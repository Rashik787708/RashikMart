# RashikMart — Testing Report

This document records the testing strategy and results for RashikMart
(Java Servlet/JSP + Maven + H2). It is part of the "Week 7" security
hardening and quality-assurance milestone.

## How to run

```bash
mvn clean test        # compile + run every unit/integration test
mvn clean verify      # test + package the WAR (used by CI)
mvn clean package     # produce target/RashikMart.war
```

Tests run against an isolated **in-memory H2 database** (configured in
`pom.xml` via the Maven Surefire `db.url` system property), so each
Maven invocation starts from a clean schema. No local `data/` files are
touched by the test suite.

## Test inventory (113 tests, 0 failures)

### DAO / database layer
| Class | Coverage |
| --- | --- |
| `UserDAOTest` | user registration, lookup by email, duplicate-email rejection, BCrypt hash not stored in plain text |
| `ProductDAOTest` | product CRUD, seller ownership filter, availability filtering |
| `CartDAOTest` | add / update / remove / clear items, cart total, available-stock cap |
| `OrderDAOTest` | transactional order creation, stock deduction, rollback on failure, buyer-ownership queries, seller-order isolation, cart cleared after order |
| `ReviewDAOTest` | purchase eligibility, duplicate-review prevention (DB + DAO), rating range 1-5, average rating/review count, XSS and SQL-injection payloads stored as safe inert text |

### Business rules / model
| Class | Coverage |
| --- | --- |
| `BusinessRuleTest` | model invariants (subtotal, stock, default active status, role values) |

### Security
| Class | Coverage |
| --- | --- |
| `AuthFilterTest` | unauthenticated requests redirect to login; incomplete/cross-role sessions rejected |
| `SecurityTest` | classic SQL injection payloads return no rows (PreparedStatement); XSS escaping of `<script>`/attribute payloads via `HtmlUtil`; BCrypt cost factor 12 and 60-char hash format |

### Servlet layer
| Class | Coverage |
| --- | --- |
| `LoginServletTest` | session-fixation defense (`changeSessionId`), invalid credentials, per-role redirects |
| `RegisterServletTest` | role whitelist (no ADMIN via public form), email format, password length, duplicate email |
| `CartServletTest` | authentication/role enforcement, quantity caps, add/update/remove |
| `EditProductServletTest` | seller cannot fetch/edit another seller's product; oversized/malformed input rejected |
| `DeleteProductServletTest` | seller cannot delete another seller's product (ownership-scoped DAO) |
| `OrderServletTest` | buyer cannot view another buyer's order; empty-cart checkout rejected; order placement |
| `ProductDetailsServletTest` | invalid/missing/unknown/inactive product handling; active product forwards with review data |
| `ReviewServletTest` | unauthenticated/non-buyer rejection, missing/invalid/inactive product, rating range 1-5, 500-char review limit, purchase-eligibility and duplicate-review rejection, valid submission |
| `AdminProductStatusServletTest` | non-admin requests receive 403; status toggle only for admins |

### End-to-end flow
| Class | Coverage |
| --- | --- |
| `RashikMartFlowTest` | register → login → seller adds product → buyer browses/carts → checkout → order placed → seller sees the order |

## Manual / integration verification

### Weaponised SQL injection & XSS probes
Beyond unit tests, the following payloads were manually verified against
the running application and are covered by `SecurityTest`:

- `' OR '1'='1`, `' OR 1=1 --`, `admin' --`, `' UNION SELECT ... --`
  → no rows returned, no authentication bypass.
- `<script>alert('XSS')</script>` and `<img src=x onerror=alert(1)>`
  → rendered as inert escaped text everywhere user input is displayed.

### Load test (EXECUTED)

A real load test was run against a live deployment (Apache Tomcat 9.0.120,
WAR built by this Maven project, H2 file DB, HikariCP pool) using
`src/test/java/com/rashik/rashikmart/loadtest/LoadTestRunner.java`
(JDK-only, `java.net.http`).

```text
LOAD START: users=10 durationMs=60000 endpoint=http://localhost:8080/RashikMart/buyer/marketplace
LOAD DONE:  wallSeconds=60
requests        = 196804
requestsPerSecond = 3280.2
errors          = 0
avgLatencyMs    = 4
minLatencyMs    = 1
maxLatencyMs    = 389
LOAD TEST EXIT CODE: 0
```

Requirements met: **≥10 concurrent users**, **60 s duration**, read-heavy
endpoint (`/buyer/marketplace` — real session + SQL + JSP rendering),
**zero errors**.

Reproduce:

```bash
# 1. build    : mvn clean package
# 2. deploy target/RashikMart.war to a Tomcat 9 webapps/
# 3. start Tomcat
# 4. run      : java -cp target/test-classes \
#                com.rashik.rashikmart.loadtest.LoadTestRunner \
#                http://localhost:8080/RashikMart 10 60
```

## CI

`.github/workflows/build.yml` runs `mvn --batch-mode clean verify` on
GitHub Actions (JDK 17 Temurin) for every push/PR to `main` and uploads
the WAR artifact.

### Latest local result

```text
mvn clean verify  → BUILD SUCCESS
Tests run: 113, Failures: 0, Errors: 0, Skipped: 0
```

## Honest status flags (Week 7 checklist)

| Item | Result |
| --- | --- |
| PreparedStatement on all DB access | PASS (no `createStatement()` in main sources) |
| BCrypt password hashing | PASS (`$2a$12$`, verified in `SecurityTest`) |
| Session fixation defense | PASS (`request.changeSessionId()` on login) |
| Role-based access (server side) | PASS (`AuthFilter` + per-servlet checks) |
| Ownership checks (seller/buyer) | PASS (servlet + DAO-scoped queries) |
| XSS output escaping | PASS (`HtmlUtil.escape` on all user output) |
| Upload validation (type/size, UUID names) | PASS |
| Safe error pages | PASS (no stack traces exposed; `web.xml` error-page) |
| Input validation (numeric + length) | PASS |
| DAO tests | PASS (4 DAO test classes) |
| Security tests | PASS (`SecurityTest`, `AuthFilterTest`) |
| Servlet/session tests | PASS (8 servlet test classes) |
| End-to-end flow test | PASS (`RashikMartFlowTest`) |
| Load test ≥10 users / 60 s | PASS (EXECUTED — see results above) |
| Maven build via CI | PASS (`mvn clean verify` green) |
| Reviews/ratings feature | PASS (Review model, ReviewDAO, ReviewServlet, reviews table, purchase eligibility, duplicate protection, rating 1-5, XSS/SQL-injection safe, displayed on product-details.jsp) |
| DB credentials committed | PASS (none; local defaults via env/system properties, DB files gitignored) |