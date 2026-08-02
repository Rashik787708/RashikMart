<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>RashikMart | Register</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <div class="login-container">
            <h1>RashikMart</h1>
            <h2>Register</h2>
            <form action="RegisterServlet" method="post">
                <label>Full Name</label><br />
                <input type="text" name="fullName" placeholder="Enter your full name" required/>
                <br/><br/>
                <label>Email</label><br />
                <input type="email" name="email" placeholder="Enter your email" required/>
                <br/><br/>
                <label>Password</label><br />
                <input type="password" name="password" placeholder="Create a password" required/>
                <br/><br/>
                <label>Confirm Password</label><br />
                <input type="password" name="confirmPassword" placeholder="Confirm your password" required/>
                <br/><br/>
                <button type="submit">Register</button>
            </form>
            <p>
                Already have an account?
                <a href="login.jsp">Login</a>
            </p>
        </div>
    </body>
</html>