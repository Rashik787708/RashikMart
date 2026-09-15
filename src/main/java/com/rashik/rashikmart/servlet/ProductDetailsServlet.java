package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.dao.ReviewDAO;
import com.rashik.rashikmart.model.Product;
import com.rashik.rashikmart.model.Review;
import com.rashik.rashikmart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet({"/buyer/product-details", "/ProductDetailsServlet"})
public class ProductDetailsServlet extends HttpServlet {

    private ProductDAO productDAO;
    private ReviewDAO reviewDAO;

    @Override
    public void init() throws ServletException {
        productDAO = new ProductDAO();
        reviewDAO = new ReviewDAO();
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

        if (!product.isActive()) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Product+is+no+longer+available");
            return;
        }

        request.setAttribute("product", product);

        User buyer = (User) session.getAttribute("user");
        List<Review> reviews = reviewDAO.findByProductId(id);
        request.setAttribute("reviews", reviews);
        request.setAttribute("reviewCount", reviewDAO.getReviewCount(id));
        request.setAttribute("avgRating", reviewDAO.getAverageRating(id));
        request.setAttribute("hasPurchased", reviewDAO.hasPurchasedProduct(buyer.getId(), id));
        request.setAttribute("hasReviewed", reviewDAO.hasReviewByBuyer(buyer.getId(), id));

        request.getRequestDispatcher("/buyer/product-details.jsp").forward(request, response);
    }
}
