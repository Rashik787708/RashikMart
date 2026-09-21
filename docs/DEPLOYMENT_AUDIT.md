# RashikMart Deployment Audit

Audit date: 2026-09-21
Project: RashikMart (GitHub: https://github.com/Rashik787708/RashikMart)

## 1. Environment / Toolchain

| Item | Value |
|------|-------|
| OS | Windows 11 (local dev) |
| Java | 17 (Maven `<maven.compiler.source/target>17</...>`) |
| Build | Maven WAR (`packaging = war`) |
| WAR name | `RashikMart` → `target/RashikMart.war` (`<finalName>RashikMart</finalName>`) |
| Servlet API | javax.servlet-api 4.0.1 (Tomcat 9) |
| H2 version | 2.3.232 (`com.h2database:h2`) |
| HikariCP | 5.1.0 |
| Password hashing | org.mindrot:jbcrypt 0.4 (BCrypt) |
| Runtime | Tomcat 9, deployed via Docker to Render |

## 2. H2 Database

- **JDBC URL (local default):** `jdbc:h2:./data/rashikmart`
- **DB type:** file-based H2 (not in-memory)
- **Local file:** `data/rashikmart.mv.db`
- **Credentials:** user `sa`, password `WE` (defaults; overridable via `DB_URL` / `DB_USER` / `DB_PASSWORD`).
- Resolution order in `DatabaseConfig`: env var `DB_URL` → system property `db.url` → `jdbc:h2:./data/rashikmart`.

## 3. Database Schema

Created idempotently by `DatabaseContextListener` (`CREATE TABLE IF NOT EXISTS`) on every startup:

| Table | Columns / notes |
|-------|-----------------|
| `users` | id (identity), name, email (unique), password, role |
| `products` | id (identity), seller_id (FK → users.id), name, description, category, price, quantity, image_url, active, created_at |
| `cart` | id, buyer_id (FK → users.id, unique), created_at |
| `cart_items` | id, cart_id (FK → cart.id ON DELETE CASCADE), product_id (FK → products.id ON DELETE CASCADE), quantity, unique(cart_id, product_id) |
| `orders` | id, buyer_id (FK → users.id), total_amount, status, created_at |
| `order_items` | id, order_id (FK → orders.id ON DELETE CASCADE), product_id (FK → products.id), quantity, price |
| `reviews` | id, product_id (FK → products.id ON DELETE CASCADE), buyer_id (FK → users.id), rating, review_text, created_at, unique(product_id, buyer_id) |

Notes:
- There is **no separate `categories` table** — category is a VARCHAR column on `products`.
- The local DB was last written before the `reviews` feature shipped, so the `reviews` table did not exist in it yet; it is created automatically on the next application start.

## 4. Local Data Contents (data/rashikmart.mv.db)

Read-only inspection results (H2 Shell, `ACCESS_MODE_DATA=r`):

| Table | Rows | Classification |
|-------|------|----------------|
| `users` | 79 | 1 admin, 1 demo seller (Mohammed Rashik), 77 auto-generated test/load-test users (`*@test.com`) |
| `products` | 87 | 48 = real demo catalog (seller id 111), 39 = test fixtures / load-test junk |
| `cart` | 21 | runtime/test-only |
| `cart_items` | 0 | – |
| `orders` | 20 | runtime/test-only |
| `order_items` | 25 | runtime/test-only |
| `reviews` | table absent | created on next startup |

Important demo data that must appear on Render:
- Seller: `Mohammed Rashik` / `mohammed786rashik@gmail.com` / role `SELLER`
- 48 products in 6 categories (Electronics, Mobiles & Accessories, Computers & Accessories, Fashion, Home & Kitchen, Books)

These 48 products are exactly what the bundled `src/main/resources/seed_catalog.sql` seeds. The remaining rows are test artifacts and will **not** be exported/seed (see docs section 5).

## 5. Seeding Mechanism (already present, idempotent)

`DatabaseContextListener.contextInitialized`:
1. Creates all tables (`IF NOT EXISTS`) — safe on existing DBs.
2. Seeds admin `admin@rashikmart.com` only when the email is missing (`UserDAO.findByEmail`).
3. Runs `seed_catalog.sql` (classpath resource) via H2 `RunScript`:
   - Seller inserted only when the email does not exist.
   - Products inserted only when the same seller+name+category is missing.
   - Therefore **duplicates are never created on restart**.

Required fix: `src/main/resources/seed_catalog.sql` is currently **untracked** by Git and would be missing from the deployed WAR (no catalog). It must be committed.

## 6. Image Storage

- Uploaded product photos live in an **external** directory, resolved by `DatabaseConfig.getUploadDir()`:
  1. env `RASHIKMART_UPLOAD_DIR`
  2. system property `rashikmart.upload.dir`
  3. default `./data/product-images`
- Local store: `data/product-images/` (gitignored, 48 catalog images, ~5.7 MB).
- DB column `products.image_url` stores only the file name (e.g. `wireless-bluetooth-headphones.jpg`); JSPs build `/images/products/<name>`.
- `ProductImageServlet` `/images/products/*` resolves: external upload dir → exploded WAR `/images/products` fallback → bundled `default-product.svg`.
- `default-product.svg` is committed in `src/main/webapp/images/`.
- Only 4 legacy images are committed in `src/main/webapp/images/products/` today; the 48 catalog images are **not** in the WAR.

Required fix: copy the 48 catalog images from `data/product-images/` into `src/main/webapp/images/products/` so they are packaged inside `RashikMart.war` and served on Render via the WAR fallback. No database path changes needed (only file names are stored).
Source images come from Wikimedia Commons / Open Library covers (freely licensed) — safe to commit. No Windows absolute paths are used anywhere in the app.

## 7. Diagnostics & Run-time Secrets

- No plaintext passwords stored: admin uses BCrypt via listener; seller uses a BCrypt hash in seed SQL.
- Demo admin login (`admin@rashikmart.com` / `admin123`) and demo seller are intentional demo accounts, already present in repository source.
- The default DB password `WE` is a local dev password already committed; it is overridable through env vars for Render.
- Untracked dev-local files that must remain out of Git: `cloudflared.exe`, `config.yml`, `java` (empty), `scripts/`, `data/`, `logs/`.

## 8. Dockerfile & Render

- `Dockerfile` is already correct:
  - Build stage: `maven:3.9-eclipse-temurin-17` → `mvn clean package -DskipTests`
  - Runtime stage: `tomcat:9.0-jdk17`, deploys `target/RashikMart.war` as `ROOT.war`
- WAR name verified against `pom.xml` (`<finalName>RashikMart</finalName>`).
- Render previously failed because the Root Directory was set to `RashikMart` while the repository root is already `RashikMart` (i.e., Render looked for `RashikMart/RashikMart`). Fix is a dashboard setting: Root Directory = **empty**, Dockerfile path = `./Dockerfile`.
- Container filesystem is ephemeral: H2 file data does not survive a container replacement unless persistent storage is configured. Seeding initializes a fresh DB automatically; persistence of runtime data on Render is documented as a limitation.

## 9. Open Issues Found

1. `src/main/resources/seed_catalog.sql` untracked → must be committed.
2. 48 catalog product images not shipped in the WAR → must be copied into `src/main/webapp/images/products/`.
3. `cloudflared.exe`, `config.yml`, `java`, `scripts/`, and local DB/logs must be ignored by Git.
4. No `README.md` exists → create one.
5. `h2-classpath.txt` is a tracked dev helper listing local `.m2` paths (harmless, not used at runtime).