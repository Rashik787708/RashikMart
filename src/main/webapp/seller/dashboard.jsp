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
        for (Product p : products) totalStock += p.getQuantity();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Seller Dashboard - RashikMart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/seller.css">
</head>
<body class="seller-body">

<header class="seller-navbar">
    <a href="<%= request.getContextPath() %>/seller/dashboard.jsp" class="seller-brand">RashikMart</a>
    <nav class="seller-nav">
        <a href="<%= request.getContextPath() %>/seller/dashboard.jsp" class="active">Dashboard</a>
        <a href="<%= request.getContextPath() %>/seller/add-product.jsp">Add Product</a>
        <a href="<%= request.getContextPath() %>/seller/products.jsp">My Products</a>
        <a href="<%= request.getContextPath() %>/logout">Logout</a>
    </nav>
</header>

<main class="seller-main">

    <div class="seller-header-row">
        <div>
            <div class="seller-eyebrow">SELLER PANEL</div>
            <h1>Welcome, <%= userName %></h1>
            <p class="seller-subtitle">Manage your agricultural products, pricing and stock for the marketplace.</p>
            <div class="seller-role-badge">ACCOUNT TYPE · SELLER</div>
        </div>
        <div class="status-pill"><span class="status-dot"></span> ACTIVE</div>
    </div>

    <% if (success != null && !success.trim().isEmpty()) { %>
        <div class="message success"><%= success %></div>
    <% } %>
    <% if (error != null && !error.trim().isEmpty()) { %>
        <div class="message error"><%= error %></div>
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
            <div class="stat-value" style="font-size:22px;padding-top:6px;">Live</div>
            <div class="stat-hint">Visible to buyers now</div>
        </div>
    </div>

    <section class="product-section">
        <div class="section-top">
            <div>
                <div class="seller-eyebrow">PRODUCT MANAGEMENT</div>
                <h2>Start selling on RashikMart</h2>
                <p class="section-desc">Add agricultural products, set pricing and keep stock updated.</p>
            </div>
            <a href="<%= request.getContextPath() %>/seller/add-product.jsp" class="btn-primary">ADD NEW PRODUCT</a>
        </div>

        <div class="feature-grid">
            <article class="feature-card">
                <div class="feature-number">01</div>
                <h3>Add Products</h3>
                <p>Create product listings with name, category, price and quantity.</p>
            </article>
            <article class="feature-card">
                <div class="feature-number">02</div>
                <h3>Manage Stock</h3>
                <p>Keep track of the quantity available for buyers at any time.</p>
            </article>
            <article class="feature-card">
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
                    <a href="<%= request.getContextPath() %>/seller/products.jsp" class="muted-link">View all products</a>
                <% } %>
            </div>

            <% if (productCount == 0) { %>
                <div class="empty-state">
                    <h3>No products yet</h3>
                    <p>Add your first agricultural product with name, category, price and stock quantity.</p>
                    <a href="<%=