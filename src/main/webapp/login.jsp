<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Login - RashikMart</title>

    <!-- FORCE TOMCAT TO LOAD THE CURRENT CSS -->
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/css/style.css?v=20260824">

</head>


<body>

<!-- ================= NAVBAR ================= -->

<header class="navbar">

    <div class="brand">
        RashikMart
    </div>

    <nav>

        <a href="login.jsp"
           class="nav-link active">
            Login
        </a>

        <a href="register.jsp"
           class="nav-link">
            Register
        </a>

    </nav>

</header>


<!-- ================= MAIN ================= -->

<main class="page">

    <div class="auth-card">


        <!-- HEADER -->

        <div class="auth-header">

            <div class="eyebrow">
                WELCOME BACK
            </div>

            <h1>
                Login
            </h1>

            <p>
                Sign in to continue to RashikMart
            </p>

        </div>


        <!-- ================= MESSAGES ================= -->

        <%
            String error = request.getParameter("error");
            String success = request.getParameter("success");

            if (error != null) {
        %>

            <div class="message error">
                <%= error %>
            </div>

        <%
            }

            if (success != null) {
        %>

            <div class="message success">
                <%= success %>
            </div>

        <%
            }
        %>


        <!-- ================= LOGIN FORM ================= -->

        <form action="<%= request.getContextPath() %>/login"
              method="post">


            <!-- EMAIL -->

            <div class="form-group">

                <label for="email">
                    Email
                </label>

                <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="Enter your email"
                    autocomplete="email"
                    required
                >

            </div>


            <!-- PASSWORD -->

            <div class="form-group">

                <label for="password">
                    Password
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Enter your password"
                    autocomplete="current-password"
                    required
                >

            </div>


            <!-- ================= ROLE ================= -->

            <div class="form-group">

                <label>
                    Login as
                </label>


                <div class="role-selector">


                    <input
                        type="radio"
                        id="buyer"
                        name="role"
                        value="BUYER"
                        checked
                    >

                    <label for="buyer">
                        Buyer
                    </label>


                    <input
                        type="radio"
                        id="seller"
                        name="role"
                        value="SELLER"
                    >

                    <label for="seller">
                        Seller
                    </label>


                    <div class="role-slider"></div>

                </div>

            </div>


            <!-- ================= BUTTON ================= -->

            <button
                type="submit"
                class="primary-button">

                Login

            </button>

        </form>


        <!-- ================= REGISTER LINK ================= -->

        <div class="auth-footer">

            Don't have an account?

            <a href="register.jsp">
                Create one
            </a>

        </div>


    </div>

</main>


<!-- ================= FOOTER ================= -->

<footer class="footer">

    © 2026 RashikMart

</footer>


</body>

</html>