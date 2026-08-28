<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.dao.ProductDAO" %>
<%@ page import="java.util.List" %>

<%
    User user = (User) session.getAttribute("user");

    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+first");
        return;
    }

    String userName = user.getName();
    String role = user.getRole();

    if (role == null || !"SELLER".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Seller+access+required");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    ProductDAO productDAO = new ProductDAO();
    List<Product> products = productDAO.findBySellerId(user.getId());
    int productCount = (products != null) ? products.size() : 0;

    int totalStock = 0;
    if (products != null) {
        for (Product p : products) {
            totalStock += p.getQuantity();
        }
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Seller Dashboard - RashikMart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 18px;
            margin: 36px 0 10px;
        }

        .stat-card {
            background: #ffffff;
            border: 1px solid #d5d5d5;
            border-radius: 8px;
            padding: 22px 24px;
            transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
        }

        .stat-card:hover {
            border-color: #000000;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
            transform: translateY(-2px);
        }

        .stat-label {
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 2px;
            color: #777777;
            margin-bottom: 10px;
        }

        .stat-value {
            font-size: 32px;
            font-weight: 700;
            letter-spacing: -1px;
            color: #000000;
            line-height: 1;
        }

        .stat-hint {
            margin-top: 8px;
            font-size: 13px;
            color: #666666;
        }

        .header-row {
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 24px;
            flex-wrap: wrap;
        }

        .status-pill {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 8px 14px;
            border: 1px solid #000000;
            border-radius: 999px;
            background: #000000;
            color: #ffffff;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 1.5px;
            white-space: nowrap;
        }

        .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #7CFF6B;
        }

        .products-panel {
            margin-top: 40px;
            background: #ffffff;
            border: 1px solid #d5d5d5;
            border-radius: 10px;
            overflow: hidden;
        }

        .products-panel-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            padding: 20px 24px;
            border-bottom: 1px solid #e8e8e8;
            background: #fafafa;
        }

        .products-panel-header h3 {
            font-size: 18px;
            color: #000000;
            letter-spacing: -0.3px;
        }

        .products-panel-header p {
            margin-top: 4px;
            font-size: 13px;
            color: #666666;
        }

        .products-table {
            width: 100%;
            border-collapse: collapse;
        }

        .products-table th {
            text-align: left;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 1.5px;
            color: #666666;
            background: #ffffff;
            padding: 14px 20px;
            border-bottom: 1px solid #ececec;
        }

        .products-table td {
            padding: 16px 20px;
            border-bottom: 1px solid #f0f0f0;
            font-size: 14px;
            color: #222222;
            vertical-align: middle;
        }

        .products-table tr:last-child td {
            border-bottom: none;
        }

        .products-table tbody tr:hover {
            background: #fafafa;
        }

        .product-name {
            font-weight: 700;
            color: #000000;
        }

        .category-chip {
            display: inline-block;
            padding: 5px 10px;
            border: 1px solid #d0d0d0;
            border-radius: 999px;
            font-size: 12px;
            color: #333333;
            background: #f7f7f7;
        }

        .empty-state {
            text-align: center;
            padding: 48px 24px;
        }

        .empty-state h3 {
            font-size: 20px;
            margin-bottom: 8px;
            color: #000000;
        }

        .empty-state p {
            font-size: 14px;
            color: #666666;
            margin-bottom: 22px;
            line-height: 1.6;
        }

        .section-top {
            display: flex;
            align-items: flex-end;
            justify-content: space-between;
            gap: 20px;
            flex-wrap: wrap;
            margin-bottom: 22px;
        }

        .muted-link {
            font-size: 14px;
            color: #000000;
            font-weight: 600;
            text-decoration: none;
            border-bottom: 1px solid #000000;
            padding-bottom: 2px;
        }

        .muted-link:hover {
            opacity: 0.7;
        }

        @media (max-width: 800px) {
            .stats-grid {
                grid-template-columns: 1fr;
            }

            .products-panel-header {
                flex-direction: column;
                align-items: flex-start;
            }

            .products-table th,
            .products-table td {
                padding: 12px 14px;
            }
        }
    </style>
</head>
<body class="dashboard-body">

<header class="dashboard-navbar">
    <a href="<%= request.getContextPath() %>/seller/dashboard.jsp" class="dashboard-brand">
        RashikMart
    </a>

    <nav class="dashboard-nav">
        <a href="<%= request.getContextPath() %>/seller/dashboard.jsp" class="dashboard-nav-link active">
            Dashboard
        </a>
        <a href="<%= request.getContextPath() %>/seller/add-product.jsp" class="dashboard-nav-link">
            Add Product
        </a>
        <a href="<%= request.getContextPath() %>/seller/products.jsp" class="dashboard-nav-link">
            My Products
        </a>
        <a href="<%= request.getContextPath() %>/logout" class="dashboard-nav-link">
            Logout
        </a>
    </nav>
</header>

<main class="dashboard-main">

    <div class="header-row">
        <div class="dashboard-heading">
            <div class="dashboard-eyebrow">SELLER PANEL</div>
            <h1>Welcome, <%= userName %></h1>
            <p class="dashboard-subtitle">
                Manage your agricultural products, pricing and stock for the marketplace.
            </p>
            <div class="seller-role">ACCOUNT TYPE · SELLER</div>
        </div>

        <div class="status-pill">
            <span class="status-dot"></span>
            ACTIVE
        </div>
    </div>

    <% if (success != null && !success.trim().isEmpty()) { %>
        <div class="message success" style="margin-top: 22px;"><%= success %></div>
    <% } %>
    <% if (error != null && !error.trim().isEmpty()) { %>
        <div class="message error" style="margin-top: 22px;"><%= error %></div>
    <% } %>

    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-label">TOTAL PRODUCTS</div>
            <div class="stat-value"><%= productCount %></div>
            <div class="stat-hint">Listings in your catalog</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">TOTAL STOCK</div>
            <div class="stat-value"><%= totalStock %></div>
            <div class="stat-hint">Units available for buyers</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">MARKETPLACE</div>
            <div class="stat-value" style="font-size: 22px; padding-top: 6px;">Live</div>
            <div class="stat-hint">Visible to buyers now</div>
        </div>
    </div>

    <section class="product-management">
        <div class="section-top">
            <div>
                <div class="dashboard-eyebrow">PRODUCT MANAGEMENT</div>
                <h2>Start selling on RashikMart</h2>
                <p class="section-description" style="margin-bottom: 0;">
                    Add agricultural products, set pricing and keep stock updated.
                </p>
            </div>
            <a href="<%= request.getContextPath() %>/seller/add-product.jsp" class="dashboard-primary-button">
                ADD NEW PRODUCT
            </a>
        </div>

        <div class="dashboard-feature-grid">
            <article class="dashboard-feature-card">
                <div class="feature-number">01</div>
                <h3>Add Products</h3>
                <p>Create product listings with name, category, price and quantity.</p>
            </article>

            <article class="dashboard-feature-card">
                <div class="feature-number">02</div>
                <h3>Manage Stock</h3>
                <p>Keep track of the quantity available for buyers at any time.</p>
            </article>

            <article class="dashboard-feature-card">
                <div class="feature-number">03</div>
                <h3>Reach Buyers</h3>
                <p>Your products become available in the buyer marketplace instantly.</p>
            </article>
        </div>

        <div class="products-panel">
            <div class="products-panel-header">
                <div>
                    <h3>Your products</h3>
                    <p>
                        <% if (productCount == 0) { %>
                            No listings yet — add your first product to get started.
                        <% } else { %>
                            Showing <%= productCount %> listing<%= productCount == 1 ? "" : "s" %> from your catalog.
                        <% } %>
                    </p>
                </div>
                <% if (productCount > 0) { %>
                    <a href="<%= request.getContextPath() %>/seller/products.jsp" class="muted-link">
                        View all products
                    </a>
                <% } %>
            </div>

            <% if (productCount == 0) { %>
                <div class="empty-state">
                    <h3>No products yet</h3>
                    <p>
                        Add your first agricultural product with name, category,<br>
                        price and stock quantity.
                    </p>
                    <a href="<%= request.getContextPath() %>/seller/add-product.jsp" class="dashboard-primary-button">
                        ADD PRODUCT
                    </a>
                </div>
            <% } else { %>
                <div style="overflow-x: auto;">
                    <table class="products-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>NAME</th>
                                <th>CATEGORY</th>
                                <th>PRICE</th>
                                <th>QTY</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Product p : products) { %>
                                <tr>
                                    <td>#<%= p.getId() %></td>
                                    <td class="product-name"><%= p.getName() %></td>
                                    <td>
                                        <span class="category-chip"><%= p.getCategory() %></span>
                                    </td>
                                    <td>₹<%= p.getPrice() %></td>
                                    <td><%= p.getQuantity() %></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>
        </div>
    </section>

</main>

<footer class="dashboard-footer">
    <p>© 2026 RashikMart. All rights reserved.</p>
</footer>

</body>
</html>