# RashikMart System Diagrams

This directory contains the authoritative architecture, data model, and behavioral diagrams for the RashikMart capstone application, rendered in Mermaid markdown format.

---

## D1: Entity-Relationship (ER) Diagram

File: [`D1_ER_Diagram.mmd`](D1_ER_Diagram.mmd)

```mermaid
erDiagram
    USERS ||--o{ PRODUCTS : "sells"
    USERS ||--o| CART : "owns"
    USERS ||--o{ ORDERS : "places"
    USERS ||--o{ REVIEWS : "writes"

    PRODUCTS ||--o{ CART_ITEMS : "referenced_by"
    PRODUCTS ||--o{ ORDER_ITEMS : "included_in"
    PRODUCTS ||--o{ REVIEWS : "receives"

    CART ||--o{ CART_ITEMS : "contains"
    ORDERS ||--|{ ORDER_ITEMS : "contains"

    USERS {
        int id PK
        string name
        string email UK
        string password
        string role "BUYER | SELLER | ADMIN"
    }

    PRODUCTS {
        int id PK
        int seller_id FK
        string name
        string description
        string category
        decimal price
        int quantity
        string image_url
        boolean active
        timestamp created_at
    }

    CART {
        int id PK
        int buyer_id FK,UK
        timestamp created_at
    }

    CART_ITEMS {
        int id PK
        int cart_id FK
        int product_id FK
        int quantity
    }

    ORDERS {
        int id PK
        int buyer_id FK
        decimal total_amount
        string status "PENDING | CONFIRMED | SHIPPED | DELIVERED"
        timestamp created_at
    }

    ORDER_ITEMS {
        int id PK
        int order_id FK
        int product_id FK
        int quantity
        decimal price
    }

    REVIEWS {
        int id PK
        int product_id FK
        int buyer_id FK
        int rating "1 to 5"
        string review_text
        timestamp created_at
    }
```

---

## D2: Use-Case Diagram

File: [`D2_UseCase_Diagram.mmd`](D2_UseCase_Diagram.mmd)

```mermaid
flowchart TD
    Guest(["Guest Visitor"])
    Buyer(["Registered Buyer"])
    Seller(["Registered Seller"])
    Admin(["System Administrator"])

    subgraph UC_Public["Public / Guest Services"]
        UC_Reg["Register Account (Buyer/Seller)"]
        UC_Login["Login & Authenticate (BCrypt)"]
        UC_BrowsePublic["Browse Public Catalog"]
        UC_ChatGuest["Ask AI Chatbot (FAQ / Shopping Assistant)"]
    end

    subgraph UC_Buyer["Buyer Operations (F1, F4, F5, F6, F8)"]
        UC_BrowseMarket["Browse & Search Marketplace"]
        UC_ManageCart["Manage Cart (Add, Update, Remove)"]
        UC_Checkout["Checkout & Place Order (Mock Payment)"]
        UC_TrackOrders["View Order History & Status Badges"]
        UC_Review["Submit Review (Delivered Orders Only)"]
    end

    subgraph UC_Seller["Seller Operations (F2, F3, F7, O2)"]
        UC_AddProd["Add Product with Photo Upload"]
        UC_EditProd["Edit Product & Stock Level"]
        UC_DeleteProd["Safe Product Delete / Deactivation"]
        UC_SellerOrders["View Incoming Customer Orders"]
        UC_AdvanceOrder["Advance Order Status (Confirmed &rarr; Shipped &rarr; Delivered)"]
    end

    subgraph UC_Admin["Admin Operations (F7, Moderation)"]
        UC_ModProd["Moderate Products (Toggle Active Status)"]
        UC_AdminMetrics["View Platform Total Revenue & Orders"]
        UC_AdminOrders["View All Orders & Update Status"]
    end

    Guest --> UC_Reg
    Guest --> UC_Login
    Guest --> UC_BrowsePublic
    Guest --> UC_ChatGuest

    Buyer --> UC_BrowseMarket
    Buyer --> UC_ManageCart
    Buyer --> UC_Checkout
    Buyer --> UC_TrackOrders
    Buyer --> UC_Review
    Buyer --> UC_ChatGuest

    Seller --> UC_AddProd
    Seller --> UC_EditProd
    Seller --> UC_DeleteProd
    Seller --> UC_SellerOrders
    Seller --> UC_AdvanceOrder

    Admin --> UC_ModProd
    Admin --> UC_AdminMetrics
    Admin --> UC_AdminOrders
```

---

## D3: Place-Order Sequence Diagram

File: [`D3_PlaceOrder_Sequence_Diagram.mmd`](D3_PlaceOrder_Sequence_Diagram.mmd)

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as Buyer Browser
    participant Filter as AuthFilter / RequestIdFilter
    participant Servlet as OrderServlet (doPost)
    participant Csrf as CsrfUtil
    participant DAO as OrderDAO
    participant Pool as HikariCP DataSource
    participant DB as Database (H2 / Postgres)

    Buyer->>Filter: POST /buyer/checkout (with csrfToken & Session)
    Filter->>Filter: Generate X-Request-Id & set MDC
    Filter->>Filter: Verify BUYER role in session
    Filter->>Servlet: forward request

    Servlet->>Csrf: isValid(request)
    Csrf-->>Servlet: true (token match)

    Servlet->>DAO: createOrderFromCart(buyerId)
    activate DAO
    DAO->>Pool: getConnection()
    Pool-->>DAO: Connection (conn)
    DAO->>conn: conn.setAutoCommit(false)

    Note over DAO,DB: 1. Fetch Cart Items
    DAO->>DB: SELECT ci.* FROM cart_items WHERE buyer_id = ?
    DB-->>DAO: List of Cart Items [productId, quantity]

    Note over DAO,DB: 2. Check live stock & compute total
    DAO->>DB: SELECT quantity, price FROM products WHERE id = ?
    DB-->>DAO: Stock Available & Unit Price

    Note over DAO,DB: 3. Insert into orders table
    DAO->>DB: INSERT INTO orders (buyer_id, total_amount, status='CONFIRMED')
    DB-->>DAO: Generated Order ID

    Note over DAO,DB: 4. Insert items & deduct inventory
    loop For each cart item
        DAO->>DB: INSERT INTO order_items (order_id, product_id, quantity, price)
        DAO->>DB: UPDATE products SET quantity = quantity - ? WHERE id = ?
    end

    Note over DAO,DB: 5. Empty buyer cart
    DAO->>DB: DELETE FROM cart_items WHERE cart_id = ?

    DAO->>conn: conn.commit()
    DAO->>conn: conn.close()
    DAO-->>Servlet: Order object (ID, items, status='CONFIRMED')
    deactivate DAO

    Servlet-->>Buyer: HTTP 302 Redirect to /buyer/order-success?orderId={id}
    Buyer->>Servlet: GET /buyer/order-success?orderId={id}
    Servlet-->>Buyer: Render order confirmation page with status badge
```
