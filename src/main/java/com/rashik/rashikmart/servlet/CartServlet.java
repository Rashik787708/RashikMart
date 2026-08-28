package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.CartDAO;
import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.model.CartItem;
import com.rashik.rashikmart.model.Product;
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

@WebServlet({"/buyer/cart", "/CartServlet", "/buyer/cart-action"})
public class CartServlet extends HttpServlet {

    private CartDAO cartDAO;
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        cartDAO = new CartDAO();
        productDAO = new ProductDAO();
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

        List<CartItem> cartItems = cartDAO.getCartItems(user.getId());
        BigDecimal total = cartDAO.getCartTotal(user.getId());

        request.setAttribute("cartItems", cartItems);
        request.setAttribute("cartTotal", total);

        request.getRequestDispatcher("/buyer/cart.jsp").forward(request, response);
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
        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            action = "add";
        }

        if ("add".equalsIgnoreCase(action)) {
            handleAdd(request, response, user.getId());
        } else if ("update".equalsIgnoreCase(action)) {
            handleUpdate(request, response, user.getId());
        } else if ("remove".equalsIgnoreCase(action)) {
            handleRemove(request, response, user.getId());
        } else if ("clear".equalsIgnoreCase(action)) {
            handleClear(request, response, user.getId());
        } else {
            response.sendRedirect(request.getContextPath() + "/buyer/cart?error=Invalid+cart+action");
        }
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response, int buyerId)
            throws IOException {

        String productIdText = request.getParameter("productId");
        String quantityText = request.getParameter("quantity");
        String redirectTarget = request.getParameter("redirect");

        if (productIdText == null || productIdText.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Product+ID+missing");
            return;
        }

        int productId;
        int quantity = 1;

        try {
            productId = Integer.parseInt(productIdText.trim());
            if (quantityText != null && !quantityText.trim().isEmpty()) {
                quantity = Integer.parseInt(quantityText.trim());
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Invalid+quantity+or+product+ID");
            return;
        }

        if (quantity <= 0) {
            response.sendRedirect(request.getContextPath() + "/buyer/product-details?id=" + productId + "&error=Quantity+must+be+at+least+1");
            return;
        }

        Product product = productDAO.findById(productId);
        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Product+not+found");
            return;
        }

        if (quantity > product.getQuantity()) {
            response.sendRedirect(request.getContextPath() + "/buyer/product-details?id=" + productId + "&error=Only+" + product.getQuantity() + "+units+available+in+stock");
            return;
        }

        boolean success = cartDAO.addItem(buyerId, productId, quantity);

        String destination = (redirectTarget != null && "marketplace".equalsIgnoreCase(redirectTarget))
                ? "/buyer/marketplace?success=Item+added+to+cart"
                : "/buyer/cart?success=Item+added+to+cart";

        if (success) {
            response.sendRedirect(request.getContextPath() + destination);
        } else {
            response.sendRedirect(request.getContextPath() + "/buyer/product-details?id=" + productId + "&error=Unable+to+add+item+to+cart");
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response, int buyerId)
            throws IOException {

        String productIdText = request.getParameter("productId");
        String quantityText = request.getParameter("quantity");

        if (productIdText == null || quantityText == null) {
            response.sendRedirect(request.getContextPath() + "/buyer/cart?error=Missing+parameters");
            return;
        }

        int productId;
        int quantity;

        try {
            productId = Integer.parseInt(productIdText.trim());
            quantity = Integer.parseInt(quantityText.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/buyer/cart?error=Invalid+quantity");
            return;
        }

        if (quantity <= 0) {
            cartDAO.removeItem(buyerId, productId);
            response.sendRedirect(request.getContextPath() + "/buyer/cart?success=Item+removed+from+cart");
            return;
        }

        Product product = productDAO.findById(productId);
        if (product == null) {
            cartDAO.removeItem(buyerId, productId);
            response.sendRedirect(request.getContextPath() + "/buyer/cart?error=Product+is+no+longer+available");
            return;
        }

        if (quantity > product.getQuantity()) {
            response.sendRedirect(request.getContextPath() + "/buyer/cart?error=Only+" + product.getQuantity() + "+units+available+in+stock+for+" + product.getName());
            return;
        }

        cartDAO.updateQuantity(buyerId, productId, quantity);
        response.sendRedirect(request.getContextPath() + "/buyer/cart?success=Cart+quantity+updated");
    }

    private void handleRemove(HttpServletRequest request, HttpServletResponse response, int buyerId)
            throws IOException {

        String productIdText = request.getParameter("productId");
        if (productIdText != null && !productIdText.trim().isEmpty()) {
            try {
                int productId = Integer.parseInt(productIdText.trim());
                cartDAO.removeItem(buyerId, productId);
            } catch (NumberFormatException ignored) {}
        }
        response.sendRedirect(request.getContextPath() + "/buyer/cart?success=Item+removed");
    }

    private void handleClear(HttpServletRequest request, HttpServletResponse response, int buyerId)
            throws IOException {
        cartDAO.clearCart(buyerId);
        response.sendRedirect(request.getContextPath() + "/buyer/cart?success=Cart+cleared");
    }
}
