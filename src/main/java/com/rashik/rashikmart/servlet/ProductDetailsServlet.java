package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.model.Product;
import com.rashik.rashikmart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet({"/buyer/product-details", "/ProductDetailsServlet"})
public class ProductDetailsServlet extends HttpServlet {

    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
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

        String idText = request.getParameter("id");
        if (idText == null || idText.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Product+ID+required");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Invalid+Product+ID");
            return;
        }

        Product product = productDAO.findById(id);

        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Product+not+found");
            return;
        }

        request.setAttribute("product", product);
        request.getRequestDispatcher("/buyer/product-details.jsp").forward(request, response);
    }
}
