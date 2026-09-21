<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.dao.ProductDAO" %>
<%@ page import="com.rashik.rashikmart.dao.CartDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="com.rashik.rashikmart.util.HtmlUtil" %>
<%@ page import="com.rashik.rashikmart.util.CsrfUtil" %>

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

    CsrfUtil.getOrCreateToken(session);

    List<Product> products = (List<Product>) request.getAttribute("products");
    if (products == null) {
        ProductDAO productDAO = new ProductDAO();
        products = productDAO.findAllAvailable();
    }

    CartDAO cartDAO = new CartDAO();
    int cartCount = cartDAO.getCartItems(user.getId()).size();

    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Marketplace - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260921_1">
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
    <main class="seller-page">
        <div class="seller-container">

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

            <!-- Marketplace Header -->
            <section class="seller-header">
                <div class="seller-introduction">
                    <span class="eyebrow">MARKETPLACE</span>
                    <h1>Browse Products</h1>
                    <p>Discover fresh listings from verified sellers and add items to your cart.</p>
                </div>
                <div class="seller-role-badge">
                    ROLE: <%= role %>
                </div>
            </section>

            <% if (products == null || products.isEmpty()) { %>
                <div class="empty-state" style="background: #fff; border: 2px solid #000; box-shadow: 4px 4px 0px #000; padding: 4rem 2rem;">
                    <div style="font-size: 3rem; margin-bottom: 1rem;">🛍️</div>
                    <h2>No Products Available</h2>
                    <p style="color: #666; max-width: 420px; margin: 0.5rem auto 1.5rem;">
                        There are no active listings available right now. Please check back later!
                    </p>
                    <a href="${pageContext.request.contextPath}/buyer/orders" class="seller-primary-button">
                        View My Orders
                    </a>
                </div>
            <% } else { %>
                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(min(260px, 100%), 1fr)); gap: 1.5rem;">
                    <% for (Product p : products) {
                        String pImg = "default-product.svg";
                        try {
                            if (p.getImageUrl() != null && !p.getImageUrl().trim().isEmpty()) {
                                pImg = p.getImageUrl().trim();
                            }
                        } catch (Throwable ignored) {}
                        String imgSrc = pImg.startsWith("default-") ? request.getContextPath() + "/images/" + pImg : request.getContextPath() + "/images/products/" + pImg;
                        boolean inStock = p.getQuantity() > 0;
                    %>
                        <div style="background: #fff; border: 2px solid #000; box-shadow: 4px 4px 0px #000; display: flex; flex-direction: column; overflow: hidden;">
                            <!-- Product Image -->
                            <div style="height: 180px; background: #f0f0f0; border-bottom: 2px solid #000; overflow: hidden; display: flex; align-items: center; justify-content: center;">
                                <img src="<%= imgSrc %>"
                                     alt="<%= HtmlUtil.escape(p.getName()) %>"
                                     style="width: 100%; height: 100%; object-fit: cover;"
                                     onerror="this.src='${pageContext.request.contextPath}/images/default-product.svg';">
                            </div>

                            <!-- Product Info -->
                            <div style="padding: 1.2rem; display: flex; flex-direction: column; flex: 1;">
                                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.7rem;">
                                    <span class="category-chip"><%= HtmlUtil.escape(p.getCategory()) %></span>
                                    <span style="font-size: 0.72rem; color: #888; font-weight: 700;">#<%= p.getId() %></span>
                                </div>

                                <h3 style="font-size: 1.1rem; font-weight: 800; margin-bottom: 0.3rem;"><%= HtmlUtil.escape(p.getName()) %></h3>

                                <div style="display: flex; align-items: baseline; gap: 8px; margin-bottom: 0.6rem;">
                                    <span style="font-size: 1.5rem; font-weight: 900; color: #000;">₹<%= p.getPrice() %></span>
                                    <span style="color: #666; font-size: 0.8rem;">/ unit</span>
                                </div>

                                <p style="font-size: 0.85rem; color: #555; line-height: 1.5; margin-bottom: 1rem; flex: 1;
                                   display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                                    <%= (p.getDescription() != null && !p.getDescription().trim().isEmpty()) ? HtmlUtil.escape(p.getDescription()) : "No detailed description provided by the seller." %>
                                </p>

                                <% if (inStock) { %>
                                    <span class="status-badge" style="background: #2e7d32; color: #fff; align-self: flex-start; margin-bottom: 1rem;">IN STOCK: <%= p.getQuantity() %></span>
                                <% } else { %>
                                    <span class="status-badge" style="background: #c62828; color: #fff; align-self: flex-start; margin-bottom: 1rem;">OUT OF STOCK</span>
                                <% } %>

                                <div style="display: flex; gap: 10px; align-items: stretch;">
                                    <a href="${pageContext.request.contextPath}/buyer/product-details?id=<%= p.getId() %>"
                                       class="seller-secondary-button"
                                       style="flex: 1; display: inline-flex; align-items: center; justify-content: center; text-align: center; padding: 0.65rem 0.6rem; font-size: 0.72rem;">
                                        View Details
                                    </a>
                                    <% if (inStock) { %>
                                        <form action="${pageContext.request.contextPath}/buyer/cart" method="post" style="flex: 1; display: flex;">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="action" value="add">
                                            <input type="hidden" name="productId" value="<%= p.getId() %>">
                                            <input type="hidden" name="quantity" value="1">
                                            <input type="hidden" name="redirect" value="marketplace">
                                            <button type="submit" class="seller-primary-button" style="flex: 1; padding: 0.65rem 0.6rem; font-size: 0.72rem; white-space: nowrap;">
                                                + Add to Cart
                                            </button>
                                        </form>
                                    <% } %>
                                </div>
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