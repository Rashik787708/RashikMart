<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - RashikMart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=3">
</head>
<body>

<!-- ================= NAVBAR ================= -->
<nav class="navbar">
    <div class="nav-brand">RashikMart</div>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/login.jsp" class="nav-link">
            Login
        </a>
        <a href="${pageContext.request.contextPath}/register.jsp" class="nav-link active">
            Register
        </a>
    </div>
</nav>

<!-- ================= MAIN ================= -->
<main class="page-wrapper">
    <div class="register-card">

        <div class="card-header">
            <span class="eyebrow">JOIN RASHIKMART</span>
            <h1>Create Account</h1>
            <p class="subtitle">Choose your account type and get started</p>
        </div>

        <!-- ================= ERROR MESSAGE ================= -->
        <%
            String error = request.getParameter("error");
            if (error != null && !error.trim().isEmpty()) {
        %>
            <div class="message error"><%= error %></div>
        <%
            }
        %>

        <!-- ================= REGISTER FORM ================= -->
        <form action="${pageContext.request.contextPath}/RegisterServlet"
              method="post"
              class="register-form">

            <div class="form-group">
                <label for="name">Name</label>
                <input type="text" id="name" name="name"
                       placeholder="Enter your name" autocomplete="name" required>
            </div>

            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email"
                       placeholder="Enter your email" autocomplete="email" required>
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password"
                       placeholder="Create a password" autocomplete="new-password" required>
            </div>

            <!-- ================= ROLE ================= -->
            <div class="form-group role-group">
                <label class="role-title">Register as</label>

                <div class="role-slider">
                    <input type="radio" id="buyer" name="role" value="BUYER" checked>
                    <input type="radio" id="seller" name="role" value="SELLER">

                    <div class="role-track">
                        <span class="role-indicator"></span>
                        <label for="buyer" class="role-option buyer-option">Buyer</label>
                        <label for="seller" class="role-option seller-option">Seller</label>
                    </div>
                </div>
            </div>

            <button type="submit" class="register-button">Create Account</button>

        </form>

        <div class="account-link">
            Already have an account?
            <a href="${pageContext.request.contextPath}/login.jsp">Login</a>
        </div>

    </div>
</main>

<footer class="footer">
    © 2026 RashikMart
</footer>

</body>
</html>