package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.UserDAO;
import com.rashik.rashikmart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null
                || email.trim().isEmpty()
                || password.trim().isEmpty()) {

            response.sendRedirect(
                    "login.jsp?error=Please+enter+email+and+password"
            );

            return;
        }

        // Special admin login
        if (email.equals("RashikMart@admin")
                && password.equals("Rashik@7877")) {

            HttpSession session = request.getSession();

            session.setAttribute("userName", "Admin");
            session.setAttribute("userEmail", email);
            session.setAttribute("role", "ADMIN");

            response.sendRedirect("admin.jsp");

            return;
        }

        // Normal buyer/seller login
        User user = userDAO.loginUser(
                email.trim(),
                password
        );

        if (user == null) {

            response.sendRedirect(
                    "login.jsp?error=Invalid+email+or+password"
            );

            return;
        }

        HttpSession session = request.getSession();

        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("role", user.getRole());

        if ("BUYER".equalsIgnoreCase(user.getRole())) {

            response.sendRedirect("buyer.jsp");

        } else if ("SELLER".equalsIgnoreCase(user.getRole())) {

            response.sendRedirect("seller.jsp");

        } else {

            response.sendRedirect("login.jsp?error=Invalid+user+role");
        }
    }
}