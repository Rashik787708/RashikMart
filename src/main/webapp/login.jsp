<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>RashikMart | Login</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <div class="login-container">
            <h1>RashikMart</h1>
            <h2>Login</h2>
            <form action="LoginServlet" method="post">
                <label>Email</label><br />
                <input type="email" name="email" placeholder="Enter your email" required/>
                <br/><br/>
                <label>Password</label><br />
                <input type="password" name="password" placeholder="Enter your password" required />
                <br/><br/>
                <button type="submit">Login</button>
            </form>
            <p>
                Don't have an account?
                <a href="register.jsp">Register</a>
            </p>
        </div>
    </body>
</html>
