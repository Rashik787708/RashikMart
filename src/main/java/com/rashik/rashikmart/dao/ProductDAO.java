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

    // =========================================================
    // CREATE PRODUCT
    // =========================================================

    public boolean createProduct(Product product) {
        return addProduct(product);
    }

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

    // =========================================================
    // FIND PRODUCT BY ID
    // =========================================================

    public Product findById(int id) {

        String sql = """
                SELECT id,
                       seller_id,
                       name,
                       description,
                       category,
                       price,
                       quantity,
                       image_url,
                       active,
                       created_at
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

    // =========================================================
    // FIND PRODUCT BY ID + SELLER
    // =========================================================

    public Product findByIdAndSellerId(
            int id,
            int sellerId
    ) {

        String sql = """
                SELECT id,
                       seller_id,
                       name,
                       description,
                       category,
                       price,
                       quantity,
                       image_url,
                       active,
                       created_at
                FROM products
                WHERE id = ?
                  AND seller_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);
            statement.setInt(2, sellerId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapProduct(resultSet);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error finding seller product: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return null;
    }

    // =========================================================
    // FIND SELLER PRODUCTS
    // =========================================================

    public List<Product> findBySellerId(
            int sellerId
    ) {

        List<Product> products =
                new ArrayList<>();

        String sql = """
                SELECT id,
                       seller_id,
                       name,
                       description,
                       category,
                       price,
                       quantity,
                       image_url,
                       active,
                       created_at
                FROM products
                WHERE seller_id = ?
                  AND active = TRUE
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

    // =========================================================
    // FIND ALL PRODUCTS
    // =========================================================

    public List<Product> findAll() {

        List<Product> products =
                new ArrayList<>();

        String sql = """
                SELECT id,
                       seller_id,
                       name,
                       description,
                       category,
                       price,
                       quantity,
                       image_url,
                       active,
                       created_at
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

    // =========================================================
    // FIND AVAILABLE PRODUCTS
    // =========================================================

    public List<Product> findAllAvailable() {

        List<Product> products =
                new ArrayList<>();

        String sql = """
                SELECT id,
                       seller_id,
                       name,
                       description,
                       category,
                       price,
                       quantity,
                       image_url,
                       active,
                       created_at
                FROM products
                WHERE quantity > 0
                  AND active = TRUE
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
                    "Error finding available products: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return products;
    }

    // =========================================================
    // UPDATE PRODUCT
    // =========================================================

    public boolean updateProduct(
            Product product
    ) {

        String sql = """
                UPDATE products
                SET name = ?,
                    description = ?,
                    category = ?,
                    price = ?,
                    quantity = ?,
                    image_url = ?
                WHERE id = ?
                  AND seller_id = ?
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

    // =========================================================
    // DELETE PRODUCT (SAFE)
    // =========================================================

    public enum DeletionResult {
        DELETED,
        DEACTIVATED,
        NOT_FOUND,
        ERROR
    }

    public DeletionResult deleteProduct(
            int id,
            int sellerId
    ) {

        String selectOwnershipSql = """
                SELECT id
                FROM products
                WHERE id = ?
                  AND seller_id = ?
                FOR UPDATE
                """;

        String countOrderReferencesSql = """
                SELECT COUNT(*) AS reference_count
                FROM order_items
                WHERE product_id = ?
                """;

        String deactivateSql = """
                UPDATE products
                SET active = FALSE
                WHERE id = ?
                  AND seller_id = ?
                """;

        String deleteSql = """
                DELETE FROM products
                WHERE id = ?
                  AND seller_id = ?
                """;

        Connection connection = null;

        try {

            connection =
                    DatabaseConfig.getDataSource().getConnection();

            connection.setAutoCommit(false);

            // Step 1: verify the product exists and belongs to the seller.
            boolean owned;

            try (PreparedStatement ownershipStatement =
                         connection.prepareStatement(selectOwnershipSql)) {

                ownershipStatement.setInt(1, id);
                ownershipStatement.setInt(2, sellerId);

                try (ResultSet resultSet =
                             ownershipStatement.executeQuery()) {

                    owned = resultSet.next();
                }
            }

            if (!owned) {
                connection.rollback();
                return DeletionResult.NOT_FOUND;
            }

            // Step 2: check whether historical order records reference it.
            boolean referencedByOrders;

            try (PreparedStatement referenceStatement =
                         connection.prepareStatement(countOrderReferencesSql)) {

                referenceStatement.setInt(1, id);

                try (ResultSet resultSet =
                             referenceStatement.executeQuery()) {

                    resultSet.next();

                    referencedByOrders =
                            resultSet.getInt("reference_count") > 0;
                }
            }

            // Step 3: remove the active listing, preserving order history.
            if (referencedByOrders) {

                try (PreparedStatement deactivateStatement =
                             connection.prepareStatement(deactivateSql)) {

                    deactivateStatement.setInt(1, id);
                    deactivateStatement.setInt(2, sellerId);

                    int updated =
                            deactivateStatement.executeUpdate();

                    connection.commit();

                    return updated > 0
                            ? DeletionResult.DEACTIVATED
                            : DeletionResult.NOT_FOUND;
                }
            }

            // No order references: physical delete is safe.
            try (PreparedStatement deleteStatement =
                         connection.prepareStatement(deleteSql)) {

                deleteStatement.setInt(1, id);
                deleteStatement.setInt(2, sellerId);

                int updated =
                        deleteStatement.executeUpdate();

                connection.commit();

                return updated > 0
                        ? DeletionResult.DELETED
                        : DeletionResult.NOT_FOUND;
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error deleting product: "
                            + e.getMessage()
            );

            e.printStackTrace();

            if (connection != null) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            return DeletionResult.ERROR;

        } finally {

            if (connection != null) {

                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // =========================================================
    // UPDATE STOCK
    // =========================================================

    public boolean updateStock(
            int productId,
            int newQuantity
    ) {

        String sql = """
                UPDATE products
                SET quantity = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, newQuantity);
            statement.setInt(2, productId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error updating product stock: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // MAP PRODUCT
    // =========================================================

    private Product mapProduct(
            ResultSet resultSet
    ) throws SQLException {

        String imageUrl =
                "default-product.svg";

        try {

            String databaseImage =
                    resultSet.getString("image_url");

            if (databaseImage != null
                    && !databaseImage.trim().isEmpty()) {

                imageUrl =
                        databaseImage.trim();
            }

        } catch (SQLException ignored) {
        }

        java.sql.Timestamp createdAt = null;

        try {

            createdAt =
                    resultSet.getTimestamp("created_at");

        } catch (SQLException ignored) {
        }

        boolean active = true;

        try {

            active =
                    resultSet.getBoolean("active");

        } catch (SQLException ignored) {
        }

        Product product = new Product(
                resultSet.getInt("id"),
                resultSet.getInt("seller_id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("category"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("quantity"),
                imageUrl,
                createdAt
        );

        product.setActive(active);

        return product;
    }

    // =========================================================
    // TOGGLE PRODUCT ACTIVE STATUS (FOR ADMIN)
    // =========================================================

    public boolean toggleProductActive(int productId, boolean active) {
        String sql = """
                UPDATE products
                SET active = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setBoolean(1, active);
            statement.setInt(2, productId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error toggling product active status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}