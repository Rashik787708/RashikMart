<!DOCTYPE html>
<html>
<head>
    <title>Login - RashikMart</title>

    <!-- IMPORTANT: context path makes CSS work correctly in Tomcat -->
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="brand">
        RashikMart
    </div>

    <nav>
        <a href="login.jsp" class="nav-link active">Login</a>
        <a href="register.jsp" class="nav-link">Register</a>
    </nav>

</header>


<main class="page">

    <div class="auth-card">

        <div class="auth-header">

            <div class="eyebrow">
                WELCOME BACK
            </div>

            <h1>Login</h1>

            <p>
                Sign in to continue to RashikMart
            </p>

        </div>


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


        <form action="login" method="post">

            <div class="form-group">

                <label for="email">
                    Email
                </label>

                <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="Enter your email"
                    required
                >

            </div>


            <div class="form-group">

                <label for="password">
                    Password
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Enter your password"
                    required
                >

            </div>


            <!-- Role selection -->

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


            <button type="submit" class="primary-button">
                Login
            </button>

        </form>


        <div class="auth-footer">

            Don't have an account?

            <a href="register.jsp">
                Create one
            </a>

        </div>

    </div>

</main>


<footer class="footer">
    © 2026 RashikMart
</footer>

</body>
</html>