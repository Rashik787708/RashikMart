<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.dao.UserDAO" %>
<%@ page import="com.rashik.rashikmart.dao.ProductDAO" %>
<%@ page import="java.util.List" %>

<%
    User user = (User) session.getAttribute("user");

    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+first");
        return;
    }

    String role = user.getRole();
    if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Admin+access+required");
        return;
    }

    String userName = user.getName();
    if (userName == null || userName.trim().isEmpty()) {
        userName = "Administrator";
    }

    UserDAO userDAO = new UserDAO();
    ProductDAO productDAO = new ProductDAO();

    List<User> allUsers = userDAO.findAll();
    List<Product> allProducts = productDAO.findAll();

    int totalUsers = allUsers != null ? allUsers.size() : 0;
    int totalSellers = 0;
    int totalBuyers = 0;

    if (allUsers != null) {
        for (User u : allUsers) {
            if ("SELLER".equalsIgnoreCase(u.getRole())) {
                totalSellers++;
            } else if ("BUYER".equalsIgnoreCase(u.getRole())) {
                totalBuyers++;
            }
        }
    }

    int totalProducts = allProducts != null ? allProducts.size() : 0;
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260828_3">
</head>
<body>

    <!-- Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="nav-link active">Admin Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="seller-page">
        <div class="seller-container">

            <section class="seller-header">
                <div class="seller-introduction">
                    <span class="eyebrow">ADMINISTRATOR CONTROL PANEL</span>
                    <h1>Welcome, <%= userName %></h1>
                    <p>Global oversight of registered users, seller catalogs, and marketplace listings.</p>
                </div>
                <div class="seller-role-badge">
                    ROLE: <%= role %>
                </div>
            </section>

            <!-- Stats Overview -->
            <div class="seller-stats-grid">
                <div class="seller-stat-card">
                    <div class="seller-stat-label">Total Users</div>
                    <div class="seller-stat-value"><%= totalUsers %></div>
                    <div class="seller-stat-hint"><%= totalSellers %> Sellers &bull; <%= totalBuyers %> Buyers</div>
                </div>

                <div class="seller-stat-card">
                    <div class="seller-stat-label">Marketplace Products</div>
                    <div class="seller-stat-value"><%= totalProducts %></div>
                    <div class="seller-stat-hint">Active items across all sellers</div>
                </div>

                <div class="seller-stat-card">
                    <div class="seller-stat-label">System Status</div>
                    <div class="seller-stat-value" style="font-size: 1.8rem; padding-top: 5px;">ONLINE</div>
                    <div class="seller-stat-hint">H2 Database connection verified</div>
                </div>
            </div>

            <!-- Users Section -->
            <section class="seller-section">
                <div class="section-heading">
                    <div>
                        <span class="eyebrow">USER MANAGEMENT</span>
                        <h2>Registered Accounts</h2>
                        <p>All buyers, sellers, and administrators registered in the system.</p>
                    </div>
                </div>

                <div class="products-panel">
                    <div class="products-table-wrapper">
                        <table class="products-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Full Name</th>
                                    <th>Email Address</th>
                                    <th>Account Role</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% if (allUsers != null) {
                                    for (User u : allUsers) {
                                %>
                                    <tr>
                                        <td>#<%= u.getId() %></td>
                                        <td><strong><%= u.getName() %></strong></td>
                                        <td><%= u.getEmail() %></td>
                                        <td>
                                            <span class="category-chip" style="<%= "ADMIN".equalsIgnoreCase(u.getRole()) ? "background: #000; color: #fff;" : "" %>">
                                                <%= u.getRole() %>
                                            </span>
                                        </td>
                                    </tr>
                                <% }} %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>

            <!-- Products Section -->
            <section class="seller-section">
                <div class="section-heading">
                    <div>
                        <span class="eyebrow">ALL LISTINGS</span>
                        <h2>Marketplace Products</h2>
                        <p>All items created by sellers across the platform.</p>
                    </div>
                </div>

                <div class="products-panel">
                    <div class="products-table-wrapper">
                        <table class="products-table">
                            <thead>
                                <tr>
                                    <th>Photo</th>
                                    <th>ID</th>
                                    <th>Seller ID</th>
                                    <th>Product Name</th>
                                    <th>Category</th>
                                    <th>Price</th>
                                    <th>Stock</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% if (allProducts == null || allProducts.isEmpty()) { %>
                                    <tr>
                                        <td colspan="7" style="text-align: center; padding: 2rem; color: #777;">
                                            No products listed in the marketplace yet.
                                        </td>
                                    </tr>
                                <% } else {
                                    for (Product p : allProducts) {
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
                                        <td>Seller #<%= p.getSellerId() %></td>
                                        <td><strong><%= p.getName() %></strong></td>
                                        <td><span class="category-chip"><%= p.getCategory() %></span></td>
                                        <td><strong>₹<%= p.getPrice() %></strong></td>
                                        <td><%= p.getQuantity() %> units</td>
                                    </tr>
                                <% }} %>
                            </tbody>
                        </table>
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