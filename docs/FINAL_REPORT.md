# RashikMart Final Review Capstone Report

**Course & Regulation:** Anna University R2025 Semester 3  
**Project Title:** RashikMart — Multi-Vendor E-Commerce Platform  
**Candidate Name:** Rashik  
**Target Package:** `com.rashik.rashikmart`  
**Review Date:** October 10, 2026  

---

## 1. Executive Summary

RashikMart is a robust, lightweight, multi-vendor web application developed using pure Java Servlets, JSP, JDBC, HikariCP, and modern frontend styling. The platform accommodates three primary user roles (`BUYER`, `SELLER`, `ADMIN`), facilitating direct peer-to-peer commerce, transaction-safe inventory management, lifecycle order management, verified reviews, and conversational AI assistance.

The codebase strictly adheres to the Anna University R2025 Semester 3 Capstone specifications, eliminating monolithic third-party frameworks in favor of clean design patterns, parameterized queries, and rock-solid defense-in-depth security.

---

## 2. System Architecture & Component Design

The platform uses a modular 3-tier presentation/service/data-access architecture:

1. **Presentation Tier:**
   - Java Servlets (extending `HttpServlet`) mapped via standard annotations and `web.xml`.
   - Server-side JSPs rendering semantic HTML, with dynamic output thoroughly sanitized using `HtmlUtil.escape()`.
   - RESTful JSON endpoints (`/api/v1/health`, `/api/v1/chat`, `/api/v1/orders/status`) with standard `ApiResponse<T>` envelope serialization via Google Gson.

2. **Filter & Middleware Pipeline:**
   - `EncodingFilter`: Enforces UTF-8 character encoding on all requests and responses.
   - `RequestIdFilter`: Attaches a unique UUID `X-Request-Id` to each request and binds it to the SLF4J MDC context for correlated log aggregation.
   - `AuthFilter`: Enforces strict Role-Based Access Control (RBAC) across `/buyer/*`, `/seller/*`, and `/admin/*`.

3. **Data Access Tier:**
   - Dedicated DAO classes (`UserDAO`, `ProductDAO`, `CartDAO`, `OrderDAO`, `ReviewDAO`) abstracting all SQL interactions.
   - High-throughput pooled connection management via HikariCP (`DatabaseConfig`).
   - Dual-database compatibility: File-based and in-memory H2 for zero-configuration local development; PostgreSQL for production deployment on Render.

---

## 3. Design Patterns Applied

| Pattern | Implementation in RashikMart | Rationale |
|---|---|---|
| **Data Access Object (DAO)** | `UserDAO`, `ProductDAO`, `CartDAO`, `OrderDAO`, `ReviewDAO` | Isolates SQL logic and JDBC resource management from web controllers. |
| **Strategy Pattern** | `ChatProvider` interface (`GeminiChatProvider`, `MockChatProvider`) | Enables seamless interchange between cloud AI models and offline mock engines without modifying client callers. |
| **Factory Pattern** | `ChatProviderFactory` | Dynamically instantiates the appropriate chatbot provider based on `AI_CHATBOT_PROVIDER` environment configuration. |
| **Singleton Pool** | `DatabaseConfig.getDataSource()` | Guarantees a single shared HikariCP connection pool instance throughout the webapp lifecycle. |
| **Front Controller / Filter Dispatch** | `AuthFilter`, `RequestIdFilter`, `EncodingFilter` | Centralizes pre-processing concerns (encoding, tracing, authorization) before request dispatch. |
| **Value Object / DTO** | `ApiResponse<T>`, `ApiError`, `SellerOrderItem` | Encapsulates API payloads and multi-table projection models cleanly. |

---

## 4. Entity-Relationship Model (Schema)

The underlying relational schema consists of 7 normalized core tables:
- `users`: User identity, hashed credentials (`BCrypt`), and authorization role (`BUYER`, `SELLER`, `ADMIN`).
- `products`: Catalog items linked to a seller, with unit price, stock quantity, and soft-delete active flag.
- `cart`: Shopping cart header isolated by `buyer_id` (1:1).
- `cart_items`: Cart items with quantities and foreign-key cascades.
- `orders`: Immutable purchase headers with buyer reference, total amount, and lifecycle status.
- `order_items`: Historic line-item snapshots preserving price and quantity at time of purchase.
- `reviews`: Product reviews and ratings (1–5 stars) with composite uniqueness on `(product_id, buyer_id)`.

Refer to [`docs/diagrams/D1_ER_Diagram.mmd`](diagrams/D1_ER_Diagram.mmd) for the complete visual diagram.

---

## 5. Security & Defenses (§9 Checklist)

1. **SQL Injection Defense:** 100% of SQL statements handling user data utilize `PreparedStatement` with typed parameter binding.
2. **Cross-Site Scripting (XSS):** All dynamic strings in JSPs pass through `HtmlUtil.escape()`. Frontend chatbot widget operates strictly with DOM `textContent`.
3. **Cross-Site Request Forgery (CSRF):** Cryptographically secure, session-bound CSRF tokens validated via `CsrfUtil` on all state-changing `POST` requests.
4. **Credential Security:** Passwords hashed with BCrypt (`jBCrypt`) with salt factor 12. No plaintext passwords or API keys are ever stored or logged.
5. **Session Fixation Prevention:** Calling `request.changeSessionId()` upon successful login prevents session fixation attacks.
6. **Path Traversal Protection:** Product image streaming (`ProductImageServlet`) strictly enforces containment within the designated upload directory.

---

## 6. Known Limitations & Future Roadmap

- **External Storage Integration:** Current uploads write to a mounted filesystem; future phases can introduce S3-compatible cloud object storage (e.g. AWS S3 / Cloudflare R2).
- **Payment Gateway Integration:** Payment currently executes via a simulated mock checkout flow; production would integrate Stripe or Razorpay webhooks.
- **Service Layer Abstraction:** While business rules are centralized, full decoupling into formal `I...Service` interfaces and Flyway migration runner can be finalized in Phase 2.
