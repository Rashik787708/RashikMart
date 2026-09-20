package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.dao.ReviewDAO;
import com.rashik.rashikmart.model.Product;
import com.rashik.rashikmart.model.User;
import com.rashik.rashikmart.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/buyer/review")
public class ReviewServlet extends HttpServlet {

    private static final int MAX_REVIEW_TEXT_LENGTH = 500;

    private ReviewDAO reviewDAO;
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        reviewDAO = new ReviewDAO();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/buyer/marketplace"
        );
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

        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
            return;
        }

        User user = (User) session.getAttribute("user");

        String productIdText = request.getParameter("productId");

        if (productIdText == null || productIdText.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Product+ID+required");
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdText.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Invalid+Product+ID");
            return;
        }

        Product product = productDAO.findById(productId);

        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Product+not+found");
            return;
        }

        if (!product.isActive()) {
            response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Product+is+no+longer+available");
            return;
        }

        String redirectBase =
                request.getContextPath()
                        + "/buyer/product-details?id="
                        + productId;

        String ratingText = request.getParameter("rating");
        String reviewText = request.getParameter("reviewText");

        int rating;

        try {

            rating = Integer.parseInt(
                    ratingText == null ? "" : ratingText.trim()
            );

        } catch (NumberFormatException e) {

            response.sendRedirect(redirectBase + "&error=Rating+must+be+a+whole+number+from+1+to+5");
            return;
        }

        if (rating < 1 || rating > 5) {

            response.sendRedirect(redirectBase + "&error=Rating+must+be+between+1+and+5");
            return;
        }

        String cleanText = (reviewText == null)
                ? ""
                : reviewText.trim();

        if (cleanText.length() > MAX_REVIEW_TEXT_LENGTH) {

            response.sendRedirect(redirectBase + "&error=Review+text+must+be+under+500+characters");
            return;
        }

        try {

            reviewDAO.addReview(
                    user.getId(),
                    productId,
                    rating,
                    cleanText
            );

            response.sendRedirect(
                    redirectBase
                            + "&success=Review+submitted+successfully"
            );

        } catch (IllegalStateException e) {

            String safeMessage =
                    URLEncoder.encode(
                            e.getMessage(),
                            StandardCharsets.UTF_8
                    );

            response.sendRedirect(
                    redirectBase
                            + "&error="
                            + safeMessage
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                    redirectBase
                            + "&error=Unable+to+submit+review.+Please+try+again."
            );
        }
    }
}