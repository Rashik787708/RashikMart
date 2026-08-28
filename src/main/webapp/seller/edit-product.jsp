<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.dao.ProductDAO" %>

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

    Product product = (Product) request.getAttribute("product");
    if (product == null) {
        String idText = request.getParameter("id");
        if (idText != null && !idText.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idText.trim());
                ProductDAO productDAO = new ProductDAO();
                product = productDAO.findByIdAndSellerId(id, user.getId());
            } catch (Exception ignored) {}
        }
    }

    if (product == null) {
        response.sendRedirect(request.getContextPath() + "/seller/dashboard.jsp?error=Product+not+found");
        return;
    }

    String error = request.getParameter("error");
    String currentCategory = product.getCategory() != null ? product.getCategory().trim() : "";
    String currentImage = "default-product.svg";
    try {
        if (product.getImageUrl() != null && !product.getImageUrl().trim().isEmpty()) {
            currentImage = product.getImageUrl().trim();
        }
    } catch (Throwable t) {
        currentImage = "default-product.svg";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Product - RashikMart</title>
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
                <li><a href="${pageContext.request.contextPath}/seller/products.jsp" class="nav-link">My Products</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Main Page -->
    <main class="page">
        <div class="register-card product-form-card">
            <div class="card-header">
                <span class="eyebrow">SELLER PANEL</span>
                <h2>Edit Product #<%= product.getId() %></h2>
                <p class="subtitle">Update listing details, pricing, stock quantity, and photo</p>
            </div>

            <% if (error != null && !error.trim().isEmpty()) {
                String safeError = error.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            %>
                <div class="message error"><%= safeError %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/seller/edit-product" method="post" enctype="multipart/form-data" class="register-form">
                <input type="hidden" name="id" value="<%= product.getId() %>">
                <input type="hidden" name="currentImageUrl" value="<%= currentImage %>">

                <div class="form-group">
                    <label for="name">Product Name</label>
                    <input type="text" id="name" name="name" value="<%= product.getName() != null ? product.getName() : "" %>" placeholder="e.g. Organic Turmeric" maxlength="150" required>
                </div>

                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea id="description" name="description" placeholder="Describe your product" maxlength="1000" rows="4"><%= product.getDescription() != null ? product.getDescription() : "" %></textarea>
                </div>

                <div class="form-group">
                    <label for="category">Category</label>
                    <select id="category" name="category" required>
                        <option value="">Select category</option>
                        <option value="Grains" <%= "Grains".equalsIgnoreCase(currentCategory) ? "selected" : "" %>>Grains</option>
                        <option value="Pulses" <%= "Pulses".equalsIgnoreCase(currentCategory) ? "selected" : "" %>>Pulses</option>
                        <option value="Vegetables" <%= "Vegetables".equalsIgnoreCase(currentCategory) ? "selected" : "" %>>Vegetables</option>
                        <option value="Fruits" <%= "Fruits".equalsIgnoreCase(currentCategory) ? "selected" : "" %>>Fruits</option>
                        <option value="Spices" <%= "Spices".equalsIgnoreCase(currentCategory) ? "selected" : "" %>>Spices</option>
                        <option value="Oilseeds" <%= "Oilseeds".equalsIgnoreCase(currentCategory) ? "selected" : "" %>>Oilseeds</option>
                        <option value="Other" <%= "Other".equalsIgnoreCase(currentCategory) ? "selected" : "" %>>Other</option>
                    </select>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="price">Price (₹)</label>
                        <input type="number" id="price" name="price" value="<%= product.getPrice() != null ? product.getPrice() : "" %>" placeholder="0.00" min="0.01" step="0.01" required>
                    </div>

                    <div class="form-group">
                        <label for="quantity">Quantity Available</label>
                        <input type="number" id="quantity" name="quantity" value="<%= product.getQuantity() %>" placeholder="0" min="1" required>
                    </div>
                </div>

                <!-- Current Photo and Upload New -->
                <div class="form-group">
                    <label>Product Photo</label>
                    <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 10px; padding: 10px; background: #fafafa; border: 1px solid #ddd;">
                        <img src="<%= currentImage.startsWith("default-") ? request.getContextPath() + "/images/" + currentImage : request.getContextPath() + "/images/products/" + currentImage %>" 
                             alt="<%= product.getName() %>" 
                             style="width: 50px; height: 50px; object-fit: cover; border: 1.5px solid #000;"
                             onerror="this.src='${pageContext.request.contextPath}/images/default-product.svg';">
                        <div>
                            <span style="font-size: 0.8rem; font-weight: 700; display: block;">Current Photo: <%= currentImage %></span>
                            <span style="font-size: 0.75rem; color: #666;">Upload a new image below to replace it.</span>
                        </div>
                    </div>
                    <input type="file" id="image" name="image" accept="image/png, image/jpeg, image/webp, image/svg+xml">
                </div>

                <button type="submit" class="primary-button register-button">Save Changes</button>
            </form>

            <div class="account-link">
                <a href="${pageContext.request.contextPath}/seller/products.jsp">← Back to My Products</a>
                <span style="margin: 0 10px; color: #ccc;">|</span>
                <a href="${pageContext.request.contextPath}/seller/dashboard.jsp">Dashboard</a>
            </div>
        </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <p>© 2026 RashikMart. All rights reserved.</p>
    </footer>

</body>
</html>
