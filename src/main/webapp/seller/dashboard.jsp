<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.dao.ProductDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>

<%
    User user = (User) session.getAttribute("user");

    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+first");
        return;
    }

    String role = user.getRole();
    if (role == null || !"SELLER".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Seller+access+required");
        return;
    }

    String userName = user.getName();
    if (userName == null || userName.trim().isEmpty()) {
        userName = (String) session.getAttribute("userName");
    }
    if (userName == null || userName.trim().isEmpty()) {
        userName = "Seller";
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    ProductDAO productDAO = new ProductDAO();
    List<Product> products = productDAO.findBySellerId(user.getId());

    int totalProducts = products != null ? products.size() : 0;
    int totalStock = 0;
    Set<String> categories = new HashSet<>();

    if (products != null) {
        for (Product p : products) {
            totalStock += p.getQuantity();
            if (p.getCategory() != null && !p.getCategory().trim().isEmpty()) {
                categories.add(p.getCategory().trim());
            }
        }
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Seller Dashboard - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260828_3">
</head>
<body>

    <!-- Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/seller/dashboard.jsp" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/seller/dashboard.jsp" class="nav-link active">Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/seller/add-product.jsp" class="nav-link">Add Product</a></li>
                <li><a href="${pageContext.request.contextPath}/seller/products.jsp" class="nav-link">My Products</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="seller-page">
        <div class="seller-container">

            <!-- Success / Error Notifications -->
            <% if (success != null && !success.trim().isEmpty()) {
                String safeSuccess = success.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            %>
                <div class="message success"><%= safeSuccess %></div>
            <% } %>

            <% if (error != null && !error.trim().isEmpty()) {
                String safeError = error.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            %>
                <div class="message error"><%= safeError %></div>
            <% } %>

            <!-- Seller Header -->
            <section class="seller-header">
                <div class="seller-introduction">
                    <span class="eyebrow">SELLER PANEL</span>
                    <h1>Welcome, <%= userName %></h1>
                    <p>Manage your products, monitor inventory stock, and grow your marketplace sales.</p>
                </div>
                <div class="seller-role-badge">
                    ROLE: <%= role %>
                </div>
            </section>

            <!-- Stats Overview Cards -->
            <div class="seller-stats-grid">
                <div class="seller-stat-card">
                    <div class="seller-stat-label">Total Products</div>
                    <div class="seller-stat-value"><%= totalProducts %></div>
                    <div class="seller-stat-hint">Active listings in catalog</div>
                </div>

                <div class="seller-stat-card">
                    <div class="seller-stat-label">Total Stock Units</div>
                    <div class="seller-stat-value"><%= totalStock %></div>
                    <div class="seller-stat-hint">Units currently in inventory</div>
                </div>

                <div class="seller-stat-card">
                    <div class="seller-stat-label">Active Categories</div>
                    <div class="seller-stat-value"><%= categories.size() %></div>
                    <div class="seller-stat-hint">Distinct product types listed</div>
                </div>
            </div>

            <!-- Product Section Header & Quick Add Button -->
            <section class="seller-section">
                <div class="section-heading">
                    <div>
                        <span class="eyebrow">INVENTORY & LISTINGS</span>
                        <h2>Manage Products</h2>
                        <p>Create new listings with photos, track inventory levels, or update items.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/seller/add-product.jsp" class="seller-primary-button">
                        + Add New Product
                    </a>
                </div>

                <!-- Products Table / List View -->
                <div class="products-panel">
                    <div class="products-panel-header">
                        <div>
                            <h3>Your Product Catalog</h3>
                            <p><%= totalProducts > 0 ? "Showing " + totalProducts + " item" + (totalProducts > 1 ? "s" : "") : "No items listed yet" %></p>
                        </div>
                        <% if (totalProducts > 0) { %>
                            <a href="${pageContext.request.contextPath}/seller/products.jsp" class="seller-secondary-button">
                                View Full Inventory
                            </a>
                        <% } %>
                    </div>

                    <% if (products == null || products.isEmpty()) { %>
                        <div class="empty-state">
                            <h3>No Products Added Yet</h3>
                            <p>You haven't listed any items in your seller inventory yet. Start by adding your first product with photo!</p>
                            <a href="${pageContext.request.contextPath}/seller/add-product.jsp" class="seller-primary-button">
                                Add Your First Product
                            </a>
                        </div>
                    <% } else { %>
                        <div class="products-table-wrapper">
                            <table class="products-table">
                                <thead>
                                    <tr>
                                        <th>Photo</th>
                                        <th>ID</th>
                                        <th>Product Name</th>
                                        <th>Category</th>
                                        <th>Price</th>
                                        <th>Stock</th>
                                        <th style="text-align: right;">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        int displayLimit = Math.min(products.size(), 5);
                                        for (int i = 0; i < displayLimit; i++) {
                                            Product p = products.get(i);
                                            String pImg = "default-product.svg";
                                            try {
                                                if (p.getImageUrl() != null && !p.getImageUrl().trim().isEmpty()) {
                                                    pImg = p.getImageUrl().trim();
                                                }
                                            } catch (Throwable t) {
                                                pImg = "default-product.svg";
                                            }
                                            String imgSrc = pImg.startsWith("default-") ? request.getContextPath() + "/images/" + pImg : request.getContextPath() + "/images/products/" + pImg;
                                    %>
                                        <tr>
                                            <td style="width: 50px;">
                                                <img src="<%= imgSrc %>" 
                                                     alt="<%= p.getName() %>" 
                                                     class="product-thumb"
                                                     onerror="this.src='${pageContext.request.contextPath}/images/default-product.svg';">
                                            </td>
                                            <td>#<%= p.getId() %></td>
                                            <td><strong><%= p.getName() %></strong></td>
                                            <td><span class="category-chip"><%= p.getCategory() %></span></td>
                                            <td><strong>₹<%= p.getPrice() %></strong></td>
                                            <td><%= p.getQuantity() %> units</td>
                                            <td style="text-align: right;">
                                                <div class="table-actions" style="justify-content: flex-end;">
                                                    <a href="${pageContext.request.contextPath}/seller/edit-product?id=<%= p.getId() %>" class="action-btn">
                                                        Edit
                                                    </a>
                                                    <form action="${pageContext.request.contextPath}/seller/delete-product" method="post" style="display: inline;" onsubmit="return confirm('Are you sure you want to delete this product?');">
                                                        <input type="hidden" name="id" value="<%= p.getId() %>">
                                                        <input type="hidden" name="redirect" value="/seller/dashboard.jsp">
                                                        <button type="submit" class="action-btn delete-btn">
                                                            Delete
                                                        </button>
                                                    </form>
                                                </div>
                                            </td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    <% } %>
                </div>
            </section>

            <!-- Seller Information & Workflow Feature Cards -->
            <section class="seller-section">
                <div class="section-heading">
                    <div>
                        <span class="eyebrow">SELLER WORKFLOW</span>
                        <h2>Platform Features</h2>
                        <p>Everything you need to sell products effectively on RashikMart.</p>
                    </div>
                </div>

                <div class="seller-feature-grid">
                    <div class="seller-feature-card">
                        <span class="feature-number">01</span>
                        <h3>Photo & Product Listings</h3>
                        <p>Upload high quality product photos, set categories, custom pricing, and inventory quantities.</p>
                    </div>

                    <div class="seller-feature-card">
                        <span class="feature-number">02</span>
                        <h3>Complete CRUD Control</h3>
                        <p>Create, view, update details and photos, or remove discontinued inventory in real-time.</p>
                    </div>

                    <div class="seller-feature-card">
                        <span class="feature-number">03</span>
                        <h3>Marketplace Exposure</h3>
                        <p>Your listings and photos become instantly visible to buyers across RashikMart.</p>
                    </div>
                </div>
            </section>

        </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <p>© 2026 RashikMart. All rights reserved.</p>
    </footer>

</body>
</html>