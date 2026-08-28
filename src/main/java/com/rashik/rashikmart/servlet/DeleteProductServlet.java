package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/seller/delete-product")
public class DeleteProductServlet extends HttpServlet {

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
        response.sendRedirect(request.getContextPath() + "/seller/dashboard.jsp");
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
        boolean deleted = productDAO.deleteProduct(id, user.getId());

        String redirectUrl = request.getParameter("redirect");
        if (redirectUrl == null || redirectUrl.trim().isEmpty()) {
            redirectUrl = "/seller/dashboard.jsp";
        }

        if (deleted) {
            response.sendRedirect(request.getContextPath() + redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "success=Product+deleted+successfully");
        } else {
            response.sendRedirect(request.getContextPath() + redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "error=Unable+to+delete+product");
        }
    }
}
