package com.rashik.rashikmart.dao;

import com.rashik.rashikmart.config.DatabaseConfig;
import com.rashik.rashikmart.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // ==========================================
    // FIND USER BY EMAIL
    // ==========================================

    public User findByEmail(String email) {

        String sql =
                "SELECT id, name, email, password_hash, role " +
                        "FROM users " +
                        "WHERE email = ?";

        try (Connection connection =
                     DatabaseConfig.getDataSource().getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    User user = new User();

                    user.setId(
                            resultSet.getInt("id")
                    );

                    user.setName(
                            resultSet.getString("name")
                    );

                    user.setEmail(
                            resultSet.getString("email")
                    );

                    user.setPasswordHash(
                            resultSet.getString("password_hash")
                    );

                    user.setRole(
                            resultSet.getString("role")
                    );

                    return user;
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error finding user by email: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return null;
    }


    // ==========================================
    // REGISTER USER
    // ==========================================

    public boolean registerUser(User user) {

        String sql =
                "INSERT INTO users " +
                        "(name, email, password_hash, role) " +
                        "VALUES (?, ?, ?, ?)";

        try (Connection connection =
                     DatabaseConfig.getDataSource().getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    user.getName()
            );

            statement.setString(
                    2,
                    user.getEmail()
            );

            statement.setString(
                    3,
                    user.getPasswordHash()
            );

            statement.setString(
                    4,
                    user.getRole()
            );

            int rows = statement.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error registering user: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }


    // ==========================================
    // VERIFY PASSWORD
    // ==========================================

    public boolean verifyPassword(
            String plainPassword,
            String passwordHash) {

        if (plainPassword == null
                || passwordHash == null) {

            return false;
        }

        // Week 1 implementation.
        // Password hashing can be added later.

        return plainPassword.equals(passwordHash);
    }
}