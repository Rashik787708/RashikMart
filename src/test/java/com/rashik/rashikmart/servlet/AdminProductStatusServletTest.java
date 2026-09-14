package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.model.User;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class AdminProductStatusServletTest {

    private AdminProductStatusServlet servlet;
    private ProductDAO mockProductDAO;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() throws Exception {
        servlet = new AdminProductStatusServlet();
        mockProductDAO = mock(ProductDAO.class);

        Field daoField = AdminProductStatusServlet.class.getDeclaredField("productDAO");
        daoField.setAccessible(true);
        daoField.set(servlet, mockProductDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn("/RashikMart");
    }

    @Test
    public void testPostUnauthenticatedRedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp?error=Please+login+first");
    }

    @Test
    public void testPostNonAdminForbidden() throws Exception {
        User seller = new User(1, "Seller", "s@test.com", "pass", "SELLER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");

        servlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin role required.");
        verify(mockProductDAO, never()).toggleProductActive(anyInt(), anyBoolean());
    }

    @Test
    public void testPostAdminTogglesProductStatus() throws Exception {
        User admin = new User(1, "Admin", "admin@test.com", "pass", "ADMIN");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(admin);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getParameter("active")).thenReturn("false");

        servlet.doPost(request, response);

        verify(mockProductDAO).toggleProductActive(5, false);
        verify(response).sendRedirect("/RashikMart/admin/dashboard.jsp?success=Product+status+updated");
    }

    @Test
    public void testPostAdminInvalidIdDoesNotTouchDatabase() throws Exception {
        User admin = new User(1, "Admin", "admin@test.com", "pass", "ADMIN");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(admin);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(request.getParameter("id")).thenReturn("not-a-number");
        when(request.getParameter("active")).thenReturn("true");

        servlet.doPost(request, response);

        verify(mockProductDAO, never()).toggleProductActive(anyInt(), anyBoolean());
        verify(response).sendRedirect("/RashikMart/admin/dashboard.jsp?success=Product+status+updated");
    }
}