package com.rashikmart.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // Prevents HTTP 405 error by forwarding direct GET requests to login.jsp
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    // Handles POST login form submission
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email != null && !email.trim().isEmpty() && password != null && !password.trim().isEmpty()) {
            // Replace with your actual UserDAO validation logic:
            // User user = userDAO.loginUser(email.trim(), password);
            // if (user != null) { ... }

            response.sendRedirect(request.getContextPath() + "/buyer.jsp");
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Invalid+email+or+password");
        }
    }
}