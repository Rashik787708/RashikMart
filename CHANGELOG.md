# Changelog

All notable changes to the RashikMart project are documented in this file.
The project adheres to [Semantic Versioning](https://semver.org/).

## [v1.1.0] - 2026-10-09
### Added
- **AI Chatbot (O4, §11, §17):** Implemented `ChatProvider` strategy with `GeminiChatProvider` (server-side HTTP calls using `java.net.http.HttpClient` to `gemini-3.1-flash-lite`), `MockChatProvider` with 10+ FAQ canned responses, and `ChatProviderFactory`.
- **Chatbot Resiliency & Security:** `ChatService` with input validation (max 500 chars), per-session sliding-window rate limit (10 msg/min), in-memory query caching, real catalog context extraction, and degraded failover response.
- **Floating Chat Widget:** Lightweight vanilla JavaScript widget (`chat-widget.js`) and modern CSS (`chat-widget.css`) with safe `textContent` rendering, mobile responsiveness, and CSRF token propagation.
- **Order Lifecycle State Machine (O2):** Strict order transitions `PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED` with HTTP 409 Conflict rejection for illegal transitions; seller and admin status control via `OrderStatusServlet`.
- **Review Verification (F8):** `ReviewDAO` enforcement requiring buyers to hold a `DELIVERED` order before submitting ratings or reviews.
- **REST & Observability Foundation (§13, §18):** Added Google Gson, standardized `ApiResponse<T>` / `ApiError` envelopes, `GET /api/v1/health` probe (`SELECT 1`), `EncodingFilter` (UTF-8), and `RequestIdFilter` with `X-Request-Id` MDC logging.
- **Test Suite Expansion:** Upgraded to 159 automated test cases with JUnit + Mockito integration.

### Fixed
- **Checkout overselling:** Stock is now deducted with an atomic `UPDATE ... WHERE quantity >= ?` guard inside the checkout transaction; insufficient live stock aborts the order, preserves the cart, and can never drive stock negative under concurrent checkout.
- **Stored XSS via product images:** `EditProductServlet` no longer trusts the client-supplied `currentImageUrl`; the existing image is read from the database (scoped to the owning seller). SVG uploads are rejected (`.jpg/.jpeg/.png/.webp` only) and the image URL is HTML-escaped in `edit-product.jsp`.
- **Open redirect:** `DeleteProductServlet` restricts the post-delete `redirect` parameter to an internal allow-list.
- **Defence-in-depth headers:** uploaded product images are served with `X-Content-Type-Options: nosniff` and a restrictive `Content-Security-Policy`.

## [v1.0.0] - 2026-09-21
### Added
- **Production Deployment Readiness:** Dockerfile multi-stage Tomcat 9 containerization and PostgreSQL support for Render cloud deployment.
- **Mobile Responsive Frontend:** Adaptive UI layout across all customer-facing and seller dashboard pages.
- **Catalog Seeding & Safe Startup:** Automated schema initialization and catalog seeding via `DatabaseContextListener`.
- **Initial Verification:** 113 passing automated test cases across DAOs, Servlets, and business rules.

## [v0.1.0] - 2026-08-28
### Added
- **Core MVP Architecture:** User authentication with BCrypt hashing, session hijacking mitigation, and RBAC filters (`BUYER`, `SELLER`, `ADMIN`).
- **Product Catalog Management:** Multi-part file upload for product photos, image storage abstraction, and soft deactivation.
- **Transactional Shopping Cart:** Cart operations, stock deduction under concurrency, and mock payment checkout flow.
- **Review Engine:** Star ratings and comments storage with one-review-per-buyer constraints.
