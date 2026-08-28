<%@ page contentType="text/html;charset=UTF-8" %>

<%
    if (session.getAttribute("user") == null) {

        response.sendRedirect(
                request.getContextPath()
                        + "/login.jsp?error=Please+login+first"
        );

        return;
    }

    String role =
            (String) session.getAttribute("role");

    if (role == null
            || !"SELLER".equalsIgnoreCase(role)) {

        response.sendRedirect(
                request.getContextPath()
                        + "/login.jsp?error=Seller+access+required"
        );

        return;
    }

    String userName =
            (String) session.getAttribute("userName");

    String success =
            request.getParameter("success");

    String error =
            request.getParameter("error");
%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Seller Dashboard - RashikMart</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">

</head>

<body>

<!-- =========================
     NAVBAR
========================= -->

<nav class="navbar">

    <a
            href="<%= request.getContextPath() %>/seller/dashboard.jsp"
            class="nav-brand"
    >
        RASHIKMART
    </a>

    <div class="nav-links">

        <a
                href="<%= request.getContextPath() %>/seller/dashboard.jsp"
                class="nav-link active"
        >
            Dashboard
        </a>

        <a
                href="<%= request.getContextPath() %>/seller/add-product.jsp"
                class="nav-link"
        >
            Add Product
        </a>

        <a
                href="<%= request.getContextPath() %>/logout"
                class="nav-link"
        >
            Logout
        </a>

    </div>

</nav>


<!-- =========================
     MAIN
========================= -->

<main class="dashboard-page">

    <div class="dashboard-container">

        <div class="dashboard-header">

            <div>

                <span class="eyebrow">
                    SELLER PANEL
                </span>

                <h1>
                    Welcome,
                    <%= userName != null
                            ? userName
                            : "Seller" %>
                </h1>

                <p>
                    Manage your products and marketplace listings.
                </p>

            </div>

            <div class="role-badge">
                SELLER
            </div>

        </div>


        <!-- =========================
             MESSAGES
        ========================= -->

        <% if (success != null) { %>

            <div class="message success">
                <%= success %>
            </div>

        <% } %>


        <% if (error != null) { %>

            <div class="message error">
                <%= error %>
            </div>

        <% } %>


        <!-- =========================
             ACTION CARD
        ========================= -->

        <section class="dashboard-card">

            <div class="dashboard-card-content">

                <span class="eyebrow">
                    PRODUCT MANAGEMENT
                </span>

                <h2>
                    Start selling on RashikMart
                </h2>

                <p>
                    Add your agricultural products,
                    set pricing and manage available stock.
                </p>

                <a
                        href="<%= request.getContextPath() %>/seller/add-product.jsp"
                        class="primary-button dashboard-button"
                >
                    Add New Product
                </a>

            </div>

        </section>


        <!-- =========================
             WEEK 2 STATUS
        ========================= -->

        <section class="feature-grid">

            <div class="feature-card">

                <span class="feature-number">
                    01
                </span>

                <h3>
                    Add Products
                </h3>

                <p>
                    Create product listings with
                    name, category, price and quantity.
                </p>

            </div>


            <div class="feature-card">

                <span class="feature-number">
                    02
                </span>

                <h3>
                    Manage Stock
                </h3>

                <p>
                    Keep track of the quantity
                    available for buyers.
                </p>

            </div>


            <div class="feature-card">

                <span class="feature-number">
                    03
                </span>

                <h3>
                    Reach Buyers
                </h3>

                <p>
                    Your products will become
                    available in the buyer marketplace.
                </p>

            </div>

        </section>

    </div>

</main>


<footer class="footer">

    <p>
        © 2026 RashikMart. All rights reserved.
    </p>

</footer>

</body>

</html>