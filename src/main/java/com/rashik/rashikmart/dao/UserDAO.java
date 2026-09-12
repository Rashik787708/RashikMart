package com.rashik.rashikmart.dao;

import com.rashik.rashikmart.config.DatabaseConfig;
import com.rashik.rashikmart.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // =====================================================
    // FIND USER BY EMAIL
    // =====================================================

    public User findByEmail(String email) {

        String sql = """
                SELECT id, name, email, password, role
                FROM users
                WHERE email = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return new User(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("email"),
                            resultSet.getString("password"),
                            resultSet.getString("role")
                    );
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


    // =====================================================
    // REGISTER USER
    // =====================================================

    public boolean registerUser(User user) {

        String sql = """
                INSERT INTO users
                (name, email, password, role)
                VALUES (?, ?, ?, ?)
                """;

        String passwordToStore = user.getPassword();
        if (passwordToStore != null
                && !passwordToStore.startsWith("$2a$")
                && !passwordToStore.startsWith("$2b$")
                && !passwordToStore.startsWith("$2y$")) {
            passwordToStore = BCrypt.hashpw(passwordToStore, BCrypt.gensalt(12));
        }

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, passwordToStore);
            statement.setString(4, user.getRole());

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


    // =====================================================
    // FIND ALL USERS
    // =====================================================

    public List<User> findAll() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, name, email, password, role
                FROM users
                ORDER BY id ASC
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                users.add(new User(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        resultSet.getString("role")
                ));
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error finding all users: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return users;
    }


    // =====================================================
    // VERIFY PASSWORD
    // =====================================================

    public boolean verifyPassword(
            String enteredPassword,
            String storedPassword) {

        if (enteredPassword == null
                || storedPassword == null) {

            return false;
        }

        if (storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(enteredPassword, storedPassword);
            } catch (Exception e) {
                return false;
            }
        }

        return enteredPassword.equals(storedPassword);
    }
}