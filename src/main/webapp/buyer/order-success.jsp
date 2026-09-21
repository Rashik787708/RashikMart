<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Order" %>
<%@ page import="com.rashik.rashikmart.model.OrderItem" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.dao.OrderDAO" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.rashik.rashikmart.util.HtmlUtil" %>

<%
    User user = (User) session.getAttribute("user");

    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+first");
        return;
    }

    String role = user.getRole();
    if (role == null || !"BUYER".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Buyer+access+required");
        return;
    }

    String orderIdText = request.getParameter("orderId");
    Order order = null;

    if (orderIdText != null && !orderIdText.trim().isEmpty()) {
        try {
            int orderId = Integer.parseInt(orderIdText.trim());
            OrderDAO orderDAO = new OrderDAO();
            order = orderDAO.findOrderById(orderId, user.getId());
        } catch (NumberFormatException ignored) {}
    }

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Confirmation - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260921_1">
</head>
<body>

    <!-- Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/buyer/marketplace" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/buyer/marketplace" class="nav-link">Marketplace</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/cart" class="nav-link">My Cart</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/orders" class="nav-link">My Orders</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="seller-page">
        <div class="seller-container" style="max-width: 800px;">

            <!-- Success Banner Card -->
            <div class="seller-status-card" style="padding: clamp(1.5rem, 5vw, 2.5rem); text-align: center; display: block; background: #fff; margin-bottom: 2rem;">
                <div style="font-size: 3.5rem; margin-bottom: 0.5rem;">🎉</div>
                <span class="eyebrow" style="color: #2e7d32;">PAYMENT & ORDER CONFIRMED</span>
                <h1 style="font-size: clamp(1.6rem, 5vw, 2.2rem); font-weight: 900; margin: 0.3rem 0 0.8rem;">Thank You For Your Order!</h1>
                <p style="color: #666; max-width: 480px; margin: 0 auto 1.5rem; font-size: 0.95rem;">
                    Your order has been successfully placed with the sellers and the inventory has been updated.
                </p>

                <% if (order != null) { %>
                    <div style="display: inline-flex; gap: 1rem 2rem; background: #fafafa; border: 2px solid #000; padding: clamp(0.75rem, 3vw, 1rem) clamp(1rem, 4vw, 2rem); margin-bottom: 1.5rem; text-align: left; flex-wrap: wrap;">
                        <div>
                            <span style="font-size: 0.72rem; color: #666; text-transform: uppercase; font-weight: 700; display: block;">Order Reference</span>
                            <strong style="font-size: 1.1rem;">#<%= order.getId() %></strong>
                        </div>
                        <div>
                            <span style="font-size: 0.72rem; color: #666; text-transform: uppercase; font-weight: 700; display: block;">Order Date</span>
                            <span><%= order.getCreatedAt() != null ? sdf.format(order.getCreatedAt()) : "Today" %></span>
                        </div>
                        <div>
                            <span style="font-size: 0.72rem; color: #666; text-transform: uppercase; font-weight: 700; display: block;">Status</span>
                            <span class="category-chip" style="background: #000; color: #fff;"><%= HtmlUtil.escape(order.getStatus()) %></span>
                        </div>
                        <div>
                            <span style="font-size: 0.72rem; color: #666; text-transform: uppercase; font-weight: 700; display: block;">Total Paid</span>
                            <strong style="font-size: 1.2rem; color: #000;">₹<%= order.getTotalAmount() %></strong>
                        </div>
                    </div>
                <% } %>
            </div>

            <!-- Items Purchased Table -->
            <% if (order != null && order.getItems() != null && !order.getItems().isEmpty()) { %>
                <div class="products-panel">
                    <div class="products-panel-header">
                        <div>
                            <h3>Purchased Items</h3>
                            <p><%= order.getItems().size() %> line item<%= order.getItems().size() != 1 ? "s" : "" %></p>
                        </div>
                    </div>

                    <div class="products-table-wrapper">
                        <table class="products-table">
                            <thead>
                                <tr>
                                    <th>Item</th>
                                    <th>Unit Price</th>
                                    <th>Qty</th>
                                    <th style="text-align: right;">Total</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (OrderItem item : order.getItems()) {
                                    Product p = item.getProduct();
                                    String pName = p != null ? p.getName() : "Product #" + item.getProductId();
                                    String pImg = (p != null && p.getImageUrl() != null) ? p.getImageUrl() : "default-product.svg";
                                    String imgSrc = pImg.startsWith("default-") ? request.getContextPath() + "/images/" + pImg : request.getContextPath() + "/images/products/" + pImg;
                                %>
                                    <tr>
                                        <td>
                                            <div style="display: flex; align-items: center; gap: 10px;">
                                                <img src="<%= imgSrc %>" 
                                                     alt="<%= HtmlUtil.escape(pName) %>" 
                                                     class="product-thumb"
                                                     style="width: 38px; height: 38px;"
                                                     onerror="this.src='${pageContext.request.contextPath}/images/default-product.svg';">
                                                <div>
                                                    <strong><%= HtmlUtil.escape(pName) %></strong>
                                                    <% if (p != null && p.getCategory() != null) { %>
                                                        <span style="display: block; font-size: 0.72rem; color: #666;"><%= HtmlUtil.escape(p.getCategory()) %></span>
                                                    <% } %>
                                                </div>
                                            </div>
                                        </td>
                                        <td>₹<%= item.getPrice() %></td>
                                        <td><%= item.getQuantity() %></td>
                                        <td style="text-align: right;"><strong>₹<%= item.getSubtotal() %></strong></td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            <% } %>

            <div style="display: flex; gap: 1rem; justify-content: center; margin-top: 2rem; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/buyer/marketplace" class="seller-primary-button" style="padding: 0.95rem 1.8rem;">
                    Continue Shopping
                </a>
                <a href="${pageContext.request.contextPath}/buyer/orders" class="seller-secondary-button" style="padding: 0.95rem 1.8rem;">
                    View My Orders
                </a>
            </div>

        </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <p>© 2026 RashikMart. All rights reserved.</p>
    </footer>

</body>
</html>
