package com.rashik.rashikmart.servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rashik.rashikmart.dao.OrderDAO;
import com.rashik.rashikmart.dto.ApiResponse;
import com.rashik.rashikmart.model.Order;
import com.rashik.rashikmart.model.User;
import com.rashik.rashikmart.util.CsrfUtil;
import com.rashik.rashikmart.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Order status management endpoint per spec O2.
 * Allows sellers (for their order items) and admins to advance orders:
 * CONFIRMED -> SHIPPED -> DELIVERED.
 * Rejects invalid transitions with HTTP 409 Conflict.
 */
@WebServlet(urlPatterns = {"/seller/order-status", "/admin/order-status", "/api/v1/orders/status"})
public class OrderStatusServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusServlet.class);

    private OrderDAO orderDAO;

    public OrderStatusServlet() {
        this.orderDAO = new OrderDAO();
    }

    public OrderStatusServlet(OrderDAO orderDAO) {
        this.orderDAO = orderDAO != null ? orderDAO : new OrderDAO();
    }

    @Override
    public void init() throws ServletException {
        if (this.orderDAO == null) {
            this.orderDAO = new OrderDAO();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        boolean isJsonRequest = isJsonRequest(request);

        // 1. Authentication check
        if (session == null || session.getAttribute("user") == null) {
            if (isJsonRequest) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.error("UNAUTHORIZED", "Authentication required"));
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+first");
            }
            return;
        }

        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        if (role == null) {
            role = user.getRole();
        }

        // 2. Role check (SELLER or ADMIN only)
        if (!"SELLER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            if (isJsonRequest) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                        ApiResponse.error("FORBIDDEN", "Only sellers or administrators can change order status"));
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only sellers or administrators can update order status.");
            }
            return;
        }

        // 3. CSRF Validation
        if (!CsrfUtil.isValid(request)) {
            if (isJsonRequest) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                        ApiResponse.error("FORBIDDEN", "Invalid or missing CSRF token"));
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
            }
            return;
        }

        // 4. Extract parameters (orderId & newStatus)
        String orderIdStr = request.getParameter("orderId");
        String newStatus = request.getParameter("status");

        if (orderIdStr == null || orderIdStr.isBlank() || newStatus == null || newStatus.isBlank()) {
            // Try parsing JSON payload if available
            try {
                String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                if (!body.isBlank()) {
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    if (json.has("orderId")) {
                        orderIdStr = json.get("orderId").getAsString();
                    }
                    if (json.has("status")) {
                        newStatus = json.get("status").getAsString();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        int orderId;
        try {
            orderId = Integer.parseInt(orderIdStr != null ? orderIdStr.trim() : "");
        } catch (NumberFormatException e) {
            if (isJsonRequest) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.error("BAD_REQUEST", "Valid numeric orderId is required"));
            } else {
                response.sendRedirect(request.getContextPath() + "/seller/orders?error=Invalid+order+ID");
            }
            return;
        }

        newStatus = newStatus != null ? newStatus.trim().toUpperCase() : "";

        // 5. Order lookup
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            if (isJsonRequest) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        ApiResponse.error("NOT_FOUND", "Order not found"));
            } else {
                response.sendRedirect(request.getContextPath() + "/seller/orders?error=Order+not+found");
            }
            return;
        }

        // 6. Seller authorization (verify seller sells an item in this order)
        if ("SELLER".equalsIgnoreCase(role)) {
            boolean isAuthorizedSeller = orderDAO.isSellerForOrder(orderId, user.getId());
            if (!isAuthorizedSeller) {
                if (isJsonRequest) {
                    JsonUtil.writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                            ApiResponse.error("FORBIDDEN", "You are not authorized to update orders for other sellers"));
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for this order.");
                }
                return;
            }
        }

        // 7. Transition Validation (Spec O2)
        // Allowed transitions: PENDING -> CONFIRMED, CONFIRMED -> SHIPPED, SHIPPED -> DELIVERED
        String currentStatus = order.getStatus() != null ? order.getStatus().toUpperCase() : "CONFIRMED";

        boolean validTransition = isValidTransition(currentStatus, newStatus);
        if (!validTransition) {
            String conflictMsg = "Invalid order status transition from " + currentStatus + " to " + newStatus;
            logger.warn("Order {} transition rejected: {} -> {}", orderId, currentStatus, newStatus);
            if (isJsonRequest) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_CONFLICT,
                        ApiResponse.error("CONFLICT", conflictMsg));
            } else {
                response.sendError(HttpServletResponse.SC_CONFLICT, conflictMsg);
            }
            return;
        }

        // 8. Apply status update
        boolean updated = orderDAO.updateOrderStatus(orderId, newStatus);
        if (!updated) {
            if (isJsonRequest) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ApiResponse.error("INTERNAL_ERROR", "Failed to update order status in database"));
            } else {
                response.sendRedirect(request.getContextPath() + "/seller/orders?error=Unable+to+update+status");
            }
            return;
        }

        logger.info("Order {} transitioned from {} to {} by user {}", orderId, currentStatus, newStatus, user.getEmail());

        if (isJsonRequest) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("orderId", orderId);
            data.put("previousStatus", currentStatus);
            data.put("status", newStatus);
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK, ApiResponse.success(data));
        } else {
            String redirectUrl = request.getContextPath() + "/seller/orders?success="
                    + URLEncoder.encode("Order #" + orderId + " marked as " + newStatus, StandardCharsets.UTF_8);
            response.sendRedirect(redirectUrl);
        }
    }

    public static boolean isValidTransition(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) return false;
        String from = fromStatus.trim().toUpperCase();
        String to = toStatus.trim().toUpperCase();

        if (from.equals(to)) return false;

        return switch (from) {
            case "PENDING" -> "CONFIRMED".equals(to);
            case "CONFIRMED" -> "SHIPPED".equals(to);
            case "SHIPPED" -> "DELIVERED".equals(to);
            default -> false;
        };
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path != null && path.contains("/api/")) return true;
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) return true;
        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/json");
    }
}
