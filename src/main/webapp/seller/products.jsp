<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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

    String role = user.getRole();
    if (role == null || !"SELLER".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Seller+access+required");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    ProductDAO productDAO = new ProductDAO();
    List<Product> products = productDAO.findBySellerId(user.getId());
    int totalCount = products != null ? products.size() : 0;
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Products - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260828_3">
</head>
<body>

    <!-- Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/seller/dashboard.jsp" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/seller/dashboard.jsp" class="nav-link">Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/seller/add-product.jsp" class="nav-link">Add Product</a></li>
                <li><a href="${pageContext.request.contextPath}/seller/products.jsp" class="nav-link active">My Products</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Main Page -->
    <main class="seller-page">
        <div class="seller-container">

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

            <section class="seller-header">
                <div class="seller-introduction">
                    <span class="eyebrow">SELLER PANEL</span>
                    <h1>My Product Inventory</h1>
                    <p>Manage, edit, and review all products listed under your seller account.</p>
                </div>
                <a href="${pageContext.request.contextPath}/seller/add-product.jsp" class="seller-primary-button">
                    + Add New Product
                </a>
            </section>

            <div class="products-panel">
                <div class="products-panel-header">
                    <div>
                        <h3>Listed Items</h3>
                        <p><%= totalCount %> product<%= totalCount != 1 ? "s" : "" %> in catalog</p>
                    </div>
                </div>

                <% if (products == null || products.isEmpty()) { %>
                    <div class="empty-state">
                        <h3>No Products Found</h3>
                        <p>You haven't created any marketplace listings yet. Add a product to make it available to buyers.</p>
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
                                    <th>Description</th>
                                    <th>Unit Price</th>
                                    <th>Available Quantity</th>
                                    <th style="text-align: right;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (Product p : products) {
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
                                        <td style="max-width: 240px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                            <%= (p.getDescription() != null && !p.getDescription().trim().isEmpty()) ? p.getDescription() : "-" %>
                                        </td>
                                        <td><strong>₹<%= p.getPrice() %></strong></td>
                                        <td><%= p.getQuantity() %> units</td>
                                        <td style="text-align: right;">
                                            <div class="table-actions" style="justify-content: flex-end;">
                                                <a href="${pageContext.request.contextPath}/seller/edit-product?id=<%= p.getId() %>" class="action-btn">
                                                    Edit
                                                </a>
                                                <form action="${pageContext.request.contextPath}/seller/delete-product" method="post" style="display: inline;" onsubmit="return confirm('Are you sure you want to delete this product?');">
                                                    <input type="hidden" name="id" value="<%= p.getId() %>">
                                                    <input type="hidden" name="redirect" value="/seller/products.jsp">
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

            <div class="account-link" style="text-align: left; margin-top: 1.5rem;">
                <a href="${pageContext.request.contextPath}/seller/dashboard.jsp">← Back to Dashboard</a>
            </div>

        </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <p>© 2026 RashikMart. All rights reserved.</p>
    </footer>

</body>
</html>