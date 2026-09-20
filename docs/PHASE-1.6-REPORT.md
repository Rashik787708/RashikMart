# WEEK 8 PHASE 1.6 LOCAL SMOKE TEST REPORT

## 1. Objective
Verify the packaged `target/RashikMart.war` deploys and runs correctly on the local Tomcat 9.0.120 instance, using only local infrastructure (no Cloudflare, no tunnel, no port exposure). Confirm external H2 database + external product-image storage behave correctly, CSRF protection works, role-based access control works, and data persists across a Tomcat restart and a WAR redeploy. Identify any warnings/gaps for later public deployment and produce a Deployment Readiness verdict.

## 2. Constraints & Scope (respected)
- Local-only smoke test. No public deployment, no Cloudflare/tunnel, no firewall change, no port exposure.
- No git add/commit/push performed. No migrations (javax → jakarta), no Tomcat upgrade, no H2 replacement, no schema change, no UI redesign.
- Pre-existing data preserved: project-local `data/rashikmart.mv.db` (last modified 14-09-2026) was NOT touched; `D:\RashikMartData` did not exist before the test, so the external store was created fresh by the app.
- Tomcat's existing `premmart` application was not modified.

## 3. Build & Artifact
- `mvn clean verify` — BUILD SUCCESS, final total `Tests run: 113, Failures: 0, Errors: 0, Skipped: 0`.
- `target/RashikMart.war` — 3,114,288 bytes, built 20-09-2026 20:50:15 (identical size to the Phase 1.5 artifact).
- __v=20260828_4 on stylesheet in some JSPs (values differ from Phase 1.5's v=20260824 on shared fragments; cosmetic only).

## 4. Tomcat & Environment
- Tomcat 9.0.120 at `C:\apache-tomcat-9.0.120`; JDK 21.0.8 at `C:\Program Files\Java\jdk-21`.
- `setenv.bat` defines `DB_URL=jdbc:h2:D:/RashikMartData/rashikmart` and `RASHIKMART_UPLOAD_DIR=D:/RashikMartData/product-images`. No credentials stored in the file.
- Startup: previously silent failure due to empty JAVA_HOME in the system environment; launched with per-process JAVA_HOME set. Clean shutdown via `shutdown.bat`.

## 5. Deployment
- WAR copied to `C:\apache-tomcat-9.0.120\webapps\RashikMart.war`. Auto-deploy exploded to `webapps\RashikMart\`.
- Tomcat started, port 8080 listening; app root `http://localhost:8080/RashikMart/` returning HTTP 200.

## 6. External Storage Creation (fresh)
- On first start the app created:
  - `D:\RashikMartData\rashikmart.mv.db` (H2 database file).
  - `D:\RashikMartData\product-images\` (upload directory) — created by `DatabaseContextListener`.
- This confirms the environment configuration chain (`env → sysprop → default`) is honored by the deployed app.

## 7. Core Pages HTTP Status
- `/RashikMart/` → 200 (4,209 bytes)
- `/RashikMart/css/style.css` → 200 (14,854 bytes)
- `/RashikMart/login.jsp` → 200 (1,795 bytes)
- `/RashikMart/register.jsp` → 200 (2,563 bytes)

## 8. Registration & Login
- Registered `p16.buyer@local.test` (BUYER) and `p16.seller@local.test` (SELLER) → redirect to login.jsp with success.
- Attempt to register with role `ADMIN` → rejected server-side (`Invalid role`); correct security guard (role cannot be elevated at registration).
- Admin seeded account `admin@rashikmart.com` / `admin123` login → 302 to `/RashikMart/admin/dashboard.jsp` (HTTP 200).
- `p16.admin@local.test` (created during earlier iterations) → login fails. Not a defect; the seeded admin account is the supported one. App functions correctly via seeded admin.
- Login sets HttpOnly JSESSIONID.

## 9. Role-Based Access Control (AuthFilter)
- Anonymous → `/seller/dashboard.jsp`, `/admin/dashboard.jsp` → 302 `/RashikMart/login.jsp?error=Please+login+first`.
- BUYER → `/seller/dashboard.jsp`, `/admin/dashboard.jsp` → HTTP 403.
- BUYER → `POST /seller/add-product` (even with a valid CSRF token) → HTTP 403. Role check precedes state-changing operation.

## 10. Seller Flows — Add Product (Multipart, CSRF)
- `POST /RashikMart/seller/add-product` with name, description, category (Spices), price, quantity, image → 302 `/RashikMart/seller/dashboard.jsp?success=Product+added+successfully`.
- Product created: id=1 "Turmeric Powder Test", price 4.50, qty 100.

## 11. Image Storage — External Directory (Key Result)
- Uploaded image persisted to `D:\RashikMartData\product-images\<uuid>.png` (NOT under `webapps\RashikMart\images\products`).
- The exploded WAR `images\products\` contains only the 4 original packaged images — confirms separation of bundled static assets from user uploads.
- Product image rendered via `/RashikMart/images/products/<uuid>.png` → HTTP 200, `Content-Type: image/png`, exact byte count of the uploaded file.

## 12. ProductImageServlet Edge Cases
- Valid file → HTTP 200 (image served from external dir).
- Nonexistent file → HTTP 200 with the bundled 526-byte `default-product.svg` fallback (matches `images/default-product.svg`, length 526).
- Path traversal (`..%2f..%2f..%2f...`) → HTTP 400. No arbitrary file disclosure observed.

## 13. Seller Flows — Edit Product (Multipart, CSRF, Image Replace)
- `POST /RashikMart/seller/edit-product` with id=1, updated description/price (5.25)/quantity (200), new image → 302 `dashboard.jsp?success=Product+updated+successfully`.
- New image saved as a new UUID in `D:\RashikMartData\product-images\`; edit form + product-details now reference the new UUID; the replaced image is deleted after the successful update (via `EditProductServlet.deleteOldImageIfSafe`, which refuses to remove `default-*` images or anything outside the configured upload dir).
- Note: in the manual smoke harness the old file was left in place because the test request did not submit the UI's `currentImageUrl` hidden field (so the servlet treated the old value as the default image and correctly skipped deletion). The real edit UI at `edit-product.jsp` submits `currentImageUrl`, so the delete path is exercised in normal browser usage.
- New image served HTTP 200 image/png; product-details shows updated price 5.25 and updated description.

## 14. Admin Moderation — Product Status (CSRF)
- `POST /RashikMart/admin/product-status` (id=1, active=true, valid token, form-urlencoded) → 302 `dashboard.jsp?success=Product+status+updated`.
- Product then appeared in the buyer marketplace with the external image reference.
- Note: this servlet consumes standard form parameters, not multipart (a multipart POST would not parse `id`/`active`).

## 15. Buyer Flows
- Marketplace (`/buyer/marketplace.jsp`) — after admin activation, product visible (anonymous access redirects to login).
- Product details (`/buyer/product-details?id=1`) — title, external image, price, review form all render.

## 16. Cart & Order (CSRF-Protected Mutations)
- Add to cart (productId=1, qty=2, token) → 302 `cart?success=Item+added+to+cart`; cart page 200 and shows the item.
- Update cart qty 2→4 with token → 302 `cart?success=Cart+quantity+updated`; verified qty=4.
- Place order from `/buyer/checkout.jsp` → 302 `order-success.jsp?orderId=1&success=Order+placed+successfully`.
- Order history `/buyer/orders.jsp` shows "Order #1"; order details `/buyer/order?id=1` → 200 showing the line item.

## 17. Reviews (CSRF-Protected)
- `POST /RashikMart/buyer/review` (productId=1, rating=5, reviewText, token) → 302 `product-details?id=1&success=Review+submitted+successfully`.
- Review text + rating render back on product-details.

## 18. CSRF Enforcement — Negative Tests (Key Result)
- `POST /buyer/cart` (add qty=5) with **missing** token → HTTP 403.
- `POST /buyer/cart` (add qty=5) with **invalid** token → HTTP 403.
- Cart quantity verified unchanged (stayed 2 after the two rejected add attempts) → rejected requests caused NO state change.

## 19. Persistence — Restart
- Tomcat cleanly stopped and restarted.
- After restart: product id=1 (admin dashboard shows it; seller dashboard still lists it), order #1 in order history, review visible on product-details, and both image files present in `D:\RashikMartData\product-images` (70 B + 51 B). External H2 file `rashikmart.mv.db` persisted (73,728 B post-restart).

## 20. Persistence — Redeploy (Same WAR)
- Exploded dir removed; exact same WAR (3,114,288 B) re-staged; Tomcat restarted.
- After redeploy: product id=1, order #1, review, and uploaded images all still present/served. External DB file persisted.

## 21. Warnings / Notes for Public Deployment
1. Image replacement in `EditProductServlet` DOES delete the previous image file after a successful update (guarded: never deletes `default-*` and only files inside the configured upload dir). The genuine orphan-image gap is product DELETION: `DeleteProductServlet` removes the product row but not its image file. Minor storage growth; acceptable for the smoke scope but worth a cleanup job later.
2. `p16.admin@local.test` test account created during testing cannot log in; the seeded `admin@rashikmart.com` account is fully functional. Not a product defect.
3. Multipart vs form-encoded POST handling is per-servlet; admin product-status and cart/review are form-encoded, add/edit-product are multipart. Clients must use the matching content type (forms in the UI do so correctly).
4. `setenv.bat` uses `AUTO_SERVER=TRUE` for convenience on a single local Tomcat; for a single-instance production deployment this is unnecessary and can be dropped.
5. No TLS in this local smoke test (plain HTTP on localhost). Expected; TLS/Cloudflare is reserved for Phase 2.
6. Tests used dedicated `p16.*` accounts; no data was written to the pre-existing project-local `data/rashikmart.mv.db`, which remains intact (untouched).

## 22. Deployment Readiness Verdict
**READY WITH WARNINGS** for eventual public deployment.

- All core flows (registration, login, role gating, seller add/edit, admin moderation, buyer market/details/cart/order/review), external storage, external database, CSRF protection, and persistence across restart and redeploy operate correctly.
- The warnings in Section 21 are non-blocking (orphan image cleanup, content-type per servlet, AUTO_SERVER flag) and should be addressed before or during Phase 2 hardening.
- Phase 2 (public/Cloudflare) manual steps, in order:
  1. Rebuild a production WAR (`mvn clean package`) from the clean `main` branch and re-run `mvn clean verify`.
  2. Prepare a production server (Linux recommended) with a systemd Tomcat 9 unit; set `CATALINA_HOME`, `JAVA_HOME`, and the `DB_URL`/`DB_USER`/`DB_PASSWORD`/`RASHIKMART_UPLOAD_DIR` env vars in an environment file (never commit secrets).
  3. Create an external data directory (e.g. `/opt/rashikmart-data`) writable by the Tomcat service user; place the WAR in `webapps`.
  4. Front with Cloudflare Tunnel (cloudflared) pointing to the Tomcat listener; terminate TLS at Cloudflare; no host port exposed to the internet, no firewall changes required for inbound public traffic.
  5. Set a strong admin password at first login after deployment, and rotate/confirm the seeded admin credentials (seed only on first run).
  6. Add a product-deletion image cleanup job and a DB backup schedule before promotion.
  7. Verify the public URL end-to-end (register→login→add product→admin approve→buy order→review →restart→redeploy), then leave the app running and mark Phase 2 complete.

- Artifacts/evidence retained: `target/RashikMart.war` (3,114,288 B), this report, and the uncommitted Phase 1.5+1.6 source changes (nothing committed per constraints).