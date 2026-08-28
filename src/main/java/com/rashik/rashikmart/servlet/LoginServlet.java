package com.rashik.rashikmart.servlet;

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

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        /*
         * -----------------------------
         * VALIDATION
         * -----------------------------
         */

        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Please+enter+email+and+password"
            );

            return;
        }

        email = email.trim();

        /*
         * -----------------------------
         * FIND USER
         * -----------------------------
         */

        User user = userDAO.findByEmail(email);

        if (user == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Invalid+email+or+password"
            );

            return;
        }

        /*
         * -----------------------------
         * VERIFY PASSWORD
         * -----------------------------
         */

        boolean passwordCorrect =
                userDAO.verifyPassword(
                        password,
                        user.getPassword()
                );

        if (!passwordCorrect) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Invalid+email+or+password"
            );

            return;
        }

        /*
         * -----------------------------
         * CREATE SESSION
         * -----------------------------
         *
         * IMPORTANT:
         *
         * "user" stores the complete User object.
         *
         * "userName", "userEmail", "role"
         * store individual values.
         */

        HttpSession session = request.getSession();

        session.setAttribute("user", user);

        session.setAttribute(
                "userId",
                user.getId()
        );

        session.setAttribute(
                "userName",
                user.getName()
        );

        session.setAttribute(
                "userEmail",
                user.getEmail()
        );

        session.setAttribute(
                "role",
                user.getRole()
        );

        session.setAttribute(
                "userRole",
                user.getRole()
        );

        /*
         * -----------------------------
         * ROLE-BASED REDIRECT
         * -----------------------------
         */

        String role = user.getRole();

        if ("BUYER".equalsIgnoreCase(role)) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/buyer/marketplace"
            );

        } else if ("SELLER".equalsIgnoreCase(role)) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/seller/dashboard.jsp"
            );

        } else if ("ADMIN".equalsIgnoreCase(role)) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/dashboard.jsp"
            );

        } else {

            session.invalidate();

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Invalid+user+role"
            );
        }
    }
}