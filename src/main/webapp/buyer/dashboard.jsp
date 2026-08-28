<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>

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

    String userName = user.getName();
    if (userName == null || userName.trim().isEmpty()) {
        userName = (String) session.getAttribute("userName");
    }
    if (userName == null || userName.trim().isEmpty()) {
        userName = "Buyer";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Buyer Dashboard - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260828">
</head>
<body>

    <!-- Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/buyer/marketplace" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/buyer/dashboard.jsp" class="nav-link active">Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/marketplace" class="nav-link">Marketplace</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/cart" class="nav-link">My Cart</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/orders" class="nav-link">My Orders</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="seller-page">
        <div class="seller-container">

            <section class="seller-header">
                <div class="seller-introduction">
                    <span class="eyebrow">BUYER PANEL</span>
                    <h1>Welcome, <%= userName %></h1>
                    <p>Explore agricultural listings, connect with verified sellers, and manage your orders.</p>
                </div>
                <div class="seller-role-badge">
                    ROLE: <%= role %>
                </div>
            </section>

            <section class="seller-status-card">
                <div>
                    <span class="eyebrow">MARKETPLACE</span>
                    <h2>Browse Marketplace Products</h2>
                    <p>Explore products listed by sellers, add items to your cart, and place orders directly.</p>
                </div>
                <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
                    <a href="${pageContext.request.contextPath}/buyer/marketplace" class="seller-primary-button">
                        Browse Marketplace →
                    </a>
                    <span class="status-badge">ACTIVE</span>
                </div>
            </section>

        </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <p>© 2026 RashikMart. All rights reserved.</p>
    </footer>

</body>
</html>