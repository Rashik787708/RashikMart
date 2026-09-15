package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.dao.ReviewDAO;
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
import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class ProductDetailsServletTest {

    private ProductDetailsServlet servlet;
    private ProductDAO mockProductDAO;
    private ReviewDAO mockReviewDAO;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @Before
    public void setUp() throws Exception {
        servlet = new ProductDetailsServlet();
        mockProductDAO = mock(ProductDAO.class);
        mockReviewDAO = mock(ReviewDAO.class);

        Field daoField = ProductDetailsServlet.class.getDeclaredField("productDAO");
        daoField.setAccessible(true);
        daoField.set(servlet, mockProductDAO);

        Field reviewDaoField = ProductDetailsServlet.class.getDeclaredField("reviewDAO");
        reviewDaoField.setAccessible(true);
        reviewDaoField.set(servlet, mockReviewDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/RashikMart");
    }

    @Test
    public void testGetUnauthenticatedRedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp?error=Please+login+first");
    }

    @Test
    public void testGetNonBuyerRedirects() throws Exception {
        User seller = new User(1, "Seller", "s@test.com", "pass", "SELLER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp?error=Buyer+access+required");
    }

    @Test
    public void testGetMissingIdRedirects() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");
        when(request.getParameter("id")).thenReturn("");
        when(request.getRequestURI()).thenReturn("/RashikMart/buyer/product-details");

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/marketplace?error=Product+ID+required");
    }

    @Test
    public void testGetInvalidIdRedirects() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");
        when(request.getParameter("id")).thenReturn("abc");

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/marketplace?error=Invalid+Product+ID");
    }

    @Test
    public void testGetNotFoundRedirects() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");
        when(request.getParameter("id")).thenReturn("5");
        when(mockProductDAO.findById(5)).thenReturn(null);

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/marketplace?error=Product+not+found");
    }

    @Test
    public void testGetInactiveProductRedirects() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");
        when(request.getParameter("id")).thenReturn("5");

        Product product = new Product(5, 1, "Apples", "Fresh", "Produce", new BigDecimal("10.00"), 0);
        product.setActive(false);
        when(mockProductDAO.findById(5)).thenReturn(product);

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/marketplace?error=Product+is+no+longer+available");
    }

    @Test
    public void testGetActiveProductForwardsToDetails() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");
        when(request.getParameter("id")).thenReturn("5");

        Product product = new Product(5, 1, "Apples", "Fresh", "Produce", new BigDecimal("10.00"), 20);
        when(mockProductDAO.findById(5)).thenReturn(product);
        when(request.getRequestDispatcher("/buyer/product-details.jsp")).thenReturn(dispatcher);

        when(mockReviewDAO.findByProductId(5)).thenReturn(new ArrayList<>());
        when(mockReviewDAO.getReviewCount(5)).thenReturn(0);
        when(mockReviewDAO.getAverageRating(5)).thenReturn(0.0);
        when(mockReviewDAO.hasPurchasedProduct(10, 5)).thenReturn(false);
        when(mockReviewDAO.hasReviewByBuyer(10, 5)).thenReturn(false);

        servlet.doGet(request, response);

        verify(request).setAttribute("product", product);
        verify(request).setAttribute("reviews", new ArrayList<>());
        verify(dispatcher).forward(request, response);
    }
}