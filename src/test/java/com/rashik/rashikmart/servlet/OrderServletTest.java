package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.CartDAO;
import com.rashik.rashikmart.dao.OrderDAO;
import com.rashik.rashikmart.model.CartItem;
import com.rashik.rashikmart.model.Order;
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

public class OrderServletTest {

    private OrderServlet servlet;
    private OrderDAO mockOrderDAO;
    private CartDAO mockCartDAO;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @Before
    public void setUp() throws Exception {
        servlet = new OrderServlet();
        mockOrderDAO = mock(OrderDAO.class);
        mockCartDAO = mock(CartDAO.class);

        Field orderDaoField = OrderServlet.class.getDeclaredField("orderDAO");
        orderDaoField.setAccessible(true);
        orderDaoField.set(servlet, mockOrderDAO);

        Field cartDaoField = OrderServlet.class.getDeclaredField("cartDAO");
        cartDaoField.setAccessible(true);
        cartDaoField.set(servlet, mockCartDAO);

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
    public void testGetNonBuyerRedirects() throws Exception {
        User seller = new User(1, "Seller", "s@test.com", "pass", "SELLER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp?error=Buyer+access+required");
    }

    @Test
    public void testGetOrderInvalidIdRedirects() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");
        when(request.getRequestURI()).thenReturn("/RashikMart/buyer/order");
        when(request.getParameter("id")).thenReturn("abc");

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/orders?error=Invalid+Order+ID");
    }

    @Test
    public void testGetOtherBuyersOrderRejected() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");
        when(request.getRequestURI()).thenReturn("/RashikMart/buyer/order");
        when(request.getParameter("id")).thenReturn("77");

        // Order 77 belongs to another buyer; ownership-scoped lookup returns null
        when(mockOrderDAO.findOrderById(77, 10)).thenReturn(null);

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/orders?error=Order+not+found+or+unauthorized");
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    public void testGetOwnOrderForwardsToDetails() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");
        when(request.getRequestURI()).thenReturn("/RashikMart/buyer/order");
        when(request.getParameter("id")).thenReturn("5");

        Order order = new Order(5, 10, new BigDecimal("120.00"), "CONFIRMED", null);
        when(mockOrderDAO.findOrderById(5, 10)).thenReturn(order);
        when(request.getRequestDispatcher("/buyer/order-details.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("order", order);
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testGetCheckoutEmptyCartRedirects() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");
        when(request.getRequestURI()).thenReturn("/RashikMart/buyer/checkout");
        when(mockCartDAO.getCartItems(10)).thenReturn(new ArrayList<>());

        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/cart?error=Your+cart+is+empty");
    }

    @Test
    public void testGetCheckoutPopulatedCartForwards() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");
        when(request.getRequestURI()).thenReturn("/RashikMart/buyer/checkout");

        List<CartItem> items = new ArrayList<>();
        items.add(new CartItem());
        when(mockCartDAO.getCartItems(10)).thenReturn(items);
        when(mockCartDAO.getCartTotal(10)).thenReturn(new BigDecimal("120.00"));
        when(request.getRequestDispatcher("/buyer/checkout.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("cartItems", items);
        verify(request).setAttribute("cartTotal", new BigDecimal("120.00"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testPostPlaceOrderSuccessRedirectsToOrderSuccess() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");

        Order order = new Order(88, 10, new BigDecimal("120.00"), "CONFIRMED", null);
        when(mockOrderDAO.createOrderFromCart(10)).thenReturn(order);

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/order-success.jsp?orderId=88&success=Order+placed+successfully");
    }

    @Test
    public void testPostPlaceOrderEmptyCartRedirectsWithError() throws Exception {
        User buyer = new User(10, "Buyer", "b@test.com", "pass", "BUYER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");

        when(mockOrderDAO.createOrderFromCart(10)).thenThrow(new IllegalStateException("Your cart is empty"));

        servlet.doPost(request, response);
        verify(response).sendRedirect(contains("Your+cart+is+empty"));
    }
}