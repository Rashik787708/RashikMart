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
    <title>Seller Dashboard - RashikMart</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<div class="container">

    <h1>RashikMart</h1>

    <h2>Seller Dashboard</h2>

    <p>
        Welcome to RashikMart Seller Panel.
    </p>

    <p>
        Role:
        <strong><%= role %></strong>
    </p>

    <hr>

    <p>
        Product management features will be added in Week 2.
    </p>

    <a href="<%= request.getContextPath() %>/logout">
        Logout
    </a>

</div>

</body>
</html>