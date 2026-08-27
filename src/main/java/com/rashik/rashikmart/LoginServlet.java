package com.rashik.rashikmart;

import com.rashik.rashikmart.dao.UserDAO;
import com.rashik.rashikmart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
                request.getContextPath() + "/login.jsp"
        );
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // =========================
        // 1. GET FORM DATA
        // =========================

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // =========================
        // 2. BASIC VALIDATION
        // =========================

        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Please+enter+email+and+password"
            );

            return;
        }

        email = email.trim();

        // =========================
        // 3. FIND USER
        // =========================

        User user = userDAO.findByEmail(email);

        if (user == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Invalid+email+or+password"
            );

            return;
        }

        // =========================
        // 4. VERIFY PASSWORD
        // =========================

        boolean passwordCorrect =
                userDAO.verifyPassword(
                        password,
                        user.getPasswordHash()
                );

        if (!passwordCorrect) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Invalid+email+or+password"
            );

            return;
        }

        // =========================
        // 5. CREATE SESSION
        // =========================

        HttpSession session = request.getSession();

        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userRole", user.getRole());

        // =========================
        // 6. ROLE-BASED REDIRECT
        // =========================

        String role = user.getRole();

        if ("BUYER".equalsIgnoreCase(role)) {

            response.sendRedirect(
                    request.getContextPath() + "/buyer/dashboard.jsp"
            );

        } else if ("SELLER".equalsIgnoreCase(role)) {

            response.sendRedirect(
                    request.getContextPath() + "/seller/dashboard.jsp"
            );

        } else if ("ADMIN".equalsIgnoreCase(role)) {

            response.sendRedirect(
                    request.getContextPath() + "/admin/dashboard.jsp"
            );

        } else {

            // Unknown role
            session.invalidate();

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Invalid+user+role"
            );
        }
    }
}