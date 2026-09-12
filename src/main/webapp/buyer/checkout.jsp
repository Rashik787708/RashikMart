<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.CartItem" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.dao.CartDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>

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

    List<CartItem> cartItems = (List<CartItem>) request.getAttribute("cartItems");
    BigDecimal cartTotal = (BigDecimal) request.getAttribute("cartTotal");

    if (cartItems == null || cartTotal == null) {
        CartDAO cartDAO = new CartDAO();
        cartItems = cartDAO.getCartItems(user.getId());
        cartTotal = cartDAO.getCartTotal(user.getId());
    }

    if (cartItems == null || cartItems.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/buyer/cart?error=Your+cart+is+empty");
        return;
    }

    String error = request.getParameter("error");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout - RashikMart</title>
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
                <li><a href="${pageContext.request.contextPath}/buyer/cart" class="nav-link">My Cart (<%= cartItems.size() %>)</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/orders" class="nav-link">My Orders</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="seller-page">
        <div class="seller-container" style="max-width: 960px;">

            <% if (error != null && !error.trim().isEmpty()) {
                String safeError = error.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            %>
                <div class="message error"><%= safeError %></div>
            <% } %>

            <section class="seller-header">
                <div class="seller-introduction">
                    <span class="eyebrow">FINAL STEP</span>
                    <h1>Confirm & Place Order</h1>
                    <p>Please review your purchase items and shipping details before completing your order.</p>
                </div>
                <a href="${pageContext.request.contextPath}/buyer/cart" class="seller-secondary-button">
                    ← Edit Cart
                </a>
            </section>

            <div style="display: flex; gap: 2rem; align-items: flex-start; flex-wrap: wrap;">

                <!-- Order Items Review Table -->
                <div style="flex: 1.8; min-width: 320px;">
                    <div class="products-panel">
                        <div class="products-panel-header">
                            <div>
                                <h3>Order Items</h3>
                                <p><%= cartItems.size() %> product<%= cartItems.size() != 1 ? "s" : "" %> selected</p>
                            </div>
                        </div>

                        <div class="products-table-wrapper">
                            <table class="products-table">
                                <thead>
                                    <tr>
                                        <th>Product</th>
                                        <th>Price</th>
                                        <th>Qty</th>
                                        <th style="text-align: right;">Subtotal</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (CartItem item : cartItems) {
                                        Product prod = item.getProduct();
                                        String pImg = "default-product.svg";
                                        try {
                                            if (prod != null && prod.getImageUrl() != null && !prod.getImageUrl().trim().isEmpty()) {
                                                pImg = prod.getImageUrl().trim();
                                            }
                                        } catch (Throwable ignored) {}
                                        String imgSrc = pImg.startsWith("default-") ? request.getContextPath() + "/images/" + pImg : request.getContextPath() + "/images/products/" + pImg;
                                        String prodName = prod != null ? prod.getName() : "Product #" + item.getProductId();
                                        BigDecimal unitPrice = prod != null ? prod.getPrice() : BigDecimal.ZERO;
                                    %>
                                        <tr>
                                            <td>
                                                <div style="display: flex; align-items: center; gap: 10px;">
                                                    <img src="<%= imgSrc %>" 
                                                         alt="<%= prodName %>" 
                                                         class="product-thumb"
                                                         style="width: 38px; height: 38px;"
                                                         onerror="this.src='${pageContext.request.contextPath}/images/default-product.svg';">
                                                    <div>
                                                        <strong><%= prodName %></strong>
                                                        <% if (prod != null && prod.getCategory() != null) { %>
                                                            <span style="display: block; font-size: 0.72rem; color: #666;"><%= prod.getCategory() %></span>
                                                        <% } %>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>₹<%= unitPrice %></td>
                                            <td><%= item.getQuantity() %></td>
                                            <td style="text-align: right;"><strong>₹<%= item.getSubtotal() %></strong></td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Buyer Details & Place Order -->
                <div style="flex: 1.2; min-width: 280px;">
                    <div class="seller-stat-card" style="padding: 1.8rem;">
                        <span class="eyebrow">BUYER & SHIPPING</span>
                        <h3 style="font-size: 1.2rem; margin: 0.3rem 0 1rem;">Customer Info</h3>

                        <div style="background: #fafafa; border: 1.5px solid #eee; padding: 1rem; margin-bottom: 1.5rem; font-size: 0.88rem;">
                            <div style="margin-bottom: 0.4rem;">
                                <span style="color: #666; font-size: 0.75rem; text-transform: uppercase; display: block; font-weight: 700;">Account Name</span>
                                <strong><%= user.getName() %></strong>
                            </div>
                            <div style="margin-bottom: 0.4rem;">
                                <span style="color: #666; font-size: 0.75rem; text-transform: uppercase; display: block; font-weight: 700;">Email Address</span>
                                <span><%= user.getEmail() %></span>
                            </div>
                            <div>
                                <span style="color: #666; font-size: 0.75rem; text-transform: uppercase; display: block; font-weight: 700;">Account Type</span>
                                <span class="category-chip" style="font-size: 0.7rem; padding: 2px 6px;">BUYER VERIFIED</span>
                            </div>
                        </div>

                        <div style="border-top: 1.5px solid #eee; padding-top: 1rem; margin-bottom: 1.2rem;">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem; font-size: 0.9rem;">
                                <span style="color: #666;">Total Items</span>
                                <strong><%= cartItems.size() %> items</strong>
                            </div>
                            <div style="display: flex; justify-content: space-between; margin-bottom: 0.8rem; font-size: 0.9rem;">
                                <span style="color: #666;">Delivery</span>
                                <strong style="color: #2e7d32;">FREE</strong>
                            </div>
                            <div style="display: flex; justify-content: space-between; align-items: baseline; border-top: 2px solid #000; padding-top: 0.8rem;">
                                <span style="font-weight: 800; font-size: 1rem; text-transform: uppercase;">Total to Pay</span>
                                <span style="font-size: 1.8rem; font-weight: 900; color: #000;">₹<%= cartTotal %></span>
                            </div>
                        </div>

                        <form action="${pageContext.request.contextPath}/buyer/place-order" method="post">
                            <button type="submit" 
                                    class="primary-button" 
                                    style="width: 100%; padding: 1rem; font-size: 1rem; box-shadow: 4px 4px 0px #000;"
                                    onclick="this.disabled=true; this.innerText='Processing Order...'; this.form.submit();">
                                Place Order Now (₹<%= cartTotal %>)
                            </button>
                        </form>
                    </div>
                </div>

            </div>

        </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <p>© 2026 RashikMart. All rights reserved.</p>
    </footer>

</body>
</html>
