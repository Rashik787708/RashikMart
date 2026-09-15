package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.dao.ReviewDAO;
import com.rashik.rashikmart.model.Product;
import com.rashik.rashikmart.model.Review;
import com.rashik.rashikmart.model.User;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

public class ReviewServletTest {

    private ReviewServlet servlet;
    private ReviewDAO mockReviewDAO;
    private ProductDAO mockProductDAO;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    private static final User BUYER = new User(10, "Buyer", "b@test.com", "pass", "BUYER");

    @Before
    public void setUp() throws Exception {
        servlet = new ReviewServlet();
        mockReviewDAO = mock(ReviewDAO.class);
        mockProductDAO = mock(ProductDAO.class);

        Field reviewDaoField = ReviewServlet.class.getDeclaredField("reviewDAO");
        reviewDaoField.setAccessible(true);
        reviewDaoField.set(servlet, mockReviewDAO);

        Field productDaoField = ReviewServlet.class.getDeclaredField("productDAO");
        productDaoField.setAccessible(true);
        productDaoField.set(servlet, mockProductDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn("/RashikMart");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(BUYER);
        when(session.getAttribute("role")).thenReturn("BUYER");
    }

    private void mockActiveProduct(int productId) {
        Product product = new Product(
                productId,
                1,
                "Reviewable",
                "desc",
                "General",
                new BigDecimal("100.00"),
                10
        );
        when(mockProductDAO.findById(productId)).thenReturn(product);
    }

    @Test
    public void testPostUnauthenticatedRedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp?error=Please+login+first");
    }

    @Test
    public void testPostNonBuyerRedirects() throws Exception {
        User seller = new User(1, "Seller", "s@test.com", "pass", "SELLER");
        when(session.getAttribute("user")).thenReturn(seller);
        when(session.getAttribute("role")).thenReturn("SELLER");

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/login.jsp?error=Buyer+access+required");
    }

    @Test
    public void testPostMissingProductIdRedirects() throws Exception {
        when(request.getParameter("productId")).thenReturn(null);

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/marketplace?error=Product+ID+required");
        verify(mockReviewDAO, never()).addReview(anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    public void testPostInvalidProductIdRedirects() throws Exception {
        when(request.getParameter("productId")).thenReturn("abc");

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/marketplace?error=Invalid+Product+ID");
        verify(mockReviewDAO, never()).addReview(anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    public void testPostUnknownProductRedirects() throws Exception {
        when(request.getParameter("productId")).thenReturn("99");
        when(mockProductDAO.findById(99)).thenReturn(null);

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/marketplace?error=Product+not+found");
        verify(mockReviewDAO, never()).addReview(anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    public void testPostInactiveProductRedirects() throws Exception {
        when(request.getParameter("productId")).thenReturn("5");
        Product product = new Product(5, 1, "Off", "desc", "General", new BigDecimal("10.00"), 5);
        product.setActive(false);
        when(mockProductDAO.findById(5)).thenReturn(product);

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/marketplace?error=Product+is+no+longer+available");
        verify(mockReviewDAO, never()).addReview(anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    public void testPostNonNumericRatingRejected() throws Exception {
        when(request.getParameter("productId")).thenReturn("5");
        when(request.getParameter("rating")).thenReturn("high");
        when(request.getParameter("reviewText")).thenReturn("Great");
        mockActiveProduct(5);

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/product-details?id=5&error=Rating+must+be+a+whole+number+from+1+to+5");
        verify(mockReviewDAO, never()).addReview(anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    public void testPostRatingOutOfRangeRejected() throws Exception {
        when(request.getParameter("productId")).thenReturn("5");
        when(request.getParameter("reviewText")).thenReturn("Great");
        mockActiveProduct(5);

        when(request.getParameter("rating")).thenReturn("0");
        servlet.doPost(request, response);

        when(request.getParameter("rating")).thenReturn("6");
        servlet.doPost(request, response);

        verify(response, times(2)).sendRedirect("/RashikMart/buyer/product-details?id=5&error=Rating+must+be+between+1+and+5");

        verify(mockReviewDAO, never()).addReview(anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    public void testPostOversizedReviewTextRejected() throws Exception {
        when(request.getParameter("productId")).thenReturn("5");
        when(request.getParameter("rating")).thenReturn("4");
        mockActiveProduct(5);

        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            longText.append('a');
        }
        when(request.getParameter("reviewText")).thenReturn(longText.toString());

        servlet.doPost(request, response);
        verify(response).sendRedirect("/RashikMart/buyer/product-details?id=5&error=Review+text+must+be+under+500+characters");
        verify(mockReviewDAO, never()).addReview(anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    public void testPostBuyerWhoHasNotPurchasedRejected() throws Exception {
        when(request.getParameter("productId")).thenReturn("5");
        when(request.getParameter("rating")).thenReturn("4");
        when(request.getParameter("reviewText")).thenReturn("Nice product");
        mockActiveProduct(5);

        when(mockReviewDAO.addReview(10, 5, 4, "Nice product"))
                .thenThrow(new IllegalStateException("You must purchase this product before reviewing it."));

        servlet.doPost(request, response);
        verify(response).sendRedirect(contains("/buyer/product-details?id=5&error=You+must+purchase+this+product+before+reviewing+it"));
    }

    @Test
    public void testPostDuplicateReviewRejected() throws Exception {
        when(request.getParameter("productId")).thenReturn("5");
        when(request.getParameter("rating")).thenReturn("4");
        when(request.getParameter("reviewText")).thenReturn("Nice product");
        mockActiveProduct(5);

        when(mockReviewDAO.addReview(10, 5, 4, "Nice product"))
                .thenThrow(new IllegalStateException("You have already reviewed this product."));

        servlet.doPost(request, response);
        verify(response).sendRedirect(contains("/buyer/product-details?id=5&error=You+have+already+reviewed+this+product"));
    }

    @Test
    public void testPostValidPurchasedProductReviewRedirectsToSuccess() throws Exception {
        when(request.getParameter("productId")).thenReturn("5");
        when(request.getParameter("rating")).thenReturn("4");
        when(request.getParameter("reviewText")).thenReturn("Nice product");
        mockActiveProduct(5);

        when(mockReviewDAO.addReview(10, 5, 4, "Nice product"))
                .thenReturn(new Review(5, 10, 4, "Nice product"));

        servlet.doPost(request, response);
        verify(mockReviewDAO).addReview(10, 5, 4, "Nice product");
        verify(response).sendRedirect("/RashikMart/buyer/product-details?id=5&success=Review+submitted+successfully");
    }
}