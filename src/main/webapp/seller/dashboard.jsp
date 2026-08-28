<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rashik.rashikmart.model.User" %>

<%
    User user = (User) session.getAttribute("user");

    if (user == null) {
        response.sendRedirect(
                request.getContextPath() + "/login.jsp?error=Please+login+first"
        );
        return;
    }

    String role = user.getRole();

    if (role == null || !"SELLER".equalsIgnoreCase(role)) {
        response.sendRedirect(
                request.getContextPath() + "/login.jsp?error=Access+denied"
        );
        return;
    }

    String userName = user.getName();

    if (userName == null || userName.trim().isEmpty()) {
        userName = "Seller";
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Seller Dashboard | RashikMart</title>

    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/css/style.css">

</head>


<body class="seller-dashboard-page">


<!-- ================= NAVBAR ================= -->

<header class="seller-navbar">

    <div class="seller-navbar-inner">

        <a href="<%= request.getContextPath() %>/seller/dashboard.jsp"
           class="seller-logo">
            RASHIK<span>MART</span>
        </a>


        <nav class="seller-nav">

            <a href="<%= request.getContextPath() %>/seller/dashboard.jsp"
               class="seller-nav-link active">
                Dashboard
            </a>

            <a href="<%= request.getContextPath() %>/seller/add-product.jsp"
               class="seller-nav-link">
                Products
            </a>

            <a href="#orders"
               class="seller-nav-link">
                Orders
            </a>

        </nav>


        <div class="seller-profile">

            <div class="seller-avatar">
                <%= userName.substring(0, 1).toUpperCase() %>
            </div>

            <div class="seller-profile-info">

                <strong>
                    <%= userName %>
                </strong>

                <span>
                    Seller
                </span>

            </div>


            <a href="<%= request.getContextPath() %>/logout"
               class="logout-link">
                Logout
            </a>

        </div>

    </div>

</header>


<!-- ================= MAIN ================= -->

<main class="seller-main">


    <!-- ================= PAGE HEADER ================= -->

    <section class="seller-page-header">

        <div>

            <p class="seller-overline">
                SELLER DASHBOARD
            </p>

            <h1>
                Welcome back, <%= userName %>.
            </h1>

            <p class="seller-page-description">
                Manage your products, monitor your marketplace activity
                and keep your listings up to date.
            </p>

        </div>


        <a href="<%= request.getContextPath() %>/seller/add-product.jsp"
           class="seller-primary-button">

            <span>+</span>
            Add Product

        </a>

    </section>



    <!-- ================= STATS ================= -->

    <section class="seller-stats">


        <div class="seller-stat">

            <div class="stat-top">

                <span class="stat-label">
                    PRODUCTS
                </span>

                <span class="stat-icon">
                    P
                </span>

            </div>

            <strong class="stat-value">
                0
            </strong>

            <p>
                Active listings
            </p>

        </div>



        <div class="seller-stat">

            <div class="stat-top">

                <span class="stat-label">
                    ORDERS
                </span>

                <span class="stat-icon">
                    O
                </span>

            </div>

            <strong class="stat-value">
                0
            </strong>

            <p>
                Orders received
            </p>

        </div>



        <div class="seller-stat">

            <div class="stat-top">

                <span class="stat-label">
                    SALES
                </span>

                <span class="stat-icon">
                    ₹
                </span>

            </div>

            <strong class="stat-value">
                ₹0
            </strong>

            <p>
                Total sales
            </p>

        </div>


    </section>



    <!-- ================= CONTENT GRID ================= -->

    <section class="seller-content-grid">


        <!-- QUICK ACTIONS -->

        <div class="seller-panel">

            <div class="panel-heading">

                <div>

                    <p class="seller-overline">
                        GET STARTED
                    </p>

                    <h2>
                        Quick actions
                    </h2>

                </div>

            </div>


            <div class="quick-actions">


                <a href="<%= request.getContextPath() %>/seller/add-product.jsp"
                   class="quick-action">

                    <div class="quick-action-icon">
                        +
                    </div>

                    <div>

                        <strong>
                            Add a product
                        </strong>

                        <span>
                            Create a new marketplace listing
                        </span>

                    </div>

                    <span class="action-arrow">
                        →
                    </span>

                </a>



                <a href="#products"
                   class="quick-action">

                    <div class="quick-action-icon">
                        P
                    </div>

                    <div>

                        <strong>
                            Manage products
                        </strong>

                        <span>
                            View and update your listings
                        </span>

                    </div>

                    <span class="action-arrow">
                        →
                    </span>

                </a>



                <a href="#orders"
                   class="quick-action">

                    <div class="quick-action-icon">
                        O
                    </div>

                    <div>

                        <strong>
                            View orders
                        </strong>

                        <span>
                            Track incoming buyer orders
                        </span>

                    </div>

                    <span class="action-arrow">
                        →
                    </span>

                </a>


            </div>

        </div>



        <!-- ACCOUNT OVERVIEW -->

        <div class="seller-panel account-panel">

            <div class="panel-heading">

                <div>

                    <p class="seller-overline">
                        ACCOUNT
                    </p>

                    <h2>
                        Seller profile
                    </h2>

                </div>

            </div>


            <div class="account-details">


                <div class="account-row">

                    <span>
                        Name
                    </span>

                    <strong>
                        <%= userName %>
                    </strong>

                </div>


                <div class="account-row">

                    <span>
                        Email
                    </span>

                    <strong>
                        <%= user.getEmail() %>
                    </strong>

                </div>


                <div class="account-row">

                    <span>
                        Account type
                    </span>

                    <strong>
                        <%= role %>
                    </strong>

                </div>


                <div class="account-row">

                    <span>
                        Status
                    </span>

                    <strong class="account-status">
                        Active
                    </strong>

                </div>


            </div>

        </div>


    </section>



    <!-- ================= PRODUCTS ================= -->

    <section class="seller-panel products-panel"
             id="products">

        <div class="panel-heading products-heading">

            <div>

                <p class="seller-overline">
                    INVENTORY
                </p>

                <h2>
                    Your products
                </h2>

            </div>


            <a href="<%= request.getContextPath() %>/seller/add-product.jsp"
               class="panel-link">

                Add product →

            </a>

        </div>


        <div class="empty-products">

            <div class="empty-icon">
                +
            </div>

            <h3>
                No products yet
            </h3>

            <p>
                Your products will appear here once you create
                your first listing.
            </p>

            <a href="<%= request.getContextPath() %>/seller/add-product.jsp"
               class="seller-secondary-button">

                Create your first product

            </a>

        </div>

    </section>



    <!-- ================= ORDERS ================= -->

    <section class="seller-panel orders-panel"
             id="orders">

        <div class="panel-heading">

            <div>

                <p class="seller-overline">
                    SALES
                </p>

                <h2>
                    Recent orders
                </h2>

            </div>

        </div>


        <div class="empty-orders">

            <p>
                No orders have been received yet.
            </p>

        </div>

    </section>


</main>



<!-- ================= FOOTER ================= -->

<footer class="seller-footer">

    <div>

        <strong>
            RASHIKMART
        </strong>

        <span>
            Seller Portal
        </span>

    </div>


    <p>
        © 2026 RashikMart. All rights reserved.
    </p>

</footer>


</body>

</html>