<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260824">
</head>
<body>

    <header class="navbar">
        <a href="${pageContext.request.contextPath}/" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/login.jsp" class="nav-link">Login</a></li>
                <li><a href="${pageContext.request.contextPath}/register.jsp" class="nav-link active">Register</a></li>
            </ul>
        </nav>
    </header>

    <main class="page">
        <div class="auth-card">
            <div class="auth-header">
                <span class="eyebrow">Get Started</span>
                <h2>Create Account</h2>
            </div>

            <%
                String error = request.getParameter("error");
                if (error != null) {
                    String safeError = error.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            %>
                <div class="message error"><%= safeError %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/RegisterServlet" method="post">

                <div class="role-selector">
                    <div class="role-option">
                        <input type="radio" id="role-buyer" name="role" value="BUYER" checked>
                        <label for="role-buyer">Buyer</label>
                    </div>
                    <div class="role-option">
                        <input type="radio" id="role-seller" name="role" value="SELLER">
                        <label for="role-seller">Seller</label>
                    </div>
                </div>

                <div class="form-group">
                    <label for="name">Full Name</label>
                    <input type="text" id="name" name="name" placeholder="enter your full name" required>
                </div>

                <div class="form-group">
                    <label for="email">Email Address</label>
                    <input type="email" id="email" name="email" placeholder="enter your email" required>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" placeholder="create a password" required>
                </div>

                <button type="submit" class="primary-button">Create Account</button>
            </form>

            <div class="auth-footer">
                <p>Already have an account? <a href="${pageContext.request.contextPath}/login.jsp">Login here</a></p>
            </div>
        </div>
    </main>

</body>
</html>