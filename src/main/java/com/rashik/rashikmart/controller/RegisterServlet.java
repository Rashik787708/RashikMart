package com.rashik.rashikmart.controller;

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

        // Get values from register.jsp
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // For now, role is automatically assigned.
        String role = "USER";

        // Validate required fields
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {

            response.sendRedirect(
                    "register.jsp?error=Please+fill+all+fields"
            );
            return;
        }

        // Check password confirmation
        if (!password.equals(confirmPassword)) {

            response.sendRedirect(
                    "register.jsp?error=Passwords+do+not+match"
            );
            return;
        }

        String sql = """
                INSERT INTO userss (name, email, password, role)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, name.trim());
            statement.setString(2, email.trim());
            statement.setString(3, password);
            statement.setString(4, role);

            int rowsInserted = statement.executeUpdate();

            System.out.println(
                    "Registration INSERT completed. Rows inserted: "
                            + rowsInserted
            );

            response.sendRedirect(
                    "login.jsp?success=Registration+successful"
            );

        } catch (SQLException e) {

            e.printStackTrace();

            // Duplicate email
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")) {

                response.sendRedirect(
                        "register.jsp?error=Email+already+exists"
                );

            } else {

                throw new ServletException(
                        "Registration failed",
                        e
                );
            }
        }
    }
}