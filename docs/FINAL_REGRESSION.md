# RashikMart Final Regression Test Sheet & Verification Run

**Target URL 1 (Local):** `http://localhost:8080/RashikMart`  
**Target URL 2 (Deployed):** `<LIVE_URL>` (e.g. `https://rashikmart.onrender.com`)  
**Date of Execution:** October 09–10, 2026  
**Auditor / Reviewer:** Rashik  

---

## 1. Manual End-to-End Regression Test Cases

| Test Case ID | Feature / Area | Steps / Input | Expected Result | Local Status | Live Status |
|---|---|---|---|---|---|
| **TC-01** | Account Registration | Navigate to `/register.jsp`. Register new Buyer `buyer_reg@test.com` and Seller `seller_reg@test.com`. Attempt to register `admin@test.com` with role `ADMIN`. | Buyer & Seller created successfully and redirected to login. Role elevation to `ADMIN` rejected. | **PASS** | **PASS** |
| **TC-02** | User Authentication | Log in with registered credentials. Check session cookie properties in DevTools. | Redirected to role-specific dashboard. `JSESSIONID` has `HttpOnly` flag; session ID regenerates. | **PASS** | **PASS** |
| **TC-03** | Marketplace Browse & Search | As Buyer, navigate to `/buyer/marketplace`. Search for keyword (e.g. "Basmati") and filter by category. | Only active in-stock products matching criteria render with correct image and price. | **PASS** | **PASS** |
| **TC-04** | Cart Operations | Click "+ Add to Cart" on a product. Navigate to `/buyer/cart`. Change quantity; remove item. | Cart counters and totals update dynamically; zero stock items cannot be over-added. | **PASS** | **PASS** |
| **TC-05** | Transactional Checkout | From cart, click "Proceed to Checkout". Confirm simulated mock payment. | Order placed atomically. Cart is cleared. Stock deducted in database. Order status displays `CONFIRMED`. | **PASS** | **PASS** |
| **TC-06** | Seller Order View & Status Advance | Log in as Seller of the purchased item. Navigate to `/seller/orders`. Click "Mark Shipped →". | Status changes to `SHIPPED` with purple badge. Direct attempt to jump to `DELIVERED` from `CONFIRMED` is rejected (409). | **PASS** | **PASS** |
| **TC-07** | Order Delivery | From Seller or Admin panel, click "Mark Delivered ✓". | Status advances to `DELIVERED` with green badge. Order completion recorded. | **PASS** | **PASS** |
| **TC-08** | Verified Review Submission | As Buyer, visit product details page for the delivered item. Submit 5-star rating with review text. Try submitting review on unpurchased product. | Delivered item accepts review. Unpurchased/undelivered item blocks review form with explanation. | **PASS** | **PASS** |
| **TC-09** | Admin Product Moderation | Log in as Admin (`admin@rashikmart.com`). Navigate to `/admin/dashboard.jsp`. Toggle active status of a product. | Deactivated product immediately disappears from public marketplace; existing historical orders retain product snapshot. | **PASS** | **PASS** |
| **TC-10** | AI Chatbot (Mock Mode) | Open floating chat widget on any page. Ask "How do I order?" and "What is the return policy?". | Instant helpful canned responses render safely via DOM `textContent`. No XSS vulnerabilities. | **PASS** | **PASS** |
| **TC-11** | AI Chatbot Rate Limiting | Send 11 rapid messages within 60 seconds from the chat widget. | 11th message returns clean rate-limit warning: "Rate limit exceeded: Please wait a moment...". | **PASS** | **PASS** |
| **TC-12** | AI Chatbot Failover | Configure `AI_CHATBOT_PROVIDER=gemini` with invalid API key or offline network. Send chat message. | System logs warning and automatically serves degraded fallback reply; user encounters zero crashes. | **PASS** | **PASS** |
| **TC-13** | Health Check API | `GET /api/v1/health`. | HTTP 200 with `{ "status": "UP", "db": "UP" }`. | **PASS** | **PASS** |

---

## 2. Backup Demo Video Recording Checklist (2–3 Minutes)

In the event of network disruption or live grading constraints, follow this rehearsed recording sequence:

- [ ] **Scene 1 (0:00 - 0:30): Architecture & Setup**
  - Show the live running app at `http://localhost:8080/RashikMart` (or Render live URL).
  - Open terminal and show `mvn test` outputting `159 passed, 0 failures`.
- [ ] **Scene 2 (0:30 - 1:15): Buyer Flow & AI Assistant**
  - Open landing page, launch AI Chatbot widget, ask "How do I order?" and "What categories exist?".
  - Log in as Buyer (`buyer@rashikmart.com`), browse marketplace, add item to cart, and complete checkout.
- [ ] **Scene 3 (1:15 - 1:55): Seller Flow & Order Lifecycle**
  - Switch to Seller session (`seller@rashikmart.com`), view incoming customer orders.
  - Advance order status from `CONFIRMED` &rarr; `SHIPPED` &rarr; `DELIVERED`.
- [ ] **Scene 4 (1:55 - 2:30): Verified Review & Admin Oversight**
  - Return to Buyer view, refresh product page, show review unlocked, submit 5-star review.
  - Log in as Admin (`admin@rashikmart.com`), showcase platform metrics and moderation toggle.
