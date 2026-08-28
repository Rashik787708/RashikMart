package com.rashik.rashikmart.dao;

import com.rashik.rashikmart.config.DatabaseConfig;
import com.rashik.rashikmart.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // =========================
    // ADD PRODUCT
    // =========================

    public boolean addProduct(Product product) {

        String sql = """
                INSERT INTO products
                (seller_id, name, description, category, price, quantity)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, product.getSellerId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setString(4, product.getCategory());
            statement.setBigDecimal(5, product.getPrice());
            statement.setInt(6, product.getQuantity());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error adding product: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =========================
    // FIND PRODUCT BY ID
    // =========================

    public Product findById(int id) {

        String sql = """
                SELECT id,
                       seller_id,
                       name,
                       description,
                       category,
                       price,
                       quantity
                FROM products
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapProduct(resultSet);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error finding product: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return null;
    }

    // =========================
    // FIND PRODUCTS BY SELLER
    // =========================

    public List<Product> findBySellerId(int sellerId) {

        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT id,
                       seller_id,
                       name,
                       description,
                       category,
                       price,
                       quantity
                FROM products
                WHERE seller_id = ?
                ORDER BY id DESC
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, sellerId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    products.add(
                            mapProduct(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error finding seller products: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return products;
    }

    // =========================
    // FIND ALL PRODUCTS
    // =========================

    public List<Product> findAll() {

        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT id,
                       seller_id,
                       name,
                       description,
                       category,
                       price,
                       quantity
                FROM products
                ORDER BY id DESC
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                products.add(
                        mapProduct(resultSet)
                );
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error finding products: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return products;
    }

    // =========================
    // MAP RESULT SET
    // =========================

    private Product mapProduct(
            ResultSet resultSet
    ) throws SQLException {

        return new Product(
                resultSet.getInt("id"),
                resultSet.getInt("seller_id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("category"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("quantity")
        );
    }
}