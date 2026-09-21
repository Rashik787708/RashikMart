package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.model.Product;
import com.rashik.rashikmart.model.User;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.mockito   .Mockito.*;

public class EditProductServletTest {

    private EditProductServlet servlet;
    private ProductDAO mockProductDAO;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @Before
    public void setUp() throws Exception {
        servlet = new EditProductServlet();
        mockProductDAO = mock(ProductDAO.class);

        Field daoField = EditProductServlet.class.getDeclaredField("productDAO");
        daoField.setAccessible(true);
        daoField.set(servlet, mockProductDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/RashikMart");
        when(session.getAttribute("csrfToken")).thenReturn("valid-csrf-token");
        when(request.getParameter("csrfToken")).thenReturn("valid-csrf-token");
    }

    @Test
    public void testGetUnauthenticatedRedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp?error=Please+login+first");
    }

    @Test
    public void testGetNonSellerRedirectsToLogin() throws Exception {
        User buyer = new User(2, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp?error=Seller+access+required");
    }

    @Test
    public void testGetInvalidProductIdRedirects() throws Exception {
        User seller = new User(1, "Seller", "s@test.com", "pass", "SELLER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");
        when(request.getParameter("id")).thenReturn("abc");

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/seller/dashboard.jsp?error=Invalid+Product+ID");
    }

    @Test
    public void testGetOtherSellersProductRejected() throws Exception {
        User seller = new User(1, "Seller A", "a@test.com", "pass", "SELLER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");
        when(request.getParameter("id")).thenReturn("99");

        // Product 99 belongs to a different seller, so the ownership-scoped lookup returns null
        when(mockProductDAO.findByIdAndSellerId(99, 1)).thenReturn(null);

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/seller/dashboard.jsp?error=Product+not+found+or+unauthorized");
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    public void testGetOwnProductForwardsToEditPage() throws Exception {
        User seller = new User(1, "Seller A", "a@test.com", "pass", "SELLER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");
        when(request.getParameter("id")).thenReturn("5");

        Product product = new Product(5, 1, "Apples", "Fresh", "Produce", new BigDecimal("10.00"), 20);
        when(mockProductDAO.findByIdAndSellerId(5, 1)).thenReturn(product);
        when(request.getRequestDispatcher("/seller/edit-product.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("product", product);
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testPostOversizedNameRejected() throws Exception {
        User seller = new User(1, "Seller A", "a@test.com", "pass", "SELLER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");

        when(request.getParameter("id")).thenReturn("5");
        when(request.getParameter("name")).thenReturn(repeat('x', 151));
        when(request.getParameter("category")).thenReturn("Produce");
        when(request.getParameter("price")).thenReturn("10.00");
        when(request.getParameter("quantity")).thenReturn("20");

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/seller/dashboard.jsp?error=Input+exceeds+maximum+length");
        verify(mockProductDAO, never()).updateProduct(any(Product.class));
    }

    @Test
    public void testPostInvalidPriceRejected() throws Exception {
        User seller = new User(1, "Seller A", "a@test.com", "pass", "SELLER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");

        when(request.getParameter("id")).thenReturn("5");
        when(request.getParameter("name")).thenReturn("Apples");
        when(request.getParameter("category")).thenReturn("Produce");
        when(request.getParameter("price")).thenReturn("not-a-number");
        when(request.getParameter("quantity")).thenReturn("20");

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/seller/edit-product?id=5&error=Invalid+price");
        verify(mockProductDAO, never()).updateProduct(any(Product.class));
    }

    private String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}