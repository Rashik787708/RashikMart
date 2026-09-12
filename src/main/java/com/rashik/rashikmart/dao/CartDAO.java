package com.rashik.rashikmart.dao;

import com.rashik.rashikmart.config.DatabaseConfig;
import com.rashik.rashikmart.model.CartItem;
import com.rashik.rashikmart.model.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    private final ProductDAO productDAO = new ProductDAO();

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private int getCartId(
            Connection connection,
            int buyerId
    ) throws SQLException {

        String sql = """
                SELECT id
                FROM cart
                WHERE buyer_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, buyerId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }

        return -1;
    }

    private int getOrCreateCartId(
            Connection connection,
            int buyerId
    ) throws SQLException {

        int cartId =
                getCartId(connection, buyerId);

        if (cartId != -1) {
            return cartId;
        }

        String sql = """
                INSERT INTO cart (buyer_id)
                VALUES (?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setInt(1, buyerId);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        // A concurrent insert may have won the UNIQUE race;
        // fall back to a lookup instead of failing.
        return getCartId(connection, buyerId);
    }

    // =========================================================
    // ADD ITEM TO CART
    // =========================================================

    public boolean addItem(
            int buyerId,
            int productId,
            int quantity
    ) {

        if (buyerId <= 0 || productId <= 0 || quantity <= 0) {
            return false;
        }

        String checkSql = """
                SELECT quantity
                FROM cart_items
                WHERE cart_id = ?
                  AND product_id = ?
                """;

        String insertSql = """
                INSERT INTO cart_items
                (cart_id, product_id, quantity)
                VALUES (?, ?, ?)
                """;

        String updateSql = """
                UPDATE cart_items
                SET quantity = ?
                WHERE cart_id = ?
                  AND product_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection()
        ) {

            int cartId =
                    getOrCreateCartId(connection, buyerId);

            if (cartId == -1) {
                return false;
            }

            Product product =
                    productDAO.findById(productId);

            if (product == null) {
                return false;
            }

            try (PreparedStatement checkStatement =
                         connection.prepareStatement(checkSql)) {

                checkStatement.setInt(1, cartId);
                checkStatement.setInt(2, productId);

                try (ResultSet resultSet =
                             checkStatement.executeQuery()) {

                    if (resultSet.next()) {

                        int existingQuantity =
                                resultSet.getInt("quantity");

                        int newQuantity =
                                existingQuantity + quantity;

                        if (newQuantity > product.getQuantity()) {
                            return false;
                        }

                        try (PreparedStatement updateStatement =
                                     connection.prepareStatement(updateSql)) {

                            updateStatement.setInt(1, newQuantity);
                            updateStatement.setInt(2, cartId);
                            updateStatement.setInt(3, productId);

                            return updateStatement.executeUpdate() > 0;
                        }

                    } else {

                        if (quantity > product.getQuantity()) {
                            return false;
                        }

                        try (PreparedStatement insertStatement =
                                     connection.prepareStatement(insertSql)) {

                            insertStatement.setInt(1, cartId);
                            insertStatement.setInt(2, productId);
                            insertStatement.setInt(3, quantity);

                            return insertStatement.executeUpdate() > 0;
                        }
                    }
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error adding item to cart: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // GET CART ITEMS
    // =========================================================

    public List<CartItem> getCartItems(int buyerId) {

        List<CartItem> cartItems =
                new ArrayList<>();

        String sql = """
                SELECT c.buyer_id,
                       ci.product_id,
                       ci.quantity
                FROM cart_items ci
                JOIN cart c
                  ON c.id = ci.cart_id
                WHERE c.buyer_id = ?
                ORDER BY ci.product_id DESC
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, buyerId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    int productId =
                            resultSet.getInt("product_id");

                    Product product =
                            productDAO.findById(productId);

                    CartItem item =
                            new CartItem();

                    item.setBuyerId(
                            resultSet.getInt("buyer_id")
                    );

                    item.setProductId(productId);

                    item.setQuantity(
                            resultSet.getInt("quantity")
                    );

                    item.setProduct(product);

                    cartItems.add(item);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error loading cart: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return cartItems;
    }

    // =========================================================
    // UPDATE QUANTITY
    // =========================================================

    public boolean updateQuantity(
            int buyerId,
            int productId,
            int quantity
    ) {

        if (quantity <= 0) {
            return removeItem(
                    buyerId,
                    productId
            );
        }

        Product product =
                productDAO.findById(productId);

        if (product == null ||
                quantity > product.getQuantity()) {
            return false;
        }

        String sql = """
                UPDATE cart_items
                SET quantity = ?
                WHERE cart_id = ?
                  AND product_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection()
        ) {

            int cartId =
                    getCartId(connection, buyerId);

            if (cartId == -1) {
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, quantity);
                statement.setInt(2, cartId);
                statement.setInt(3, productId);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error updating cart quantity: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // REMOVE ITEM
    // =========================================================

    public boolean removeItem(
            int buyerId,
            int productId
    ) {

        String sql = """
                DELETE FROM cart_items
                WHERE cart_id = ?
                  AND product_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection()
        ) {

            int cartId =
                    getCartId(connection, buyerId);

            if (cartId == -1) {
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, cartId);
                statement.setInt(2, productId);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error removing cart item: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // CLEAR CART
    // =========================================================

    public boolean clearCart(int buyerId) {

        String sql = """
                DELETE FROM cart_items
                WHERE cart_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection()
        ) {

            int cartId =
                    getCartId(connection, buyerId);

            if (cartId == -1) {
                return true;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, cartId);

                statement.executeUpdate();

                return true;
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error clearing cart: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // CART TOTAL
    // =========================================================

    public BigDecimal getCartTotal(int buyerId) {

        String sql = """
                SELECT ci.quantity,
                       p.price
                FROM cart_items ci
                JOIN cart c
                  ON c.id = ci.cart_id
                JOIN products p
                  ON p.id = ci.product_id
                WHERE c.buyer_id = ?
                """;

        BigDecimal total =
                BigDecimal.ZERO;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, buyerId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    BigDecimal price =
                            resultSet.getBigDecimal("price");

                    int quantity =
                            resultSet.getInt("quantity");

                    if (price != null) {

                        BigDecimal subtotal =
                                price.multiply(
                                        BigDecimal.valueOf(quantity)
                                );

                        total =
                                total.add(subtotal);
                    }
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error calculating cart total: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return total;
    }
}