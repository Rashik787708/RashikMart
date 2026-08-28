<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="com.rashik.rashikmart.model.User" %>

<%
    /*
     * ============================================================
     * SESSION CHECK
     * ============================================================
     */

    User user = (User) session.getAttribute("user");

    if (user == null) {

        response.sendRedirect(
                request.getContextPath()
                        + "/login.jsp?error=Please+login+first"
        );

        return;
    }


    /*
     * ============================================================
     * USER DETAILS
     * ============================================================
     */

    String userName = user.getName();

    String role = user.getRole();


    /*
     * ============================================================
     * SELLER ROLE CHECK
     * ============================================================
     */

    if (role == null
            || !"SELLER".equalsIgnoreCase(role)) {

        response.sendRedirect(
                request.getContextPath()
                        + "/login.jsp?error=Seller+access+required"
        );

        return;
    }


    /*
     * ============================================================
     * SUCCESS / ERROR MESSAGES
     * ============================================================
     */

    String success =
            request.getParameter("success");

    String error =
            request.getParameter("error");
%>


<!DOCTYPE html>

<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Seller Dashboard - RashikMart</title>


    <!-- ========================================================
         GLOBAL STYLESHEET
         ======================================================== -->

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">

</head>


<body>


<!-- ============================================================
     NAVIGATION BAR
     ============================================================ -->

<nav class="navbar">


    <!-- BRAND -->

    <a
            href="<%= request.getContextPath() %>/seller/dashboard.jsp"
            class="nav-brand"
    >
        RASHIKMART
    </a>


    <!-- NAVIGATION LINKS -->

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



<!-- ============================================================
     SELLER PAGE
     ============================================================ -->

<main class="seller-page">


    <div class="seller-container">


        <!-- ====================================================
             SELLER HEADER
             ==================================================== -->

        <section class="seller-header">


            <!-- SELLER INTRODUCTION -->

            <div class="seller-introduction">


                <span class="eyebrow">
                    SELLER PANEL
                </span>


                <h1>
                    Welcome, <%= userName %>
                </h1>


                <p>
                    Manage your products and marketplace listings.
                </p>


            </div>


            <!-- SELLER ROLE -->

            <div class="seller-role">

                <span>
                    ACCOUNT TYPE
                </span>

                <strong>
                    SELLER
                </strong>

            </div>


        </section>



        <!-- ====================================================
             SUCCESS MESSAGE
             ==================================================== -->

        <% if (success != null && !success.trim().isEmpty()) { %>

            <div class="message success">

                <%= success %>

            </div>

        <% } %>



        <!-- ====================================================
             ERROR MESSAGE
             ==================================================== -->

        <% if (error != null && !error.trim().isEmpty()) { %>

            <div class="message error">

                <%= error %>

            </div>

        <% } %>



        <!-- ====================================================
             PRODUCT MANAGEMENT SECTION
             ==================================================== -->

        <section class="seller-section">


            <!-- SECTION HEADING -->

            <div class="section-heading">


                <div>


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


                </div>


                <!-- ADD PRODUCT BUTTON -->

                <a
                        href="<%= request.getContextPath() %>/seller/add-product.jsp"
                        class="seller-primary-button"
                >
                    ADD NEW PRODUCT
                </a>


            </div>



            <!-- ==================================================
                 SELLER FEATURES
                 ================================================== -->

            <div class="seller-feature-grid">


                <!-- FEATURE 01 -->

                <article class="seller-feature-card">


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


                </article>



                <!-- FEATURE 02 -->

                <article class="seller-feature-card">


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


                </article>



                <!-- FEATURE 03 -->

                <article class="seller-feature-card">


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


                </article>


            </div>



            <!-- ==================================================
                 WEEK 2 STATUS
                 ================================================== -->

            <section class="seller-status-card">


                <div>


                    <span class="eyebrow">
                        WEEK 2
                    </span>


                    <h2>
                        Product Management
                    </h2>


                    <p>
                        The seller workflow is ready.
                        Next, sellers can add products,
                        define pricing and manage available
                        stock for the marketplace.
                    </p>


                </div>


                <span class="status-badge">
                    IN PROGRESS
                </span>


            </section>


        </section>


    </div>


</main>



<!-- ============================================================
     FOOTER
     ============================================================ -->

<footer class="footer">

    <p>
        © 2026 RashikMart. All rights reserved.
    </p>

</footer>


</body>

</html>