# RashikMart Database Seeding

## Local database discovered

- **Engine:** H2 2.3.232 (file-based, matches `pom.xml`)
- **File:** `data/rashikmart.mv.db`
- **JDBC URL (local default):** `jdbc:h2:./data/rashikmart` (overridable via env `DB_URL`, user `DB_USER`, password `DB_PASSWORD` — see `com.rashik.rashikmart.config.DatabaseConfig`)
- **Backup (untracked):** `database/rashikmart-local-backup.mv.db`

## Seed file location

- **`database/seed.sql`** — committed, auditable export of the local demo data (1 seller + 48 products, 6 categories).
- The exact same bytes are packaged in the WAR as `WEB-INF/classes/seed_catalog.sql` (from `src/main/resources/seed_catalog.sql`) so the container can find it on the classpath. `database/seed.sql` is also safe to import manually against an empty H2 schema with Schema 2 already created by the app.

Excluded on purpose: the local DB contained ~77 auto-generated test/load-test users (`*@test.com`) plus test orders/carts. Only the real demo data (seller **Mohammed Rashik** + 48-product catalog) is seeded.

## How seeding works

On every startup, `DatabaseContextListener.contextInitialized` does:

1. Connect to H2 (`DatabaseConfig`).
2. Create the schema if missing (`CREATE TABLE IF NOT EXISTS` for users, products, cart, cart_items, orders, order_items, reviews) — idempotent, reuses the existing mechanism.
3. Create the default admin account only if it is missing.
4. **Seed only if the database is fresh:** if `SELECT COUNT(*) FROM products` is `0`, run `seed_catalog.sql` via H2 `RunScript`. If products already exist, the seed is skipped and existing data is never touched.

The seed itself is also idempotent:

- the seller is inserted only when the email does not exist;
- each product is inserted only when the same seller + product name + category is missing.

### Result

| Scenario | Behavior |
|----------|----------|
| Fresh database (new Render deploy) | schema created → seed runs → seller + 48 products appear |
| Restart (same data) | seed skipped → no duplicates |
| Local database with data | seed skipped → local data untouched |

## Product images

- `products.image_url` stores only the file name (e.g. `wireless-bluetooth-headphones.jpg`) — no Windows paths are stored.
- The 48 catalog images were copied from `data/product-images/` into `src/main/webapp/images/products/`, so they are packaged inside the WAR.
- `ProductImageServlet` (`/images/products/*`) serves: external upload directory → WAR fallback `images/products` → bundled `default-product.svg`.

## Rebuild

```
mvn clean package -DskipTests
```

Produces `target/RashikMart.war`. Verified contents: `WEB-INF/classes/seed_catalog.sql` and 52 files under `images/products/`.

Seeding verified with H2 2.3.232 against a fresh database:

| Step | users | products |
|------|-------|----------|
| After first seed | 1 | 48 |
| After a restart (second seed run) | 1 | 48 (no duplicates) |

## Deploy

1. Commit everything (`git add .`, `git commit`, `git push origin main`).
2. Render builds `Dockerfile` → `mvn clean package -DskipTests` → `target/RashikMart.war` deployed as Tomcat `ROOT.war`.
3. On the first boot of a fresh container a new H2 database is created and automatically seeded.

Note: Render containers use an ephemeral filesystem, so H2 data written at run time (new orders/users) is lost when the container is replaced. Seed data is re-applied on a fresh database every time. For durable live data, configure a persistent database (e.g. Render Disk or a managed DB) — out of scope for this demo.