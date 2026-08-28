<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+first");
        return;
    }

    String role = (String) session.getAttribute("role");
    if (role == null || !"SELLER".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Seller+access+required");
        return;
    }

    String error = request.getParameter("error");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Product - RashikMart</title>
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
                <li><a href="${pageContext.request.contextPath}/seller/add-product.jsp" class="nav-link active">Add Product</a></li>
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
                <h2>Add Product</h2>
                <p class="subtitle">Create a new marketplace listing with photos & pricing</p>
            </div>

            <% if (error != null && !error.trim().isEmpty()) {
                String safeError = error.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            %>
                <div class="message error"><%= safeError %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/seller/add-product" method="post" enctype="multipart/form-data" class="register-form">

                <div class="form-group">
                    <label for="name">Product Name</label>
                    <input type="text" id="name" name="name" placeholder="e.g. Organic Turmeric" maxlength="150" required>
                </div>

                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea id="description" name="description" placeholder="Describe your product in detail..." maxlength="1000" rows="4"></textarea>
                </div>

                <div class="form-group">
                    <label for="category">Category</label>
                    <select id="category" name="category" required>
                        <option value="">Select category</option>
                        <option value="Grains">Grains</option>
                        <option value="Pulses">Pulses</option>
                        <option value="Vegetables">Vegetables</option>
                        <option value="Fruits">Fruits</option>
                        <option value="Spices">Spices</option>
                        <option value="Oilseeds">Oilseeds</option>
                        <option value="Other">Other</option>
                    </select>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="price">Price (₹)</label>
                        <input type="number" id="price" name="price" placeholder="0.00" min="0.01" step="0.01" required>
                    </div>

                    <div class="form-group">
                        <label for="quantity">Quantity in Stock</label>
                        <input type="number" id="quantity" name="quantity" placeholder="0" min="1" required>
                    </div>
                </div>

                <!-- Product Photo Upload -->
                <div class="form-group">
                    <label for="image">Product Photo</label>
                    <input type="file" id="image" name="image" accept="image/png, image/jpeg, image/webp, image/svg+xml">
                    <small style="color: #666666; font-size: 0.78rem; display: block; margin-top: 5px;">
                        Accepted: JPG, PNG, WEBP, SVG (Max 5MB). Leave empty to use default photo.
                    </small>
                </div>

                <button type="submit" class="primary-button register-button">Add Product</button>
            </form>

            <div class="account-link">
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