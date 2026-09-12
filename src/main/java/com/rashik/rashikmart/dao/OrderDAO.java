package com.rashik.rashikmart.dao;

import com.rashik.rashikmart.config.DatabaseConfig;
import com.rashik.rashikmart.model.Order;
import com.rashik.rashikmart.model.OrderItem;
import com.rashik.rashikmart.model.Product;
import com.rashik.rashikmart.model.SellerOrderItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // =====================================================
    // 1. CREATE ORDER FROM CART (TRANSACTIONAL)
    // =====================================================

    public Order createOrderFromCart(int buyerId) throws Exception {

        String selectCartSql = """
                SELECT c.id AS cart_id,
                       ci.product_id,
                       ci.quantity AS cart_qty
                FROM cart c
                JOIN cart_items ci ON c.id = ci.cart_id
                WHERE c.buyer_id = ?
                """;

        String selectProductSql = """
                SELECT id, name, price, quantity, image_url, description, category, seller_id
                FROM products
                WHERE id = ?
                """;

        String insertOrderSql = """
                INSERT INTO orders (buyer_id, total_amount, status)
                VALUES (?, ?, 'CONFIRMED')
                """;

        String insertOrderItemSql = """
                INSERT INTO order_items (order_id, product_id, quantity, price)
                VALUES (?, ?, ?, ?)
                """;

        String updateStockSql = """
                UPDATE products
                SET quantity = quantity - ?
                WHERE id = ?
                """;

        String clearCartSql = """
                DELETE FROM cart_items
                WHERE cart_id = ?
                """;

        Connection connection = null;

        try {
            connection = DatabaseConfig.getDataSource().getConnection();
            connection.setAutoCommit(false); // Begin transaction

            // 1. Fetch Cart Items
            int cartId = -1;
            List<int[]> cartEntries = new ArrayList<>(); // [productId, requestedQuantity]

            try (PreparedStatement cartStmt = connection.prepareStatement(selectCartSql)) {
                cartStmt.setInt(1, buyerId);
                try (ResultSet rs = cartStmt.executeQuery()) {
                    while (rs.next()) {
                        cartId = rs.getInt("cart_id");
                        cartEntries.add(new int[]{rs.getInt("product_id"), rs.getInt("cart_qty")});
                    }
                }
            }

            if (cartEntries.isEmpty()) {
                throw new IllegalStateException("Your cart is empty.");
            }

            // 2. Validate live stock and calculate server total
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItemsToCreate = new ArrayList<>();

            for (int[] entry : cartEntries) {
                int productId = entry[0];
                int requestedQty = entry[1];

                try (PreparedStatement prodStmt = connection.prepareStatement(selectProductSql)) {
                    prodStmt.setInt(1, productId);
                    try (ResultSet prodRs = prodStmt.executeQuery()) {
                        if (!prodRs.next()) {
                            throw new IllegalStateException("Product (ID: " + productId + ") is no longer available.");
                        }

                        String prodName = prodRs.getString("name");
                        BigDecimal currentPrice = prodRs.getBigDecimal("price");
                        int availableStock = prodRs.getInt("quantity");
                        String imgUrl = prodRs.getString("image_url");

                        if (requestedQty > availableStock) {
                            throw new IllegalStateException("Product '" + prodName + "' only has " + availableStock + " in stock (requested: " + requestedQty + ").");
                        }

                        if (requestedQty <= 0) {
                            throw new IllegalStateException("Invalid quantity for product '" + prodName + "'.");
                        }

                        BigDecimal subtotal = currentPrice.multiply(BigDecimal.valueOf(requestedQty));
                        totalAmount = totalAmount.add(subtotal);

                        Product productSnapshot = new Product(
                                productId,
                                prodRs.getInt("seller_id"),
                                prodName,
                                prodRs.getString("description"),
                                prodRs.getString("category"),
                                currentPrice,
                                availableStock,
                                imgUrl
                        );

                        OrderItem item = new OrderItem(0, 0, productId, requestedQty, currentPrice, productSnapshot);
                        orderItemsToCreate.add(item);
                    }
                }
            }

            // 3. Insert Order
            int generatedOrderId;
            try (PreparedStatement orderStmt = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, buyerId);
                orderStmt.setBigDecimal(2, totalAmount);
                orderStmt.executeUpdate();

                try (ResultSet keys = orderStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedOrderId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated order ID.");
                    }
                }
            }

            // 4. Insert Order Items & Deduct Stock
            for (OrderItem item : orderItemsToCreate) {
                item.setOrderId(generatedOrderId);

                // Insert item with purchase-time price
                try (PreparedStatement itemStmt = connection.prepareStatement(insertOrderItemSql)) {
                    itemStmt.setInt(1, generatedOrderId);
                    itemStmt.setInt(2, item.getProductId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getPrice());
                    itemStmt.executeUpdate();
                }

                // Deduct stock
                try (PreparedStatement stockStmt = connection.prepareStatement(updateStockSql)) {
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getProductId());
                    int updated = stockStmt.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Failed to update stock for product ID: " + item.getProductId());
                    }
                }
            }

            // 5. Clear Cart
            if (cartId != -1) {
                try (PreparedStatement clearStmt = connection.prepareStatement(clearCartSql)) {
                    clearStmt.setInt(1, cartId);
                    clearStmt.executeUpdate();
                }
            }

            // 6. Commit Transaction
            connection.commit();

            // Construct and return created Order
            Order order = new Order(generatedOrderId, buyerId, totalAmount, "CONFIRMED", new Timestamp(System.currentTimeMillis()));
            order.setItems(orderItemsToCreate);
            return order;

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // =====================================================
    // 2. FIND ORDERS BY BUYER ID
    // =====================================================

    public List<Order> findOrdersByBuyerId(int buyerId) {

        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT id, buyer_id, total_amount, status, created_at
                FROM orders
                WHERE buyer_id = ?
                ORDER BY id DESC
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, buyerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                            rs.getInt("id"),
                            rs.getInt("buyer_id"),
                            rs.getBigDecimal("total_amount"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at")
                    );
                    order.setItems(findOrderItems(order.getId()));
                    orders.add(order);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding buyer orders: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    // =====================================================
    // 3. FIND ORDER BY ID (AND VERIFY BUYER)
    // =====================================================

    public Order findOrderById(int orderId, int buyerId) {

        String sql = """
                SELECT id, buyer_id, total_amount, status, created_at
                FROM orders
                WHERE id = ? AND buyer_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, orderId);
            statement.setInt(2, buyerId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order(
                            rs.getInt("id"),
                            rs.getInt("buyer_id"),
                            rs.getBigDecimal("total_amount"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at")
                    );
                    order.setItems(findOrderItems(order.getId()));
                    return order;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding order by id: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // =====================================================
    // 4. FIND ALL ORDERS (FOR ADMIN)
    // =====================================================

    public List<Order> findAllOrders() {

        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT id, buyer_id, total_amount, status, created_at
                FROM orders
                ORDER BY id DESC
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet rs = statement.executeQuery()
        ) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("buyer_id"),
                        rs.getBigDecimal("total_amount"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                );
                order.setItems(findOrderItems(order.getId()));
                orders.add(order);
            }

        } catch (SQLException e) {
            System.err.println("Error finding all orders: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    // =====================================================
    // HELPER: FIND ORDER ITEMS WITH PRODUCT SNAPSHOT
    // =====================================================

    public List<OrderItem> findOrderItems(int orderId) {

        List<OrderItem> items = new ArrayList<>();

        String sql = """
                SELECT oi.id AS item_id,
                       oi.order_id,
                       oi.product_id,
                       oi.quantity,
                       oi.price AS item_price,
                       p.name AS p_name,
                       p.category AS p_cat,
                       p.image_url AS p_img
                FROM order_items oi
                LEFT JOIN products p ON oi.product_id = p.id
                WHERE oi.order_id = ?
                ORDER BY oi.id ASC
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, orderId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Product p = null;
                    if (rs.getString("p_name") != null) {
                        p = new Product();
                        p.setId(rs.getInt("product_id"));
                        p.setName(rs.getString("p_name"));
                        p.setCategory(rs.getString("p_cat"));
                        p.setImageUrl(rs.getString("p_img"));
                    }

                    OrderItem item = new OrderItem(
                            rs.getInt("item_id"),
                            rs.getInt("order_id"),
                            rs.getInt("product_id"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("item_price"),
                            p
                    );
                    items.add(item);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding order items: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    // =====================================================
    // 5. FIND SELLER ORDERS (STRICT SELLER ISOLATION)
    // =====================================================

    public List<SellerOrderItem> findSellerOrders(int sellerId) {
        List<SellerOrderItem> items = new ArrayList<>();

        String sql = """
                SELECT o.id AS order_id,
                       o.created_at AS order_date,
                       o.status AS order_status,
                       u.name AS buyer_name,
                       u.email AS buyer_email,
                       oi.product_id,
                       oi.quantity,
                       oi.price AS unit_price,
                       p.name AS product_name,
                       p.category AS product_category,
                       p.image_url AS product_image
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                JOIN orders o ON oi.order_id = o.id
                JOIN users u ON o.buyer_id = u.id
                WHERE p.seller_id = ?
                ORDER BY o.id DESC, oi.id ASC
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, sellerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    SellerOrderItem item = new SellerOrderItem(
                            rs.getInt("order_id"),
                            rs.getTimestamp("order_date"),
                            rs.getString("order_status"),
                            rs.getString("buyer_name"),
                            rs.getString("buyer_email"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("product_category"),
                            rs.getString("product_image"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price")
                    );
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding seller orders: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    public BigDecimal getSellerRevenue(int sellerId) {
        String sql = """
                SELECT SUM(oi.quantity * oi.price) AS total_revenue
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                WHERE p.seller_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, sellerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    BigDecimal rev = rs.getBigDecimal("total_revenue");
                    return rev != null ? rev : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating seller revenue: " + e.getMessage());
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public int getSellerTotalOrders(int sellerId) {
        String sql = """
                SELECT COUNT(DISTINCT oi.order_id) AS total_orders
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                WHERE p.seller_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, sellerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_orders");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting seller orders: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // =====================================================
    // 6. PLATFORM STATISTICS (FOR ADMIN)
    // =====================================================

    public BigDecimal getPlatformTotalRevenue() {
        String sql = "SELECT SUM(total_amount) AS platform_revenue FROM orders";
        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            if (rs.next()) {
                BigDecimal rev = rs.getBigDecimal("platform_revenue");
                return rev != null ? rev : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating platform revenue: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public int getPlatformTotalOrders() {
        String sql = "SELECT COUNT(*) AS total_count FROM orders";
        try (
                Connection connection =
                        DatabaseConfig.getDataSource().getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("total_count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting platform orders: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}
