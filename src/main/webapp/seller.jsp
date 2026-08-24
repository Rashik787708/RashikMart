<%@ page session="true" %>

<%
    String role = (String) session.getAttribute("role");

    if (role == null || !"SELLER".equalsIgnoreCase(role)) {
        response.sendRedirect("login.jsp?error=Please+login+first");
        return;
    }
%>

<!DOCTYPE html>
<html>

<head>
    <title>Seller Dashboard - RashikMart</title>

    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/css/style.css">
</head>

<body>

<div class="container">

    <h1>Seller Dashboard</h1>

    <h2>
        Welcome,
        <%= session.getAttribute("userName") %>
    </h2>

    <p>
        Email:
        <%= session.getAttribute("userEmail") %>
    </p>

    <p>
        Role: SELLER
    </p>

    <hr>

    <h3>Seller features will be added later.</h3>

    <a href="logout">Logout</a>

</div>

</body>
</html>