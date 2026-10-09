# Engineering Retrospective (Sprint Log)

A reflective, honest retrospective on development sprints throughout the RashikMart capstone timeline.

## Sprint 1 (Aug 01 – Aug 15): Foundations & Auth
- **Focus:** Project scaffolding, Maven build configuration, database connectivity, and BCrypt user authentication.
- **Retrospective:** Building solid session-fixation defense (`changeSessionId`) early prevented auth bugs later, but manually writing raw SQL highlighted the urgent need for strict PreparedStatement discipline.

## Sprint 2 (Aug 16 – Aug 31): Product Catalog & File Uploads
- **Focus:** Seller inventory management, image file uploading, and soft-delete safeguards.
- **Retrospective:** Separating user uploads from packaged WAR assets into external storage was initially tricky on Windows vs Linux, but essential for zero data loss on restarts.

## Sprint 3 (Sep 01 – Sep 15): Shopping Cart, Checkout & Orders
- **Focus:** Transactional shopping cart, stock deduction, and order isolation between buyers and sellers.
- **Retrospective:** Wrapping cart-to-order conversion in explicit JDBC transactions with `connection.setAutoCommit(false)` proved vital to eliminate race conditions and stock discrepancies.

## Sprint 4 (Sep 16 – Sep 21): Deployment, Mobile Responsiveness & Polish
- **Focus:** PostgreSQL support on Render, Docker containerization, mobile CSS media queries, and catalog seeding.
- **Retrospective:** Supporting both local embedded H2 and cloud PostgreSQL required careful SQL dialect harmonization, saving hours during cloud deployments.

## Sprint 5 (Sep 22 – Oct 09): AI Chatbot, Order State Machine & Review Hardening
- **Focus:** Gemini AI assistant with offline mock fallback, order lifecycle state machine (`PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED`), delivered-order review prerequisites, and SLF4J MDC correlation.
- **Retrospective:** Decoupling Gemini behind a `ChatProvider` strategy ensured 100% test reliability and zero downtime even when external AI API keys or network quotas fluctuate.
