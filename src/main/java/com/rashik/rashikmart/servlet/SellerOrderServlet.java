package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.OrderDAO;
import com.rashik.rashikmart.model.SellerOrderItem;
import com.rashik.rashikmart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/seller/orders")
public class SellerOrderServlet extends HttpServlet {

    private OrderDAO orderDAO;

    @Override
    public void init() throws ServletException {
        orderDAO = new OrderDAO();
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
        if (role == null || !"SELLER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Seller+access+required");
            return;
        }

        User user = (User) session.getAttribute("user");

        List<SellerOrderItem> sellerOrders = orderDAO.findSellerOrders(user.getId());
        BigDecimal totalRevenue = orderDAO.getSellerRevenue(user.getId());
        int totalOrders = orderDAO.getSellerTotalOrders(user.getId());

        request.setAttribute("sellerOrders", sellerOrders);
        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("totalOrders", totalOrders);

        request.getRequestDispatcher("/seller/orders.jsp").forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        doGet(request, response);
    }
}
