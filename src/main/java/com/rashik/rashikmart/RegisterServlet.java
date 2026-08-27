package com.rashik.rashikmart;

import com.rashik.rashikmart.dao.UserDAO;
import com.rashik.rashikmart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // -------------------------
        // 1. Get form data
        // -------------------------

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        // -------------------------
        // 2. Validate fields
        // -------------------------

        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()
                || role == null || role.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/register.jsp?error=Please+fill+all+fields"
            );

            return;
        }

        name = name.trim();
        email = email.trim().toLowerCase();
        role = role.trim().toUpperCase();

        // -------------------------
        // 3. Validate role
        // -------------------------

        if (!role.equals("BUYER") && !role.equals("SELLER")) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/register.jsp?error=Invalid+role"
            );

            return;
        }

        // -------------------------
        // 4. Check duplicate email
        // -------------------------

        User existingUser = userDAO.findByEmail(email);

        if (existingUser != null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/register.jsp?error=Email+already+exists"
            );

            return;
        }

        // -------------------------
        // 5. Create User
        // -------------------------

        User user = new User();

        user.setName(name);
        user.setEmail(email);

        // For Week 1 we are storing the password
        // in the passwordHash field.
        user.setPasswordHash(password);

        user.setRole(role);

        // -------------------------
        // 6. Save user
        // -------------------------

        boolean registered = userDAO.registerUser(user);

        // -------------------------
        // 7. Redirect
        // -------------------------

        if (registered) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?success=Registration+successful"
            );

        } else {

            response.sendRedirect(
                    request.getContextPath()
                            + "/register.jsp?error=Registration+failed"
            );
        }
    }
}