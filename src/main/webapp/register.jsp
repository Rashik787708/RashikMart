<!DOCTYPE html>
<html>
<head>

    <title>Register - RashikMart</title>

    <!-- IMPORTANT -->
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/css/style.css">

</head>

<body>


<header class="navbar">

    <div class="brand">
        RashikMart
    </div>

    <nav>

        <a href="login.jsp" class="nav-link">
            Login
        </a>

        <a href="register.jsp" class="nav-link active">
            Register
        </a>

    </nav>

</header>



<main class="page">

    <div class="auth-card register-card">


        <div class="auth-header">

            <div class="eyebrow">
                JOIN RASHIKMART
            </div>

            <h1>Create Account</h1>

            <p>
                Choose your account type and get started
            </p>

        </div>



        <%
            String error = request.getParameter("error");

            if (error != null) {
        %>

            <div class="message error">
                <%= error %>
            </div>

        <%
            }
        %>



        <form action="RegisterServlet" method="post">


            <div class="form-group">

                <label for="name">
                    Name
                </label>

                <input
                    type="text"
                    id="name"
                    name="name"
                    placeholder="Enter your name"
                    required
                >

            </div>



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
                    placeholder="Create a password"
                    required
                >

            </div>



            <!-- ROLE SELECTOR -->

            <div class="form-group">

                <label>
                    Register as
                </label>


                <div class="role-selector">

                    <input
                        type="radio"
                        id="registerBuyer"
                        name="role"
                        value="BUYER"
                        checked
                    >

                    <label for="registerBuyer">
                        Buyer
                    </label>


                    <input
                        type="radio"
                        id="registerSeller"
                        name="role"
                        value="SELLER"
                    >

                    <label for="registerSeller">
                        Seller
                    </label>


                    <div class="role-slider"></div>

                </div>

            </div>



            <button
                type="submit"
                class="primary-button">

                Create Account

            </button>


        </form>



        <div class="auth-footer">

            Already have an account?

            <a href="login.jsp">
                Login
            </a>

        </div>


    </div>

</main>



<footer class="footer">

    © 2026 RashikMart

</footer>


</body>
</html>