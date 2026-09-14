package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.UserDAO;
import com.rashik.rashikmart.model.User;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class RegisterServletTest {

    private RegisterServlet servlet;
    private UserDAO mockUserDAO;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        servlet = new RegisterServlet();
        mockUserDAO = mock(UserDAO.class);

        Field daoField = RegisterServlet.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(servlet, mockUserDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        when(request.getContextPath()).thenReturn("/RashikMart");
    }

    @Test
    public void testGetRedirectsToRegisterPage() throws Exception {
        servlet.doGet(request, response);
        verify(response).sendRedirect("/RashikMart/register.jsp");
    }

    @Test
    public void testMissingFieldsRedirectWithError() throws Exception {
        when(request.getParameter("name")).thenReturn("");
        when(request.getParameter("email")).thenReturn("a@b.com");
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getParameter("role")).thenReturn("BUYER");

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/register.jsp?error=Please+fill+all+fields");
    }

    @Test
    public void testInvalidRoleRejected() throws Exception {
        when(request.getParameter("name")).thenReturn("Test User");
        when(request.getParameter("email")).thenReturn("user@test.com");
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getParameter("role")).thenReturn("ADMIN"); // Admin registration not allowed via public form

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/register.jsp?error=Invalid+role");
    }

    @Test
    public void testInvalidEmailFormatRejected() throws Exception {
        when(request.getParameter("name")).thenReturn("Test User");
        when(request.getParameter("email")).thenReturn("invalid-email-no-at");
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getParameter("role")).thenReturn("BUYER");

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/register.jsp?error=Invalid+email+format");
    }

    @Test
    public void testShortPasswordRejected() throws Exception {
        when(request.getParameter("name")).thenReturn("Test User");
        when(request.getParameter("email")).thenReturn("user@test.com");
        when(request.getParameter("password")).thenReturn("123"); // Less than 6 chars
        when(request.getParameter("role")).thenReturn("BUYER");

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/register.jsp?error=Password+must+be+at+least+6+characters");
    }

    @Test
    public void testDuplicateEmailRejected() throws Exception {
        when(request.getParameter("name")).thenReturn("Existing User");
        when(request.getParameter("email")).thenReturn("dup@test.com");
        when(request.getParameter("password")).thenReturn("password123");
        when(request.getParameter("role")).thenReturn("BUYER");

        when(mockUserDAO.findByEmail("dup@test.com")).thenReturn(new User(1, "Existing", "dup@test.com", "hash", "BUYER"));

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/register.jsp?error=Email+already+exists");
        verify(mockUserDAO, never()).registerUser(any());
    }

    @Test
    public void testSuccessfulRegistrationRedirectsToLogin() throws Exception {
        when(request.getParameter("name")).thenReturn("New Buyer");
        when(request.getParameter("email")).thenReturn("newbuyer@test.com");
        when(request.getParameter("password")).thenReturn("securePass123");
        when(request.getParameter("role")).thenReturn("BUYER");

        when(mockUserDAO.findByEmail("newbuyer@test.com")).thenReturn(null);
        when(mockUserDAO.registerUser(any(User.class))).thenReturn(true);

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp?success=Registration+successful");
    }
}
