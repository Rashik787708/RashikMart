package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.model.Product;
import com.rashik.rashikmart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.UUID;

@WebServlet("/seller/add-product")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,       // 1MB
        maxFileSize = 1024 * 1024 * 5,          // 5MB
        maxRequestSize = 1024 * 1024 * 10       // 10MB
)
public class AddProductServlet extends HttpServlet {

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

        response.sendRedirect(
                request.getContextPath()
                        + "/seller/add-product.jsp"
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // =========================
        // SESSION CHECK
        // =========================

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Please+login+first"
            );

            return;
        }

        String role =
                (String) session.getAttribute("role");

        if (role == null
                || !"SELLER".equalsIgnoreCase(role)) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?error=Seller+access+required"
            );

            return;
        }

        User user =
                (User) session.getAttribute("user");

        // =========================
        // FORM VALUES
        // =========================

        String name =
                request.getParameter("name");

        String description =
                request.getParameter("description");

        String category =
                request.getParameter("category");

        String priceText =
                request.getParameter("price");

        String quantityText =
                request.getParameter("quantity");

        // =========================
        // BASIC VALIDATION
        // =========================

        if (name == null
                || name.trim().isEmpty()
                || category == null
                || category.trim().isEmpty()
                || priceText == null
                || priceText.trim().isEmpty()
                || quantityText == null
                || quantityText.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/seller/add-product.jsp?error=Please+fill+all+required+fields"
            );

            return;
        }

        BigDecimal price;
        int quantity;

        // =========================
        // PRICE VALIDATION
        // =========================

        try {

            price = new BigDecimal(
                    priceText.trim()
            );

            if (price.compareTo(BigDecimal.ZERO) <= 0) {

                response.sendRedirect(
                    request.getContextPath()
                            + "/seller/add-product.jsp?error=Price+must+be+greater+than+zero"
                );

                return;
            }

        } catch (NumberFormatException e) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/seller/add-product.jsp?error=Invalid+price"
            );

            return;
        }

        // =========================
        // QUANTITY VALIDATION
        // =========================

        try {

            quantity =
                    Integer.parseInt(
                            quantityText.trim()
                    );

            if (quantity <= 0) {

                response.sendRedirect(
                        request.getContextPath()
                                + "/seller/add-product.jsp?error=Quantity+must+be+greater+than+zero"
                );

                return;
            }

        } catch (NumberFormatException e) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/seller/add-product.jsp?error=Invalid+quantity"
            );

            return;
        }

        // =========================
        // IMAGE UPLOAD HANDLING
        // =========================

        String imageUrl = "default-product.svg";

        try {
            Part filePart = request.getPart("image");
            if (filePart != null && filePart.getSize() > 0) {
                String submittedName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                if (submittedName != null && !submittedName.trim().isEmpty()) {
                    String extension = "";
                    int dotIndex = submittedName.lastIndexOf('.');
                    if (dotIndex >= 0) {
                        extension = submittedName.substring(dotIndex).toLowerCase();
                    }

                    if (extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png") || extension.equals(".webp") || extension.equals(".svg")) {
                        String uniqueFileName = UUID.randomUUID().toString() + extension;
                        String uploadPath = getServletContext().getRealPath("/images/products");

                        if (uploadPath != null) {
                            File uploadDir = new File(uploadPath);
                            if (!uploadDir.exists()) {
                                uploadDir.mkdirs();
                            }

                            filePart.write(uploadPath + File.separator + uniqueFileName);
                            imageUrl = uniqueFileName;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing product image upload: " + e.getMessage());
        }

        // =========================
        // CREATE PRODUCT
        // =========================

        Product product =
                new Product(
                        user.getId(),
                        name.trim(),
                        description == null
                                ? ""
                                : description.trim(),
                        category.trim(),
                        price,
                        quantity,
                        imageUrl
                );

        // =========================
        // SAVE PRODUCT
        // =========================

        boolean added =
                productDAO.addProduct(product);

        if (added) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/seller/dashboard.jsp?success=Product+added+successfully"
            );

        } else {

            response.sendRedirect(
                    request.getContextPath()
                            + "/seller/add-product.jsp?error=Unable+to+add+product"
            );
        }
    }
}