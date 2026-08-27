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
    <title>Buyer Dashboard - RashikMart</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<div class="container">

    <h1>RashikMart</h1>

    <h2>Buyer Dashboard</h2>

    <p>
        Welcome to RashikMart.
    </p>

    <p>
        Role:
        <strong><%= role %></strong>
    </p>

    <hr>

    <p>
        Product browsing and cart features will be added in Week 2.
    </p>

    <a href="<%= request.getContextPath() %>/logout">
        Logout
    </a>

</div>

</body>
</html>