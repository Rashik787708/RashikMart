package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.UserDAO;
import com.rashik.rashikmart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.IOException;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        // Validation
        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || role == null || role.trim().isEmpty()) {

            response.sendRedirect(
                    "register.jsp?error=Please+fill+all+fields"
            );

            return;
        }

        // Only allow buyer or seller registration
        if (!role.equalsIgnoreCase("BUYER")
                && !role.equalsIgnoreCase("SELLER")) {

            response.sendRedirect(
                    "register.jsp?error=Invalid+role"
            );

            return;
        }

        User user = new User(
                name.trim(),
                email.trim(),
                password,
                role.toUpperCase()
        );

        boolean registered = userDAO.registerUser(user);

        if (registered) {

            response.sendRedirect(
                    "login.jsp?success=Registration+successful"
            );

        } else {

            response.sendRedirect(
                    "register.jsp?error=Email+already+exists+or+registration+failed"
            );
        }
    }
}