<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Order" %>
<%@ page import="com.rashik.rashikmart.model.OrderItem" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.dao.OrderDAO" %>
<%@ page import="com.rashik.rashikmart.dao.CartDAO" %>
<%@ page import="java.util.List" %>
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

    List<Order> orders = (List<Order>) request.getAttribute("orders");
    if (orders == null) {
        OrderDAO orderDAO = new OrderDAO();
        orders = orderDAO.findOrdersByBuyerId(user.getId());
    }

    CartDAO cartDAO = new CartDAO();
    int cartCount = cartDAO.getCartItems(user.getId()).size();

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Orders - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260828_4">
</head>
<body>

    <!-- Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/buyer/marketplace" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/buyer/marketplace" class="nav-link">Marketplace</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/cart" class="nav-link">My Cart (<%= cartCount %>)</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/orders" class="nav-link active">My Orders</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="seller-page">
        <div class="seller-container">

            <section class="seller-header">
                <div class="seller-introduction">
                    <span class="eyebrow">ORDER HISTORY</span>
                    <h1>My Placed Orders</h1>
                    <p>Track all past and current orders placed with RashikMart sellers.</p>
                </div>
                <a href="${pageContext.request.contextPath}/buyer/marketplace" class="seller-primary-button">
                    Browse Marketplace
                </a>
            </section>

            <% if (orders == null || orders.isEmpty()) { %>
                <div class="empty-state" style="background: #fff; border: 2px solid #000; box-shadow: 4px 4px 0px #000; padding: 4rem 2rem;">
                    <div style="font-size: 3rem; margin-bottom: 1rem;">📦</div>
                    <h2>No Orders Placed Yet</h2>
                    <p style="color: #666; max-width: 420px; margin: 0.5rem auto 1.5rem;">
                        You haven't placed any orders yet. Visit the marketplace to find fresh produce and products.
                    </p>
                    <a href="${pageContext.request.contextPath}/buyer/marketplace" class="primary-button" style="display: inline-block; max-width: 240px; padding: 0.85rem 1.5rem;">
                        Start Shopping
                    </a>
                </div>
            <% } else { %>
                <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                    <% for (Order order : orders) { %>
                        <div class="products-panel" style="margin-bottom: 0;">
                            <div class="products-panel-header" style="background: #fafafa; border-bottom: 2px solid #000; padding: 1.2rem 1.5rem;">
                                <div style="display: flex; gap: 2rem; align-items: center; flex-wrap: wrap;">
                                    <div>
                                        <span style="font-size: 0.72rem; color: #666; text-transform: uppercase; font-weight: 700; display: block;">Order Placed</span>
                                        <strong><%= order.getCreatedAt() != null ? sdf.format(order.getCreatedAt()) : "Recently" %></strong>
                                    </div>
                                    <div>
                                        <span style="font-size: 0.72rem; color: #666; text-transform: uppercase; font-weight: 700; display: block;">Total Amount</span>
                                        <strong style="color: #000;">₹<%= order.getTotalAmount() %></strong>
                                    </div>
                                    <div>
                                        <span style="font-size: 0.72rem; color: #666; text-transform: uppercase; font-weight: 700; display: block;">Status</span>
                                        <span class="category-chip" style="background: #000; color: #fff; padding: 2px 8px;"><%= HtmlUtil.escape(order.getStatus()) %></span>
                                    </div>
                                </div>
                                <div>
                                    <a href="${pageContext.request.contextPath}/buyer/order?id=<%= order.getId() %>" style="font-size: 0.9rem; font-weight: 800; color: #000; text-decoration: underline;">
                                        Order #<%= order.getId() %> →
                                    </a>
                                </div>
                            </div>

                            <div class="products-table-wrapper" style="padding: 1rem 1.5rem;">
                                <table class="products-table">
                                    <thead>
                                        <tr>
                                            <th>Product Name</th>
                                            <th>Unit Price</th>
                                            <th>Quantity</th>
                                            <th style="text-align: right;">Subtotal</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (order.getItems() != null) {
                                            for (OrderItem item : order.getItems()) {
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
                                                             style="width: 32px; height: 32px;"
                                                             onerror="this.src='${pageContext.request.contextPath}/images/default-product.svg';">
                                                        <span><strong><%= HtmlUtil.escape(pName) %></strong></span>
                                                    </div>
                                                </td>
                                                <td>₹<%= item.getPrice() %></td>
                                                <td><%= item.getQuantity() %></td>
                                                <td style="text-align: right;"><strong>₹<%= item.getSubtotal() %></strong></td>
                                            </tr>
                                        <% }} %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } %>

        </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <p>© 2026 RashikMart. All rights reserved.</p>
    </footer>

</body>
</html>
