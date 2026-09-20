package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.CartDAO;
import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.model.CartItem;
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
import java.util.List;

import static org.mockito.Mockito.*;

public class CartServletTest {

    private CartServlet servlet;
    private CartDAO mockCartDAO;
    private ProductDAO mockProductDAO;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @Before
    public void setUp() throws Exception {
        servlet = new CartServlet();
        mockCartDAO = mock(CartDAO.class);
        mockProductDAO = mock(ProductDAO.class);

        Field cartDAOField = CartServlet.class.getDeclaredField("cartDAO");
        cartDAOField.setAccessible(true);
        cartDAOField.set(servlet, mockCartDAO);

        Field prodDAOField = CartServlet.class.getDeclaredField("productDAO");
        prodDAOField.setAccessible(true);
        prodDAOField.set(servlet, mockProductDAO);

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
    public void testGetNonBuyerRedirectsToLogin() throws Exception {
        User seller = new User(1, "Seller", "s@test.com", "pass", "SELLER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp?error=Buyer+access+required");
    }

    @Test
    public void testGetAuthenticatedBuyerLoadsCart() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");

        List<CartItem> items = new ArrayList<>();
        when(mockCartDAO.getCartItems(10)).thenReturn(items);
        when(mockCartDAO.getCartTotal(10)).thenReturn(new BigDecimal("120.00"));
        when(request.getRequestDispatcher("/buyer/cart.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("cartItems", items);
        verify(request).setAttribute("cartTotal", new BigDecimal("120.00"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testPostAddToCartSuccess() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");

        when(request.getParameter("action")).thenReturn("add");
        when(request.getParameter("productId")).thenReturn("5");
        when(request.getParameter("quantity")).thenReturn("2");
        when(request.getParameter("redirect")).thenReturn("cart");

        Product product = new Product(1, "Apples", "Fresh", "Produce", new BigDecimal("10.00"), 20);
        when(mockProductDAO.findById(5)).thenReturn(product);
        when(mockCartDAO.addItem(10, 5, 2)).thenReturn(true);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/RashikMart/buyer/cart?success=Item+added+to+cart");
    }

    @Test
    public void testPostAddToCartExcessQuantityFails() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");

        when(request.getParameter("action")).thenReturn("add");
        when(request.getParameter("productId")).thenReturn("5");
        when(request.getParameter("quantity")).thenReturn("50");

        Product product = new Product(1, "Apples", "Fresh", "Produce", new BigDecimal("10.00"), 10);
        when(mockProductDAO.findById(5)).thenReturn(product);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("Only+10+units+available"));
        verify(mockCartDAO, never()).addItem(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void testPostUpdateQuantity() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");

        when(request.getParameter("action")).thenReturn("update");
        when(request.getParameter("productId")).thenReturn("5");
        when(request.getParameter("quantity")).thenReturn("3");

        Product product = new Product(1, "Apples", "Fresh", "Produce", new BigDecimal("10.00"), 10);
        when(mockProductDAO.findById(5)).thenReturn(product);

        servlet.doPost(request, response);

        verify(mockCartDAO).updateQuantity(10, 5, 3);
        verify(response).sendRedirect("/RashikMart/buyer/cart?success=Cart+quantity+updated");
    }

    @Test
    public void testPostRemoveItem() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");

        when(request.getParameter("action")).thenReturn("remove");
        when(request.getParameter("productId")).thenReturn("5");

        servlet.doPost(request, response);

        verify(mockCartDAO).removeItem(10, 5);
        verify(response).sendRedirect("/RashikMart/buyer/cart?success=Item+removed");
    }
}
