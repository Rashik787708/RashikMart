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

    String error =
            request.getParameter("error");
%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Add Product - RashikMart</title>

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
                class="nav-link"
        >
            Dashboard
        </a>

        <a
                href="<%= request.getContextPath() %>/seller/add-product.jsp"
                class="nav-link active"
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
     PAGE
========================= -->

<main class="page">

    <div class="register-card product-form-card">

        <div class="card-header">

            <span class="eyebrow">
                SELLER PANEL
            </span>

            <h2>
                Add Product
            </h2>

            <p class="subtitle">
                Create a new marketplace listing
            </p>

        </div>


        <!-- ERROR -->

        <% if (error != null) { %>

            <div class="message error">

                <%= error %>

            </div>

        <% } %>


        <!-- FORM -->

        <form
                action="<%= request.getContextPath() %>/seller/add-product"
                method="post"
                class="register-form"
        >


            <!-- PRODUCT NAME -->

            <div class="form-group">

                <label for="name">
                    Product Name
                </label>

                <input
                        type="text"
                        id="name"
                        name="name"
                        placeholder="e.g. Organic Turmeric"
                        maxlength="150"
                        required
                >

            </div>


            <!-- DESCRIPTION -->

            <div class="form-group">

                <label for="description">
                    Description
                </label>

                <textarea
                        id="description"
                        name="description"
                        placeholder="Describe your product"
                        maxlength="1000"
                        rows="4"
                ></textarea>

            </div>


            <!-- CATEGORY -->

            <div class="form-group">

                <label for="category">
                    Category
                </label>

                <select
                        id="category"
                        name="category"
                        required
                >

                    <option value="">
                        Select category
                    </option>

                    <option value="Grains">
                        Grains
                    </option>

                    <option value="Pulses">
                        Pulses
                    </option>

                    <option value="Vegetables">
                        Vegetables
                    </option>

                    <option value="Fruits">
                        Fruits
                    </option>

                    <option value="Spices">
                        Spices
                    </option>

                    <option value="Oilseeds">
                        Oilseeds
                    </option>

                    <option value="Other">
                        Other
                    </option>

                </select>

            </div>


            <!-- PRICE -->

            <div class="form-row">

                <div class="form-group">

                    <label for="price">
                        Price
                    </label>

                    <input
                            type="number"
                            id="price"
                            name="price"
                            placeholder="0.00"
                            min="0.01"
                            step="0.01"
                            required
                    >

                </div>


                <!-- QUANTITY -->

                <div class="form-group">

                    <label for="quantity">
                        Quantity
                    </label>

                    <input
                            type="number"
                            id="quantity"
                            name="quantity"
                            placeholder="0"
                            min="1"
                            required
                    >

                </div>

            </div>


            <!-- SUBMIT -->

            <button
                    type="submit"
                    class="primary-button register-button"
            >
                Add Product
            </button>


        </form>


        <div class="account-link">

            <a
                    href="<%= request.getContextPath() %>/seller/dashboard.jsp"
            >
                ← Back to Dashboard
            </a>

        </div>

    </div>

</main>


<footer class="footer">

    <p>
        © 2026 RashikMart. All rights reserved.
    </p>

</footer>


</body>

</html>