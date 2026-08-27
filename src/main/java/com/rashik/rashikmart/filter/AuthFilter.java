package com.rashik.rashikmart.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

@WebFilter(urlPatterns = {
        "/buyer/*",
        "/seller/*",
        "/admin/*"
})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("AuthFilter initialized");
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        boolean loggedIn = session != null
                && session.getAttribute("user") != null;

        if (!loggedIn) {
            httpResponse.sendRedirect(
                    httpRequest.getContextPath()
                            + "/login.jsp?error=Please+login+first"
            );
            return;
        }

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        String path = requestURI.substring(contextPath.length());

        String role = (String) session.getAttribute("role");

        if (path.startsWith("/buyer/")) {

            if (!"BUYER".equals(role)) {
                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Buyer role required."
                );
                return;
            }

        } else if (path.startsWith("/seller/")) {

            if (!"SELLER".equals(role)) {
                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Seller role required."
                );
                return;
            }

        } else if (path.startsWith("/admin/")) {

            if (!"ADMIN".equals(role)) {
                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Admin role required."
                );
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("AuthFilter destroyed");
    }
}