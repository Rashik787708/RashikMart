<%@ page contentType="text/html;charset=UTF-8" %>
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

    ProductDAO productDAO = new ProductDAO();
    List<Product> products = productDAO.findBySellerId(user.getId());
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Products - RashikMart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<nav class="navbar">
    <a href="<%= request.getContextPath() %>/seller/dashboard.jsp" class="nav-brand">RASHIKMART</a>
    <div class="nav-links">
        <a href="<%= request.getContextPath() %>/seller/dashboard.jsp" class="nav-link">Dashboard</a>
        <a href="<%= request.getContextPath() %>/seller/add-product.jsp" class="nav-link">Add Product</a>
        <a href="<%= request.getContextPath() %>/seller/products.jsp" class="nav-link active">My Products</a>
        <a href="<%= request.getContextPath() %>/logout" class="nav-link">Logout</a>
    </div>
</nav>

<main class="seller-page">
    <div class="seller-container">

        <section class="seller-header">
            <div class="seller-introduction">
                <span class="eyebrow">SELLER PANEL</span>
                <h1>My Products</h1>
                <p>All products you have listed on RashikMart.</p>
            </div>
            <a href="<%= request.getContextPath() %>/seller/add-product.jsp" class="seller-primary-button">
                ADD NEW PRODUCT
            </a>
        </section>

        <% if (products == null || products.isEmpty()) { %>
            <section class="seller-status-card">
                <div>
                    <span class="eyebrow">EMPTY</span>
                    <h2>No products yet</h2>
                    <p>Add your first product to start selling.</p>
                </div>
            </section>
        <% } else { %>
            <div style="overflow-x:auto; margin-top:1.5rem;">
                <table style="width:100%; border-collapse:collapse; background:#fff; border:1px solid #ddd;">
                    <thead>
                        <tr style="background:#000; color:#fff; text-align:left;">
                            <th style="padding:12px 14px;">ID</th>
                            <th style="padding:12px 14px;">Name</th>
                            <th style="padding:12px 14px;">Category</th>
                            <th style="padding:12px 14px;">Description</th>
                            <th style="padding:12px 14px;">Price</th>
                            <th style="padding:12px 14px;">Quantity</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Product p : products) { %>
                            <tr style="border-bottom:1px solid #eee;">
                                <td style="padding:12px 14px;"><%= p.getId() %></td>
                                <td style="padding:12px 14px;"><%= p.getName() %></td>
                                <td style="padding:12px 14px;"><%= p.getCategory() %></td>
                                <td style="padding:12px 14px; max-width:280px;">
                                    <%= p.getDescription() != null ? p.getDescription() : "-" %>
                                </td>
                                <td style="padding:12px 14px;">₹<%= p.getPrice() %></td>
                                <td style="padding:12px 14px;"><%= p.getQuantity() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>

        <p style="margin-top:1.5rem;">
            <a href="<%= request.getContextPath() %>/seller/dashboard.jsp">← Back to Dashboard</a>
        </p>
    </div>
</main>

<footer class="footer">
    <p>© 2026 RashikMart. All rights reserved.</p>
</footer>

</body>
</html>