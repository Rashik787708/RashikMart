<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260824">
</head>
<body>

    <header class="navbar">
        <a href="${pageContext.request.contextPath}/" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/login.jsp" class="nav-link active">Login</a></li>
                <li><a href="${pageContext.request.contextPath}/register.jsp" class="nav-link">Register</a></li>
            </ul>
        </nav>
    </header>

    <main class="page">
        <div class="auth-card">
            <div class="auth-header">
                <span class="eyebrow">Welcome Back</span>
                <h2>Account Login</h2>
            </div>

            <%
                String error = request.getParameter("error");
                String success = request.getParameter("success");
                if (error != null) {
                    String safeError = error.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            %>
                <div class="message error"><%= safeError %></div>
            <% } %>

            <%
                if (success != null) {
                    String safeSuccess = success.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            %>
                <div class="message success"><%= safeSuccess %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/login" method="post">
                <div class="form-group">
                    <label for="email">Email Address</label>
                    <input type="email" id="email" name="email" placeholder="enter your email" required>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" placeholder="enter your password" required>
                </div>

                <button type="submit" class="primary-button">Log In</button>
            </form>

            <div class="auth-footer">
                <p>Don't have an account? <a href="${pageContext.request.contextPath}/register.jsp">Register here</a></p>
            </div>
        </div>
    </main>

</body>
</html>