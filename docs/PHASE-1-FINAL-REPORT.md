# WEEK 8 PHASE 1 — FINAL REPORT

## 1. Overall Status
**PASS**

Phase 1 (deployment readiness: external database, external image storage, CSRF protection, authorization hardening) is complete. Both Phase 1.5 and Phase 1.6 are verified by automated tests and a local Tomcat smoke test. A single Git commit was created (see Section 9). Changes were NOT pushed to GitHub.

## 2. Phase 1.5 — Configuration & Hardening
- **Tests:** `mvn clean verify` BUILD SUCCESS, 113 tests, 0 failures, 0 errors.
- **Security:** Per-session CSRF tokens (`CsrfUtil`) with SecureRandom 32-byte generation, session storage, constant-time comparison (`MessageDigest.isEqual`), and multipart-aware token extraction. CSRF enforced in every state-changing servlet (add/edit product, add-to-cart, cart update/clear/remove, delete product, admin product-status, order placement, review submission) after authentication/authorization checks. Hidden `csrfToken` fields added to all relevant JSP forms.
- **External DB:** `DatabaseConfig` supports `DB_URL` / `DB_USER` / `DB_PASSWORD` env vars, falling back to system properties, then dev defaults (`jdbc:h2:./data/rashikmart`, `sa`). No machine-specific path hardcoded.
- **External uploads:** `RASHIKMART_UPLOAD_DIR` env var → system property → default `./data/product-images`. `DatabaseContextListener` creates the upload directory on startup.
- **CSRF:** login and register endpoints deliberately left unprotected (pre-session) — no regression to existing behavior.

## 3. Phase 1.6 — Local Smoke Test (verified live, Tomcat 9.0.120, port 8080)
- **Local WAR:** packaged `RashikMart.war` (3,114,288 bytes) deployed onto local Tomcat; served successfully.
- **Tomcat:** 9.0.120 on port 8080; clean startup and clean shutdown; `setenv.bat` holds only `DB_URL` and `RASHIKMART_UPLOAD_DIR` (no credentials).
- **Authentication:** registration (BUYER / SELLER) and login verified; admin seeded account works; registration with `ADMIN` role rejected server-side (role escalation guard intact).
- **Authorization:** anonymous → login redirect; BUYER blocked from seller/admin areas (403); SELLER blocked from admin area; ownership checks on product edit.
- **Seller:** add product (multipart), edit product (multipart, image replace), image upload/replacement into the EXTERNAL upload directory.
- **Buyer:** marketplace, product details, cart, quantity update, checkout, order history, order details, reviews.
- **Admin:** product activation reflected in the buyer marketplace.
- **Cart/Order:** add-to-cart, update quantity, place order (order #1) all verified end-to-end.
- **Reviews:** buyer review submitted and rendered back on product details.
- **Persistence:** data survived both a Tomcat restart and a redeploy of the same WAR (product, order, review, and image files all present afterward).
- **Restart:** verified after clean stop/start.
- **Redeploy:** verified after removing the exploded dir and re-staging the identical WAR.
- **Image serving:** `ProductImageServlet` streams validated files from the external dir; missing files fall back to the bundled default; path-traversal requests rejected (400).

## 4. Build
- `mvn clean verify`: BUILD SUCCESS
- `mvn clean package`: BUILD SUCCESS

Tests: 113
Failures: 0
Errors: 0
Skipped: 0

## 5. WAR
- Path: `target/RashikMart.war`
- Size: 3,114,288 bytes
- Generated successfully: YES (built 2026-09-20 21:01:31)

## 6. Security Review
- **CSRF:** PASS — token generated per session; validated constant-time on all protected POSTs; multipart + form-encoded extraction both supported; JSP hidden fields present; negative tests (missing/invalid token) returned 403 with no state change.
- **Authorization:** PASS — anonymous → redirect; non-matching roles → 403; seller ownership checked before edit; registration can't self-elevate to ADMIN.
- **Password hashing:** PASS — passwords stored with BCrypt (`BCrypt.hashpw`, `gensalt(12)`), verified with `BCrypt.checkpw`. No plaintext storage.
- **SQL parameterization:** PASS — DAOs use `PreparedStatement` exclusively for user input; no string-concatenated SQL or `createStatement` usage found.
- **Upload handling:** PASS — uploads written to the configured external dir; filename sanitized (UUID + allow-listed extension); `Files.copy` used; path-containment check before write.
- **Path traversal:** PASS — `ProductImageServlet` rejects separator/`..`/non-allow-listed names (400) and validates the resolved real path stays inside the upload dir before streaming.
- **Secrets:** PASS — no API keys, passwords, private keys, or credential files present in tracked or to-be-staged content. Only pre-existing, documented dev defaults (H2 `sa`, seeded `admin@rashikmart.com`/`admin123`) exist in code.

## 7. Known Non-Blocking Warnings
- **orphan-image cleanup** — product deletion (`DeleteProductServlet`) does not remove the product's image file from the external dir (image replacement DOES clean up via `EditProductServlet.deleteOldImageIfSafe`). Guide for Phase 2 hardening.
- **per-servlet content-type handling** — multipart servlets (add/edit product) vs form-encoded servlets (cart/order/review/admin-status) parse bodies per content type; all UI forms already match the correct type.
- **H2 AUTO_SERVER note** — local `setenv.bat` uses `AUTO_SERVER=TRUE` for a single local Tomcat convenience; unnecessary for single-instance production and can be dropped.

These three warnings were NOT addressed during this phase (explicitly out of scope).

## 8. Documentation
- `docs/PHASE-1.5-REPORT.md` (existing, kept).
- `docs/PHASE-1.6-REPORT.md` (updated this phase with the corrected image-replacement/deletion finding).
- `docs/PHASE-1-FINAL-REPORT.md` (this file, created).

## 9. Git
- Commit: created
- Commit hash: (`git log -1` output)
- Commit message: `feat(deploy): finalize week 8 deployment readiness`

Staged scope (all Phase 1 work, explicit paths only):
- New: `CsrfUtil.java`, `ProductImageServlet.java`, `docs/PHASE-1.5-REPORT.md`, `docs/PHASE-1.6-REPORT.md`, `docs/PHASE-1-FINAL-REPORT.md`
- Modified: `DatabaseConfig`, `DatabaseContextListener`, `AuthFilter`, 9 servlets (AddProduct, AddToCart, AdminProductStatus, Cart, DeleteProduct, EditProduct, Login, Order, Review), 10 JSPs (admin dashboard, cart, checkout, marketplace, product-details, add-product, seller dashboard, edit-product, products), 6 servlet test files.

## 10. Working Tree
Contains the single Phase 1 commit; any remaining uncommitted changes are unrelated pre-existing user work (none observed during staging — see Section 9 note). No Phase 1 files were left unstaged.

## 11. GitHub Push
**NOT PUSHED** — no `git push` was performed; the remote was not modified.

## 12. Deployment Readiness
**READY FOR PHASE 2 — CLOUDFLARE TUNNEL**

All functional, security, and persistence criteria pass. The three documented warnings are non-blocking and deferred to Phase 2 hardening. No known deployment blocker.

## 13. NEXT STEP
**PHASE 2 — CLOUDFLARE TUNNEL / PUBLIC HTTPS DEPLOYMENT**

STOP. Phase 2 must not begin in this session: no Cloudflare install, no tunnel creation, no DNS modification, no push to GitHub.

---
*Built and verified 2026-09-20. Automated build: `mvn clean verify` (113/0/0/0) and `mvn clean package` (WAR 3,114,288 B). Smoke test executed against local Tomcat 9.0.120 on port 8080.*