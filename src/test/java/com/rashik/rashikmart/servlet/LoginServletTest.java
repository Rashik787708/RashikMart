package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.UserDAO;
import com.rashik.rashikmart.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class LoginServletTest {

    private LoginServlet servlet;
    private UserDAO mockUserDAO;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() throws Exception {
        servlet = new LoginServlet();
        mockUserDAO = mock(UserDAO.class);

        Field userDAOField = LoginServlet.class.getDeclaredField("userDAO");
        userDAOField.setAccessible(true);
        userDAOField.set(servlet, mockUserDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn("/RashikMart");
        when(request.getSession()).thenReturn(session);
    }

    @Test
    public void testGetRedirectsToLoginPage() throws Exception {
        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp");
    }

    @Test
    public void testPostMissingCredentialsRedirectsWithError() throws Exception {
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("password")).thenReturn("");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/RashikMart/login.jsp?error=Please+enter+email+and+password");
        verifyNoInteractions(mockUserDAO);
    }

    @Test
    public void testPostUserNotFoundRedirectsWithError() throws Exception {
        when(request.getParameter("email")).thenReturn("unknown@test.com");
        when(request.getParameter("password")).thenReturn("password123");
        when(mockUserDAO.findByEmail("unknown@test.com")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/RashikMart/login.jsp?error=Invalid+email+or+password");
    }

    @Test
    public void testPostIncorrectPasswordRedirectsWithError() throws Exception {
        User user = new User(1, "Alice", "alice@test.com", "$2a$12$hashed", "BUYER");
        when(request.getParameter("email")).thenReturn("alice@test.com");
        when(request.getParameter("password")).thenReturn("wrongpass");
        when(mockUserDAO.findByEmail("alice@test.com")).thenReturn(user);
        when(mockUserDAO.verifyPassword("wrongpass", "$2a$12$hashed")).thenReturn(false);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/RashikMart/login.jsp?error=Invalid+email+or+password");
        verify(session, never()).setAttribute(eq("user"), any());
    }

    @Test
    public void testPostSuccessfulBuyerLoginSetsSessionAndFixationDefense() throws Exception {
        User user = new User(42, "Bob Buyer", "bob@test.com", "$2a$12$hashed", "BUYER");
        when(request.getParameter("email")).thenReturn("bob@test.com");
        when(request.getParameter("password")).thenReturn("correctpass");
        when(mockUserDAO.findByEmail("bob@test.com")).thenReturn(user);
        when(mockUserDAO.verifyPassword("correctpass", "$2a$12$hashed")).thenReturn(true);

        servlet.doPost(request, response);

        // Verify session fixation mitigation
        verify(request).changeSessionId();

        // Verify session attributes
        verify(session).setAttribute("user", user);
        verify(session).setAttribute("userId", 42);
        verify(session).setAttribute("userName", "Bob Buyer");
        verify(session).setAttribute("userEmail", "bob@test.com");
        verify(session).setAttribute("role", "BUYER");

        // Verify redirect to buyer marketplace
        verify(response).sendRedirect("/RashikMart/buyer/marketplace");
    }

    @Test
    public void testPostSuccessfulSellerLoginRedirectsToSellerDashboard() throws Exception {
        User user = new User(99, "Sam Seller", "seller@test.com", "$2a$12$hashed", "SELLER");
        when(request.getParameter("email")).thenReturn("seller@test.com");
        when(request.getParameter("password")).thenReturn("pass");
        when(mockUserDAO.findByEmail("seller@test.com")).thenReturn(user);
        when(mockUserDAO.verifyPassword("pass", "$2a$12$hashed")).thenReturn(true);

        servlet.doPost(request, response);

        verify(session).setAttribute("role", "SELLER");
        verify(response).sendRedirect("/RashikMart/seller/dashboard.jsp");
    }

    @Test
    public void testPostSuccessfulAdminLoginRedirectsToAdminDashboard() throws Exception {
        User user = new User(1, "Root Admin", "admin@test.com", "$2a$12$hashed", "ADMIN");
        when(request.getParameter("email")).thenReturn("admin@test.com");
        when(request.getParameter("password")).thenReturn("adminpass");
        when(mockUserDAO.findByEmail("admin@test.com")).thenReturn(user);
        when(mockUserDAO.verifyPassword("adminpass", "$2a$12$hashed")).thenReturn(true);

        servlet.doPost(request, response);

        verify(session).setAttribute("role", "ADMIN");
        verify(response).sendRedirect("/RashikMart/admin/dashboard.jsp");
    }
}
