<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.model.Order" %>
<%@ page import="com.rashik.rashikmart.model.OrderItem" %>
<%@ page import="com.rashik.rashikmart.dao.UserDAO" %>
<%@ page import="com.rashik.rashikmart.dao.ProductDAO" %>
<%@ page import="com.rashik.rashikmart.dao.OrderDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.rashik.rashikmart.util.HtmlUtil" %>
<%@ page import="com.rashik.rashikmart.util.CsrfUtil" %>

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

    CsrfUtil.getOrCreateToken(session);

    String userName = user.getName();
    if (userName == null || userName.trim().isEmpty()) {
        userName = "Administrator";
    }

    UserDAO userDAO = new UserDAO();
    ProductDAO productDAO = new ProductDAO();
    OrderDAO orderDAO = new OrderDAO();

    List<User> allUsers = userDAO.findAll();
    List<Product> allProducts = productDAO.findAll();
    List<Order> allOrders = orderDAO.findAllOrders();

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
    BigDecimal platformRevenue = orderDAO.getPlatformTotalRevenue();
    int platformOrdersCount = orderDAO.getPlatformTotalOrders();

    String success = request.getParameter("success");
    String error = request.getParameter("error");
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

            <!-- Success / Error Notifications -->
            <% if (success != null && !success.trim().isEmpty()) {
                String safeSuccess = HtmlUtil.escape(success);
            %>
                <div class="message success"><%= safeSuccess %></div>
            <% } %>

            <% if (error != null && !error.trim().isEmpty()) {
                String safeError = HtmlUtil.escape(error);
            %>
                <div class="message error"><%= safeError %></div>
            <% } %>

            <section class="seller-header">
                <div class="seller-introduction">
                    <span class="eyebrow">ADMINISTRATOR CONTROL PANEL</span>
                    <h1>Welcome, <%= HtmlUtil.escape(userName) %></h1>
                    <p>Global oversight of registered users, seller catalogs, orders, and marketplace platform metrics.</p>
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
                    <div class="seller-stat-hint">Catalog items across all sellers</div>
                </div>

                <div class="seller-stat-card">
                    <div class="seller-stat-label">Platform Orders</div>
                    <div class="seller-stat-value"><%= platformOrdersCount %></div>
                    <div class="seller-stat-hint">Total purchases completed</div>
                </div>

                <div class="seller-stat-card">
                    <div class="seller-stat-label">Platform Revenue</div>
                    <div class="seller-stat-value">₹<%= platformRevenue %></div>
                    <div class="seller-stat-hint">Gross transaction volume</div>
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
                                        <td><strong><%= HtmlUtil.escape(u.getName()) %></strong></td>
                                        <td><%= HtmlUtil.escape(u.getEmail()) %></td>
                                        <td>
                                            <span class="category-chip" style="<%= "ADMIN".equalsIgnoreCase(u.getRole()) ? "background: #000; color: #fff;" : "" %>">
                                                <%= HtmlUtil.escape(u.getRole()) %>
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
                                    <th>Status</th>
                                    <th style="text-align: right;">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% if (allProducts == null || allProducts.isEmpty()) { %>
                                    <tr>
                                        <td colspan="9" style="text-align: center; padding: 2rem; color: #777;">
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
                                                 alt="<%= HtmlUtil.escape(p.getName()) %>" 
                                                 class="product-thumb"
                                                 onerror="this.src='${pageContext.request.contextPath}/images/default-product.svg';">
                                        </td>
                                        <td>#<%= p.getId() %></td>
                                        <td>Seller #<%= p.getSellerId() %></td>
                                        <td><strong><%= HtmlUtil.escape(p.getName()) %></strong></td>
                                        <td><span class="category-chip"><%= HtmlUtil.escape(p.getCategory()) %></span></td>
                                        <td><strong>₹<%= p.getPrice() %></strong></td>
                                        <td><%= p.getQuantity() %> units</td>
                                        <td>
                                            <span class="status-badge" style="<%= p.isActive() ? "background: #2e7d32; color: #fff;" : "background: #c62828; color: #fff;" %>">
                                                <%= p.isActive() ? "ACTIVE" : "INACTIVE" %>
                                            </span>
                                        </td>
                                        <td style="text-align: right;">
                                            <form action="${pageContext.request.contextPath}/admin/product-status" method="post" style="display: inline;">
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                <input type="hidden" name="id" value="<%= p.getId() %>">
                                                <input type="hidden" name="active" value="<%= !p.isActive() %>">
                                                <button type="submit" class="action-btn <%= p.isActive() ? "delete-btn" : "" %>" style="padding: 4px 8px; font-size: 0.72rem;">
                                                    <%= p.isActive() ? "Deactivate" : "Activate" %>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                <% }} %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>

            <!-- Orders Oversight Section -->
            <section class="seller-section">
                <div class="section-heading">
                    <div>
                        <span class="eyebrow">ORDER OVERSIGHT</span>
                        <h2>Platform Orders</h2>
                        <p>All purchase orders placed by buyers across all sellers on the platform.</p>
                    </div>
                </div>

                <div class="products-panel">
                    <div class="products-table-wrapper">
                        <table class="products-table">
                            <thead>
                                <tr>
                                    <th>Order #</th>
                                    <th>Placed Date</th>
                                    <th>Buyer ID</th>
                                    <th>Items Ordered</th>
                                    <th>Total Amount</th>
                                    <th style="text-align: right;">Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% if (allOrders == null || allOrders.isEmpty()) { %>
                                    <tr>
                                        <td colspan="6" style="text-align: center; padding: 2rem; color: #777;">
                                            No orders placed on the platform yet.
                                        </td>
                                    </tr>
                                <% } else {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
                                    for (Order o : allOrders) {
                                %>
                                    <tr>
                                        <td><strong>#<%= o.getId() %></strong></td>
                                        <td><%= o.getCreatedAt() != null ? sdf.format(o.getCreatedAt()) : "Recently" %></td>
                                        <td>Buyer #<%= o.getBuyerId() %></td>
                                        <td><%= o.getItems() != null ? o.getItems().size() : 0 %> line items (<%= o.getTotalQuantity() %> units)</td>
                                        <td><strong>₹<%= o.getTotalAmount() %></strong></td>
                                        <td style="text-align: right;">
                                            <span class="category-chip" style="background: #000; color: #fff; padding: 2px 8px;">
                                                <%= HtmlUtil.escape(o.getStatus()) %>
                                            </span>
                                        </td>
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