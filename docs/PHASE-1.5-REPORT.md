# RashikMart — Phase 1.5 Report

**Date:** 2026-09-20
**Branch:** main (working tree only, nothing committed or pushed)

## 1. What Phase 1.5 was

Deployment-hardening of the existing Maven WAR app (`mvn clean package` -> `target/RashikMart.war`):

1. Move the H2 JDBC source-of-truth out of a hardcoded URL into an environment/db-URL-backed config.
2. Move product-image uploads **out of the exploded WAR** into an **external**, persisted directory so
   photos survive server restarts and re-deploys.
3. Add **per-session CSRF protection** across all state-changing POST flows.

No deployment, no Tomcat install, no tunneling, no `git` operations were performed. End-goal remains
unchanged: run the WAR and smoke-test on localhost.

## 2. Summary of changes (verified on disk + via green build)

### 2.1 Database configuration — `DatabaseConfig.java`
- H2 datasource built from resolvable values instead of a single hardcoded URL:
  - JDBC URL <- `DB_URL` env / `db.url` sysprop -> default `jdbc:h2:./data/rashikmart`
  - user <- `DB_USER` env / `db.user` sysprop -> default `sa`
  - password <- `DB_PASSWORD` env / `db.password` sysprop -> default `WE`
  - pooled via HikariCP (`HikariDataSource`).
- `DatabaseConfig.getUploadDir()` returns the **external upload directory**:
  - `RASHIKMART_UPLOAD_DIR` env -> `rashikmart.upload.dir` sysprop -> default `./data/product-images`.
  - The returned path is a plain filesystem path (never `getRealPath`, never inside the exploded WAR).

### 2.2 External upload dir creation — `DatabaseContextListener`
- `contextInitialized` now creates the external upload directory
  (`DatabaseConfig.getUploadDir()`) with `mkdirs()` instead of the exploded-WAR `/images/products`.
- `contextDestroyed` remains a no-op (clean shutdown leaves Hikari + H2 to close themselves).

### 2.3 CSRF — `CsrfUtil` (new)
- `getOrCreateToken(request, response)` — builds a 32-byte secure token, stores in the session,
  returns it to the caller; resets when the session is new/invalidated.
- `extractToken(request)` — reads the submitted token from the request, with support for
  multipart/form-data POSTs.
- `isValid(request)` — constant-time comparison of the submitted token against the session token
  (`MessageDigest.isEqual`).

### 2.4 Servlets — CSRF enforced in `doPost` on all protected write flows
CSRF check is the FIRST gate (before any business logic) in:
| Servlet | Route | Write op |
|---|---|---|
| `LoginServlet` | `/login` | authenticate |
| `RegisterServlet` | `/register` | create account |
| `AddToCartServlet` | `/buyer/cart/add` | add to cart |
| `CartServlet` | `/buyer/cart` | update/remove cart line |
| `OrderServlet` | `/buyer/checkout` | place order |
| `ReviewServlet` | `/buyer/review` | submit review |
| `DeleteProductServlet` | `/seller/delete-product` | delete product |
| `AddProductServlet` | `/seller/add-product` | add product + image |
| `EditProductServlet` | `/seller/edit-product` | edit product + image |
| `AdminProductStatusServlet` | `/admin/product-status` | change product status |

### 2.5 Product image upload + serving (external dir)
- `AddProductServlet` / `EditProductServlet` upload through `DatabaseConfig.getUploadDir()`
  (UUID-named files, extension whitelist, `Paths` basename sanitization, unique filename).
- `ProductImageServlet` (`/images/products/*`) serves the file from the external upload dir,
  with fallback to the bundled default when missing.

### 2.6 JSPs — CSRF token on all POST forms
Every JSP form that performs a state change now includes a hidden `csrfToken` field bound to the
session token, and sessions carry the token via the CSRF filter/write on POST.

## 3. Build & verification

Local JDK 17 + Maven; target `cd` into project with:

```
mvn clean verify
mvn clean package
```

**Result:** BUILD SUCCESS — `target/RashikMart.war` produced.
- Compiler: all Java files compile (JDK 17, target 17).
- Tests: 113 tests run, 0 failures, 0 errors (DAO, model business rules, full servlet workflows,
  auth/security filters).

## 4. Proof-of-deployment markers (for later)

When Phase 1.6 smoke-testing runs against the WAR on Tomcat 9:
- `/images/products/<name>` returns the image from the **external** dir (not the exploded WAR) — verifiable
  by re-deploying and confirming the image still renders.
- Photo uploads persist across a Tomcat restart/re-deploy.
- Login/register/cart/checkout POSTs without a valid token are rejected (403 / redirect with
  `Invalid+or+missing+CSRF+token`); forms carry the hidden token so the happy path works.

## 5. Outstanding (not in this phase)
- Any eventual real deployment steps (container setup, secrets management, WAR deploy to a live
  server) are intentionally out of scope for Phase 1.5 and were not started.
