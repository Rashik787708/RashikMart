<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Order" %>
<%@ page import="com.rashik.rashikmart.model.OrderItem" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.dao.OrderDAO" %>
<%@ page import="com.rashik.rashikmart.dao.CartDAO" %>
<%@ page import="java.text.SimpleDateFormat" %>

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

    Order order = (Order) request.getAttribute("order");
    if (order == null) {
        String idText = request.getParameter("id");
        if (idText != null && !idText.trim().isEmpty()) {
            try {
                int orderId = Integer.parseInt(idText.trim());
                OrderDAO orderDAO = new OrderDAO();
                order = orderDAO.findOrderById(orderId, user.getId());
            } catch (Exception ignored) {}
        }
    }

    if (order == null) {
        response.sendRedirect(request.getContextPath() + "/buyer/orders?error=Order+not+found+or+unauthorized");
        return;
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
    <title>Order #<%= order.getId() %> Details - RashikMart</title>
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
                    <span class="eyebrow">ORDER DETAILS</span>
                    <h1>Order #<%= order.getId() %></h1>
                    <p>Detailed receipt and purchased product information for this order.</p>
                </div>
                <a href="${pageContext.request.contextPath}/buyer/orders" class="seller-secondary-button">
                    ← Back to Orders
                </a>
            </section>

            <div class="products-panel">
                <div class="products-panel-header" style="background: #fafafa; border-bottom: 2px solid #000; padding: 1.2rem 1.5rem;">
                    <div style="display: flex; gap: 2rem; align-items: center; flex-wrap: wrap;">
                        <div>
                            <span style="font-size: 0.72rem; color: #666; text-transform: uppercase; font-weight: 700; display: block;">Order Placed</span>
                            <strong><%= order.getCreatedAt() != null ? sdf.format(order.getCreatedAt()) : "Recently" %></strong>
                        </div>
                        <div>
                            <span style="font-size: 0.72rem; color: #666; text-transform: uppercase; font-weight: 700; display: block;">Total Amount</span>
                            <strong style="color: #000; font-size: 1.1rem;">₹<%= order.getTotalAmount() %></strong>
                        </div>
                        <div>
                            <span style="font-size: 0.72rem; color: #666; text-transform: uppercase; font-weight: 700; display: block;">Status</span>
                            <span class="category-chip" style="background: #000; color: #fff; padding: 2px 8px;"><%= order.getStatus() %></span>
                        </div>
                    </div>
                    <div>
                        <span style="font-size: 0.9rem; font-weight: 800;">Receipt Reference: #<%= order.getId() %></span>
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
                                                 alt="<%= pName %>" 
                                                 class="product-thumb"
                                                 style="width: 32px; height: 32px;"
                                                 onerror="this.src='${pageContext.request.contextPath}/images/default-product.svg';">
                                            <span><strong><%= pName %></strong></span>
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

            <div style="margin-top: 1.5rem;">
                <a href="${pageContext.request.contextPath}/buyer/orders" class="seller-secondary-button">
                    ← Back to All Orders
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
