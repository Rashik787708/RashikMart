<%@ page session="true" %>

<%
    String role = (String) session.getAttribute("role");

    if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect("login.jsp?error=Access+denied");
        return;
    }
%>

<!DOCTYPE html>
<html>

<head>
    <title>Admin Dashboard - RashikMart</title>
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/css/style.css">
</head>

<body>

<div class="container">

    <h1>RashikMart Admin Dashboard</h1>

    <h2>Welcome, Admin</h2>

    <p>
        Admin Email:
        <%= session.getAttribute("userEmail") %>
    </p>

    <p>Role: ADMIN</p>

    <hr>

    <h3>Admin Dashboard</h3>

    <p>
        User management and other admin features
        will be added later.
    </p>

    <a href="logout">Logout</a>

</div>

</body>
</html>