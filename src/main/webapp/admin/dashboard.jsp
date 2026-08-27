<%@ page contentType="text/html;charset=UTF-8" %>

<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(
                request.getContextPath()
                        + "/login.jsp?error=Please+login+first"
        );
        return;
    }

    String role = (String) session.getAttribute("role");
%>

<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard - RashikMart</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<div class="container">

    <h1>RashikMart</h1>

    <h2>Admin Dashboard</h2>

    <p>
        Welcome to the RashikMart Admin Panel.
    </p>

    <p>
        Role:
        <strong><%= role %></strong>
    </p>

    <hr>

    <p>
        Administration features will be added later.
    </p>

    <a href="<%= request.getContextPath() %>/logout">
        Logout
    </a>

</div>

</body>
</html>