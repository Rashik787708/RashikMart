<%@ page session="true" %>

<%
    String role = (String) session.getAttribute("role");

    if (role == null || !"BUYER".equalsIgnoreCase(role)) {
        response.sendRedirect("login.jsp?error=Please+login+first");
        return;
    }
%>

<!DOCTYPE html>
<html>

<head>
    <title>Buyer Dashboard - RashikMart</title>
    <link rel="stylesheet" href="css/style.css">
</head>

<body>

<div class="container">

    <h1>Buyer Dashboard</h1>

    <h2>
        Welcome,
        <%= session.getAttribute("userName") %>
    </h2>

    <p>
        Email:
        <%= session.getAttribute("userEmail") %>
    </p>

    <p>
        Role: BUYER
    </p>

    <hr>

    <h3>Buyer features will be added later.</h3>

    <a href="logout">Logout</a>

</div>

</body>
</html>