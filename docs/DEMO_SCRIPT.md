# RashikMart Live Capstone Presentation & Demo Script

**Presenter:** Rashik  
**Target Duration:** 3–5 Minutes  
**Audience:** Anna University Final Review Panel & Examiners  

---

## 1. Introduction (30 seconds)
> *"Good morning/afternoon, esteemed panel members. I am Rashik, and today I am presenting **RashikMart**, a lightweight, high-performance multi-vendor e-commerce web platform developed strictly per the Anna University R2025 Semester 3 Java Capstone specifications.*  
> *RashikMart is built using core Java 17 Servlets, JSP, JDBC with HikariCP connection pooling, BCrypt password hashing, and containerized deployment for Tomcat 9 and cloud environments. It incorporates dual database support for local H2 and cloud PostgreSQL, full transactional inventory guarantees, role-based security, lifecycle order tracking, and an integrated AI shopping assistant."*

---

## 2. AI Shopping Assistant & Guest Experience (45 seconds)
> *"Let's begin as a new visitor on the landing page.*  
> *Notice our floating AI assistant at the bottom right. Clicking the launcher opens the conversational widget. When I ask, **'How do I place an order?'** or **'What categories are available?'**, the assistant provides instant, context-aware answers.*  
> *The chatbot is implemented via a Strategy pattern: in development or offline mode, it runs against our deterministic MockChatProvider with over 10 FAQ canned responses; in production, it connects to Google Gemini via server-side HTTP calls with automatic failover, per-session rate limiting of 10 messages per minute, and XSS-immune DOM textContent rendering."*

---

## 3. Buyer Journey: Marketplace to Transactional Checkout (60 seconds)
> *"Now I will log in as our demo buyer, `buyer@rashikmart.com`.*  
> *Upon authentication, the application invokes `request.changeSessionId()` to defend against session fixation attacks.*  
> *Navigating to the Marketplace, we see all available products with live stock quantities and categorized filtering.*  
> *Let's add 2 units of 'Basmati Rice' to our cart. Visiting `/buyer/cart`, the subtotal is calculated.*  
> *When I click 'Proceed to Checkout', the order is created within a single ACID JDBC transaction with `autoCommit=false`. Stock is deducted, the cart is emptied atomically, and an order with status `CONFIRMED` is generated. If stock were insufficient, the transaction would roll back completely with zero overselling."*

---

## 4. Seller Fulfillment & Order Lifecycle Workflow (45 seconds)
> *"Now let's switch to our seller, `seller@rashikmart.com`.*  
> *On the Customer Orders page, the seller views incoming orders with strict multi-tenant isolation—sellers can only view orders containing their own products.*  
> *Per spec requirement O2, orders advance through a strict state machine: `CONFIRMED` &rarr; `SHIPPED` &rarr; `DELIVERED`. Clicking 'Mark Shipped' updates the state. If an unauthorized client attempts an invalid transition like jumping directly to `DELIVERED`, our backend rejects it with an HTTP 409 Conflict."*  
> *Clicking 'Mark Delivered' finalizes fulfillment."*

---

## 5. Verified Review System & Administrator Oversight (45 seconds)
> *"Returning to our buyer view on the product details page: because the buyer now holds a completed, `DELIVERED` order, the review form unlocks per spec feature F8.*  
> *The buyer submits a 5-star rating and review. The platform updates average ratings and enforces a composite unique constraint preventing duplicate submissions by the same buyer.*  
> *Lastly, logging in as `admin@rashikmart.com`, the administrator monitors platform-wide metrics and can moderate listings by toggling product active status instantly."*

---

## 6. Architecture, Tests & Conclusion (30 seconds)
> *"Behind the scenes, RashikMart includes 159 automated test cases across DAOs, Servlets, and business rules, all passing cleanly.*  
> *The system features request ID correlation (`X-Request-Id`) across structured Logback logs and a live health check endpoint at `/api/v1/health`.*  
> *Thank you, and I look forward to your questions."*
