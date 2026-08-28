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

@WebServlet("/seller/edit-product")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,       // 1MB
        maxFileSize = 1024 * 1024 * 5,          // 5MB
        maxRequestSize = 1024 * 1024 * 10       // 10MB
)
public class EditProductServlet extends HttpServlet {

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
        if (role == null || !"SELLER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Seller+access+required");
            return;
        }

        String idText = request.getParameter("id");
        if (idText == null || idText.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/dashboard.jsp?error=Product+ID+required");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/seller/dashboard.jsp?error=Invalid+Product+ID");
            return;
        }

        User user = (User) session.getAttribute("user");
        Product product = productDAO.findByIdAndSellerId(id, user.getId());

        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/seller/dashboard.jsp?error=Product+not+found+or+unauthorized");
            return;
        }

        request.setAttribute("product", product);
        request.getRequestDispatcher("/seller/edit-product.jsp").forward(request, response);
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
        if (role == null || !"SELLER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Seller+access+required");
            return;
        }

        User user = (User) session.getAttribute("user");

        String idText = request.getParameter("id");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String category = request.getParameter("category");
        String priceText = request.getParameter("price");
        String quantityText = request.getParameter("quantity");
        String currentImageUrl = request.getParameter("currentImageUrl");

        if (idText == null || idText.trim().isEmpty()
                || name == null || name.trim().isEmpty()
                || category == null || category.trim().isEmpty()
                || priceText == null || priceText.trim().isEmpty()
                || quantityText == null || quantityText.trim().isEmpty()) {

            response.sendRedirect(request.getContextPath() + "/seller/edit-product?id=" + idText + "&error=Please+fill+all+required+fields");
            return;
        }

        int id;
        BigDecimal price;
        int quantity;

        try {
            id = Integer.parseInt(idText.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/seller/dashboard.jsp?error=Invalid+Product+ID");
            return;
        }

        try {
            price = new BigDecimal(priceText.trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                response.sendRedirect(request.getContextPath() + "/seller/edit-product?id=" + id + "&error=Price+must+be+greater+than+zero");
                return;
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/seller/edit-product?id=" + id + "&error=Invalid+price");
            return;
        }

        try {
            quantity = Integer.parseInt(quantityText.trim());
            if (quantity <= 0) {
                response.sendRedirect(request.getContextPath() + "/seller/edit-product?id=" + id + "&error=Quantity+must+be+greater+than+zero");
                return;
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/seller/edit-product?id=" + id + "&error=Invalid+quantity");
            return;
        }

        String imageUrl = (currentImageUrl != null && !currentImageUrl.trim().isEmpty())
                ? currentImageUrl.trim()
                : "default-product.svg";

        // Check if a new photo is uploaded
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
            System.err.println("Error processing edit product image upload: " + e.getMessage());
        }

        Product product = new Product(
                id,
                user.getId(),
                name.trim(),
                description == null ? "" : description.trim(),
                category.trim(),
                price,
                quantity,
                imageUrl
        );

        boolean updated = productDAO.updateProduct(product);

        if (updated) {
            response.sendRedirect(request.getContextPath() + "/seller/dashboard.jsp?success=Product+updated+successfully");
        } else {
            response.sendRedirect(request.getContextPath() + "/seller/edit-product?id=" + id + "&error=Unable+to+update+product");
        }
    }
}
