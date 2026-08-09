package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.config.DatabaseConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || role == null || role.isBlank()) {

            response.sendRedirect("register.jsp?error=Please+fill+all+fields");
            return;
        }

        String sql = """
                INSERT INTO users (name, email, password, role)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConfig.getDataSource().getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, role);

            statement.executeUpdate();

            response.sendRedirect("login.jsp?success=Registration+successful");

        } catch (SQLException e) {

            // Duplicate email
            if (e.getMessage() != null &&
                    e.getMessage().toLowerCase().contains("unique")) {

                response.sendRedirect(
                        "register.jsp?error=Email+already+exists"
                );

            } else {
                throw new ServletException(
                        "Registration failed", e
                );
            }
        }
    }
}