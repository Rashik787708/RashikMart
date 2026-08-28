<%@ page contentType="text/html;charset=UTF-8" %>

<%
    /*
     * ==============================
     * AUTHENTICATION CHECK
     * ==============================
     */

    if (session.getAttribute("user") == null) {
        response.sendRedirect(
                request.getContextPath()
                        + "/login.jsp?error=Please+login+first"
        );
        return;
    }

    /*
     * ==============================
     * SESSION DATA
     * ==============================
     */

    String role = (String) session.getAttribute("role");

    String userName = (String) session.getAttribute("userName");

    /*
     * Some versions of the login system may store the
     * logged-in user object instead of userName.
     *
     * Therefore, try to obtain the name from the user object
     * if userName is not available.
     */

    if (userName == null || userName.trim().isEmpty()) {

        Object userObject = session.getAttribute("user");

        if (userObject != null) {
            try {
                java.lang.reflect.Method method =
                        userObject.getClass().getMethod("getName");

                Object nameObject = method.invoke(userObject);

                if (nameObject != null) {
                    userName = nameObject.toString();
                }

            } catch (Exception ignored) {
                // Keep fallback value below.
            }
        }
    }

    if (userName == null || userName.trim().isEmpty()) {
        userName = "Seller";
    }

    if (role == null || role.trim().isEmpty()) {
        role = "SELLER";
    }
%>


<!DOCTYPE html>

<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Seller Dashboard - RashikMart</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">

</head>


<body>


<!-- =====================================================
     NAVIGATION BAR
     ===================================================== -->

<header class="navbar">

    <a href="${pageContext.request.contextPath}/"
       class="brand">
        RashikMart
    </a>


    <nav>

        <ul class="nav-links">

            <li>
                <a href="${pageContext.request.contextPath}/seller/dashboard.jsp"
                   class="nav-link active">
                    Dashboard
                </a>
            </li>


            <li>
                <a href="#product-management"
                   class="nav-link">
                    Products
                </a>
            </li>


            <li>
                <a href="${pageContext.request.contextPath}/logout"
                   class="nav-link">
                    Logout
                </a>
            </li>

        </ul>

    </nav>

</header>



<!-- =====================================================
     MAIN SELLER PAGE
     ===================================================== -->

<main class="seller-page">


    <div class="seller-container">


        <!-- =================================================
             SELLER HEADER
             ================================================= -->

        <section class="seller-header">


            <div class="seller-introduction">

                <span class="eyebrow">
                    Seller Panel
                </span>


                <h1>
                    Welcome, <%= userName %>
                </h1>


                <p>
                    Manage your products and marketplace listings.
                </p>

            </div>


            <div class="seller-role">

                <span>
                    ROLE
                </span>

                <strong>
                    <%= role %>
                </strong>

            </div>


        </section>



        <!-- =================================================
             PRODUCT MANAGEMENT
             ================================================= -->

        <section class="seller-section"
                 id="product-management">


            <div class="section-heading">


                <div>

                    <span class="eyebrow">
                        Product Management
                    </span>


                    <h2>
                        Start selling on RashikMart
                    </h2>


                    <p>
                        Create product listings, set pricing
                        and manage available stock.
                    </p>

                </div>


                <!--
                    This link will be connected to the
                    Add Product JSP when we create it.
                -->

                <a href="${pageContext.request.contextPath}/seller/add-product.jsp"
                   class="seller-primary-button">
                    Add New Product
                </a>


            </div>



            <!-- =================================================
                 SELLER FEATURE CARDS
                 ================================================= -->

            <div class="seller-feature-grid">


                <!-- CARD 01 -->

                <article class="seller-feature-card">


                    <span class="feature-number">
                        01
                    </span>


                    <h3>
                        Add Products
                    </h3>


                    <p>
                        Create product listings with name,
                        category, price and quantity.
                    </p>


                </article>



                <!-- CARD 02 -->

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



                <!-- CARD 03 -->

                <article class="seller-feature-card">


                    <span class="feature-number">
                        03
                    </span>


                    <h3>
                        Reach Buyers
                    </h3>


                    <p>
                        Your products will become available
                        in the buyer marketplace.
                    </p>


                </article>


            </div>


        </section>



        <!-- =================================================
             WEEK 2 STATUS
             ================================================= -->

        <section class="seller-status-card">


            <div>

                <span class="eyebrow">
                    Week 2
                </span>


                <h2>
                    Product Management
                </h2>


                <p>
                    Build the seller product workflow:
                    create products, store them in the database
                    and display them in the seller dashboard.
                </p>


            </div>


            <div class="status-badge">
                IN PROGRESS
            </div>


        </section>


    </div>


</main>



<!-- =====================================================
     FOOTER
     ===================================================== -->

<footer class="footer">

    <p>
        © 2026 RashikMart. All rights reserved.
    </p>

</footer>


</body>

</html>