<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260828_4">
</head>
<body>

    <header class="navbar">
        <a href="${pageContext.request.contextPath}/" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/" class="nav-link">Home</a></li>
                <li><a href="${pageContext.request.contextPath}/login.jsp" class="nav-link">Login</a></li>
            </ul>
        </nav>
    </header>

    <main class="page">
        <div class="auth-card" style="max-width: 520px; text-align: center;">
            <div class="auth-header">
                <span class="eyebrow" style="color: #c0392b;">REQUEST NOTICE</span>
                <h2>Something Went Wrong</h2>
            </div>

            <p style="color: #555; margin: 1rem 0 1.8rem; line-height: 1.6; font-size: 0.95rem;">
                The request could not be completed as expected. An error was encountered while processing your request. Please return to the homepage or try again shortly.
            </p>

            <div style="display: flex; gap: 1rem; justify-content: center;">
                <a href="${pageContext.request.contextPath}/" class="primary-button" style="display: inline-block; text-decoration: none; padding: 0.85rem 1.5rem;">
                    Go to Homepage
                </a>
                <a href="javascript:history.back()" class="secondary-button" style="display: inline-block; text-decoration: none; padding: 0.85rem 1.5rem;">
                    Go Back
                </a>
            </div>
        </div>
    </main>

    <footer class="footer">
        <p>© 2026 RashikMart. All rights reserved.</p>
    </footer>

</body>
</html>
