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

        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Validation
        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Please+enter+email+and+password"
            );
            return;
        }

        email = email.trim();

        // Find user
        User user = userDAO.findByEmail(email);

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Invalid+email+or+password"
            );
            return;
        }

        // Verify password
        boolean passwordCorrect = userDAO.verifyPassword(password, user.getPassword());

        if (!passwordCorrect) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Invalid+email+or+password"
            );
            return;
        }

        // Create session (important: set attributes that dashboard & filter expect)
        HttpSession session = request.getSession();
        session.setAttribute("user", user);                 // used by dashboard.jsp
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("role", user.getRole());       // used by dashboard.jsp & AuthFilter
        session.setAttribute("userRole", user.getRole());

        // ===== FIXED ROLE-BASED REDIRECT =====
        String role = user.getRole();

        if ("BUYER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/buyer/dashboard.jsp");

        } else if ("SELLER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/seller/dashboard.jsp");

        } else if ("ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");

        } else {
            session.invalidate();
            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Invalid+user+role"
            );
        }
    }
}