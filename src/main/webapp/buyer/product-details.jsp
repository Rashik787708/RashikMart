<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.dao.ProductDAO" %>
<%@ page import="com.rashik.rashikmart.dao.CartDAO" %>
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

    Product product = (Product) request.getAttribute("product");
    if (product == null) {
        String idText = request.getParameter("id");
        if (idText != null && !idText.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idText.trim());
                ProductDAO productDAO = new ProductDAO();
                product = productDAO.findById(id);
            } catch (Exception ignored) {}
        }
    }

    if (product == null) {
        response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Product+not+found");
        return;
    }

    CartDAO cartDAO = new CartDAO();
    int cartCount = cartDAO.getCartItems(user.getId()).size();

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String pImg = "default-product.svg";
    try {
        if (product.getImageUrl() != null && !product.getImageUrl().trim().isEmpty()) {
            pImg = product.getImageUrl().trim();
        }
    } catch (Throwable ignored) {}
    String imgSrc = pImg.startsWith("default-") ? request.getContextPath() + "/images/" + pImg : request.getContextPath() + "/images/products/" + pImg;
    boolean inStock = product.getQuantity() > 0;
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= HtmlUtil.escape(product.getName()) %> - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260828_4">
</head>
<body>

    <!-- Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/buyer/marketplace" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/buyer/marketplace" class="nav-link active">Marketplace</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/cart" class="nav-link">My Cart (<%= cartCount %>)</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/orders" class="nav-link">My Orders</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="page">
        <div class="seller-container" style="max-width: 900px; width: 100%;">

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

            <div class="seller-status-card" style="padding: 2.5rem; display: flex; gap: 2.5rem; align-items: flex-start; flex-wrap: wrap;">

                <!-- Product Image Section -->
                <div style="flex: 1; min-width: 280px; max-width: 380px;">
                    <div style="width: 100%; height: 320px; background: #f0f0f0; border: 2px solid #000; overflow: hidden; display: flex; align-items: center; justify-content: center; box-shadow: 4px 4px 0px #000;">
                        <img src="<%= imgSrc %>"
                             alt="<%= HtmlUtil.escape(product.getName()) %>"
                             style="width: 100%; height: 100%; object-fit: cover;"
                             onerror="this.src='${pageContext.request.contextPath}/images/default-product.svg';">
                    </div>
                </div>

                <!-- Product Info & Order Section -->
                <div style="flex: 1.2; min-width: 280px;">
                    <span class="eyebrow"><%= product.getCategory() != null ? HtmlUtil.escape(product.getCategory().toUpperCase()) : "GENERAL" %> &bull; ITEM #<%= product.getId() %></span>
                    <h1 style="font-size: 2.2rem; font-weight: 800; margin: 0.3rem 0 0.8rem; letter-spacing: -0.5px;"><%= HtmlUtil.escape(product.getName()) %></h1>

                    <div style="display: flex; align-items: baseline; gap: 12px; margin-bottom: 1.2rem;">
                        <span style="font-size: 2.2rem; font-weight: 900; color: #000;">₹<%= product.getPrice() %></span>
                        <span style="color: #666; font-size: 0.9rem;">/ unit</span>
                    </div>

                    <div style="margin-bottom: 1.5rem;">
                        <% if (inStock) { %>
                            <span class="status-badge" style="background: #2e7d32; color: #fff;">IN STOCK: <%= product.getQuantity() %> UNITS AVAILABLE</span>
                        <% } else { %>
                            <span class="status-badge" style="background: #c62828; color: #fff;">OUT OF STOCK</span>
                        <% } %>
                    </div>

                    <div style="margin-bottom: 1.8rem; border-top: 1px solid #ddd; border-bottom: 1px solid #ddd; padding: 1.2rem 0;">
                        <h4 style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 1px; color: #555; margin-bottom: 0.5rem;">Product Description</h4>
                        <p style="font-size: 0.95rem; line-height: 1.6; color: #333;">
                            <%= (product.getDescription() != null && !product.getDescription().trim().isEmpty()) ? HtmlUtil.escape(product.getDescription()) : "No detailed description provided by the seller." %>
                        </p>
                    </div>

                    <% if (inStock) { %>
                        <form action="${pageContext.request.contextPath}/buyer/cart" method="post">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="productId" value="<%= product.getId() %>">

                            <div style="display: flex; align-items: flex-end; gap: 14px; margin-bottom: 1.5rem;">
                                <div style="width: 110px;">
                                    <label for="quantity" style="display: block; font-size: 0.75rem; font-weight: 700; text-transform: uppercase; margin-bottom: 4px;">Quantity</label>
                                    <input type="number"
                                           id="quantity"
                                           name="quantity"
                                           value="1"
                                           min="1"
                                           max="<%= product.getQuantity() %>"
                                           style="width: 100%; padding: 0.75rem; border: 2px solid #000; font-size: 1rem; font-weight: 700; text-align: center;"
                                           required>
                                </div>

                                <div style="flex: 1;">
                                    <button type="submit" class="primary-button" style="padding: 0.85rem; width: 100%; box-shadow: 4px 4px 0px #000;">
                                        + Add to Cart
                                    </button>
                                </div>
                            </div>
                        </form>
                    <% } else { %>
                        <div style="padding: 1rem; background: #fff5f5; border: 1.5px solid #c0392b; color: #8b1e14; font-weight: 700; text-align: center; margin-bottom: 1.5rem;">
                            This item is currently out of stock. Please check back later!
                        </div>
                    <% } %>

                    <div style="display: flex; gap: 15px; font-size: 0.85rem; font-weight: 700;">
                        <a href="${pageContext.request.contextPath}/buyer/marketplace" style="color: #000; text-decoration: underline;">← Back to Marketplace</a>
                        <span style="color: #ccc;">|</span>
                        <a href="${pageContext.request.contextPath}/buyer/cart" style="color: #000; text-decoration: underline;">Go to My Cart →</a>
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
