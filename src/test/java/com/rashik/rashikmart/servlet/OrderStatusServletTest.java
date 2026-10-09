package com.rashik.rashikmart.servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rashik.rashikmart.dao.OrderDAO;
import com.rashik.rashikmart.model.Order;
import com.rashik.rashikmart.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderStatusServletTest {

    private OrderDAO mockOrderDAO;
    private OrderStatusServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    private final User seller = new User(1, "Seller One", "seller1@test.com", "pass", "SELLER");
    private final User buyer = new User(2, "Buyer One", "buyer1@test.com", "pass", "BUYER");

    @BeforeEach
    void setUp() throws Exception {
        mockOrderDAO = mock(OrderDAO.class);
        servlet = new OrderStatusServlet(mockOrderDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/RashikMart");
    }

    @Test
    void testUnauthenticatedRedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);
        verify(response).sendRedirect(contains("/login.jsp"));
    }

    @Test
    void testBuyerCannotUpdateOrderStatus() throws Exception {
        when(session.getAttribute("user")).thenReturn(buyer);
        when(session.getAttribute("role")).thenReturn("BUYER");

        servlet.doPost(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    void testInvalidCsrfTokenRejected() throws Exception {
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");
        when(session.getAttribute("csrfToken")).thenReturn("token-1");
        when(request.getHeader("X-CSRF-Token")).thenReturn("wrong-token");

        servlet.doPost(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    void testSellerNotOwningOrderRejected() throws Exception {
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");
        when(session.getAttribute("csrfToken")).thenReturn("token-1");
        when(request.getHeader("X-CSRF-Token")).thenReturn("token-1");
        when(request.getParameter("orderId")).thenReturn("55");
        when(request.getParameter("status")).thenReturn("SHIPPED");

        Order order = new Order(55, 10, new BigDecimal("100.00"), "CONFIRMED", new Timestamp(System.currentTimeMillis()));
        when(mockOrderDAO.findById(55)).thenReturn(order);
        when(mockOrderDAO.isSellerForOrder(55, 1)).thenReturn(false);

        servlet.doPost(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    void testInvalidTransitionRejectedWith409Conflict() throws Exception {
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");
        when(session.getAttribute("csrfToken")).thenReturn("token-1");
        when(request.getHeader("X-CSRF-Token")).thenReturn("token-1");
        when(request.getParameter("orderId")).thenReturn("55");
        // Attempting to skip SHIPPED directly to DELIVERED from CONFIRMED
        when(request.getParameter("status")).thenReturn("DELIVERED");

        Order order = new Order(55, 10, new BigDecimal("100.00"), "CONFIRMED", new Timestamp(System.currentTimeMillis()));
        when(mockOrderDAO.findById(55)).thenReturn(order);
        when(mockOrderDAO.isSellerForOrder(55, 1)).thenReturn(true);

        servlet.doPost(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_CONFLICT), anyString());
    }

    @Test
    void testValidTransitionConfirmedToShippedSucceeds() throws Exception {
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");
        when(session.getAttribute("csrfToken")).thenReturn("token-1");
        when(request.getHeader("X-CSRF-Token")).thenReturn("token-1");
        when(request.getParameter("orderId")).thenReturn("55");
        when(request.getParameter("status")).thenReturn("SHIPPED");

        Order order = new Order(55, 10, new BigDecimal("100.00"), "CONFIRMED", new Timestamp(System.currentTimeMillis()));
        when(mockOrderDAO.findById(55)).thenReturn(order);
        when(mockOrderDAO.isSellerForOrder(55, 1)).thenReturn(true);
        when(mockOrderDAO.updateOrderStatus(55, "SHIPPED")).thenReturn(true);

        servlet.doPost(request, response);
        verify(mockOrderDAO).updateOrderStatus(55, "SHIPPED");
        verify(response).sendRedirect(contains("marked+as+SHIPPED"));
    }

    @Test
    void testValidTransitionShippedToDeliveredSucceeds() throws Exception {
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");
        when(session.getAttribute("csrfToken")).thenReturn("token-1");
        when(request.getHeader("X-CSRF-Token")).thenReturn("token-1");
        when(request.getParameter("orderId")).thenReturn("55");
        when(request.getParameter("status")).thenReturn("DELIVERED");

        Order order = new Order(55, 10, new BigDecimal("100.00"), "SHIPPED", new Timestamp(System.currentTimeMillis()));
        when(mockOrderDAO.findById(55)).thenReturn(order);
        when(mockOrderDAO.isSellerForOrder(55, 1)).thenReturn(true);
        when(mockOrderDAO.updateOrderStatus(55, "DELIVERED")).thenReturn(true);

        servlet.doPost(request, response);
        verify(mockOrderDAO).updateOrderStatus(55, "DELIVERED");
        verify(response).sendRedirect(contains("marked+as+DELIVERED"));
    }

    @Test
    void testJsonApiReturns409ConflictEnvelope() throws Exception {
        when(request.getRequestURI()).thenReturn("/RashikMart/api/v1/orders/status");
        when(request.getHeader("Accept")).thenReturn("application/json");
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");
        when(session.getAttribute("csrfToken")).thenReturn("token-1");
        when(request.getHeader("X-CSRF-Token")).thenReturn("token-1");
        when(request.getParameter("orderId")).thenReturn("55");
        when(request.getParameter("status")).thenReturn("DELIVERED"); // Invalid from CONFIRMED

        Order order = new Order(55, 10, new BigDecimal("100.00"), "CONFIRMED", new Timestamp(System.currentTimeMillis()));
        when(mockOrderDAO.findById(55)).thenReturn(order);
        when(mockOrderDAO.isSellerForOrder(55, 1)).thenReturn(true);

        servlet.doPost(request, response);
        printWriter.flush();

        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
        JsonObject json = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean());
        assertEquals("CONFLICT", json.getAsJsonObject("error").get("code").getAsString());
    }
}
