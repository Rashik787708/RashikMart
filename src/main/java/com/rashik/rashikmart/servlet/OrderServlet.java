package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.CartDAO;
import com.rashik.rashikmart.dao.OrderDAO;
import com.rashik.rashikmart.model.CartItem;
import com.rashik.rashikmart.model.Order;
import com.rashik.rashikmart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet({"/buyer/checkout", "/buyer/orders", "/buyer/place-order", "/OrderServlet"})
public class OrderServlet extends HttpServlet {

    private OrderDAO orderDAO;
    private CartDAO cartDAO;

    @Override
    public void init() throws ServletException {
        orderDAO = new OrderDAO();
        cartDAO = new CartDAO();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+first");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (role == null || !"BUYER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Buyer+access+required");
            return;
        }

        User user = (User) session.getAttribute("user");
        String uri = request.getRequestURI();

        if (uri.endsWith("/orders")) {
            List<Order> orders = orderDAO.findOrdersByBuyerId(user.getId());
            request.setAttribute("orders", orders);
            request.getRequestDispatcher("/buyer/orders.jsp").forward(request, response);
            return;
        }

        // Default to Checkout view
        List<CartItem> cartItems = cartDAO.getCartItems(user.getId());
        if (cartItems == null || cartItems.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/buyer/cart?error=Your+cart+is+empty");
            return;
        }

        BigDecimal cartTotal = cartDAO.getCartTotal(user.getId());
        request.setAttribute("cartItems", cartItems);
        request.setAttribute("cartTotal", cartTotal);

        request.getRequestDispatcher("/buyer/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+first");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (role == null || !"BUYER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Buyer+access+required");
            return;
        }

        User user = (User) session.getAttribute("user");

        try {
            Order order = orderDAO.createOrderFromCart(user.getId());
            response.sendRedirect(request.getContextPath() + "/buyer/order-success.jsp?orderId=" + order.getId() + "&success=Order+placed+successfully");
        } catch (IllegalStateException e) {
            String safeMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/buyer/cart?error=" + safeMsg);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/buyer/cart?error=Unable+to+place+order.+Please+try+again.");
        }
    }
}
