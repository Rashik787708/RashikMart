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
    // ADD PRODUCT (CREATE)
    // =========================

    public boolean addProduct(Product product) {

        String sql = """
                INSERT INTO products
                (seller_id, name, description, category, price, quantity, image_url)
                VALUES (?, ?, ?, ?, ?, ?, ?)
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
            statement.setString(7, product.getImageUrl());

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
    // FIND PRODUCT BY ID (READ)
    // =========================

    public Product findById(int id) {

        String sql = """
                SELECT id,
                       seller_id,
                       name,
                       description,
                       category,
                       price,
                       quantity,
                       image_url
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

    // ============================================
    // FIND PRODUCT BY ID AND SELLER ID (READ/AUTH)
    // ============================================

    public Product findByIdAndSellerId(int id, int sellerId) {

        String sql = """
                SELECT id,
                       seller_id,
                       name,
                       description,
                       category,
                       price,
                       quantity,
                       image_url
                FROM products
                WHERE id = ? AND seller_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);
            statement.setInt(2, sellerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapProduct(resultSet);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error finding product by id and seller: "
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
                       quantity,
                       image_url
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
                       quantity,
                       image_url
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
    // UPDATE PRODUCT (UPDATE)
    // =========================

    public boolean updateProduct(Product product) {

        String sql = """
                UPDATE products
                SET name = ?,
                    description = ?,
                    category = ?,
                    price = ?,
                    quantity = ?,
                    image_url = ?
                WHERE id = ? AND seller_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setString(3, product.getCategory());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getQuantity());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getId());
            statement.setInt(8, product.getSellerId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error updating product: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =========================
    // DELETE PRODUCT (DELETE)
    // =========================

    public boolean deleteProduct(int id, int sellerId) {

        String sql = """
                DELETE FROM products
                WHERE id = ? AND seller_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);
            statement.setInt(2, sellerId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error deleting product: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =========================
    // MAP RESULT SET
    // =========================

    private Product mapProduct(
            ResultSet resultSet
    ) throws SQLException {

        String img = "default-product.svg";
        try {
            String dbImg = resultSet.getString("image_url");
            if (dbImg != null && !dbImg.trim().isEmpty()) {
                img = dbImg.trim();
            }
        } catch (SQLException ignored) {}

        return new Product(
                resultSet.getInt("id"),
                resultSet.getInt("seller_id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("category"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("quantity"),
                img
        );
    }
}