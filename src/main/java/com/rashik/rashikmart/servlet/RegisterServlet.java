package com.rashik.rashikmart.servlet;

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

    private final UserDAO userDAO = new UserDAO();


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // =====================================================
        // 1. GET FORM DATA
        // =====================================================

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");


        // =====================================================
        // 2. VALIDATE INPUT
        // =====================================================

        if (name == null
                || name.trim().isEmpty()
                || email == null
                || email.trim().isEmpty()
                || password == null
                || password.trim().isEmpty()
                || role == null
                || role.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/register.jsp?error=Please+fill+all+fields"
            );

            return;
        }


        // =====================================================
        // 3. CLEAN INPUT
        // =====================================================

        name = name.trim();
        email = email.trim().toLowerCase();
        password = password.trim();
        role = role.trim().toUpperCase();


        // =====================================================
        // 4. VALIDATE ROLE
        // =====================================================

        if (!role.equals("BUYER")
                && !role.equals("SELLER")) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/register.jsp?error=Invalid+role"
            );

            return;
        }


        // =====================================================
        // 5. CHECK DUPLICATE EMAIL
        // =====================================================

        User existingUser = userDAO.findByEmail(email);

        if (existingUser != null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/register.jsp?error=Email+already+exists"
            );

            return;
        }


        // =====================================================
        // 6. CREATE USER
        // =====================================================

        User user = new User(
                name,
                email,
                password,
                role
        );


        // =====================================================
        // 7. SAVE USER
        // =====================================================

        boolean registered =
                userDAO.registerUser(user);


        // =====================================================
        // 8. RESULT
        // =====================================================

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