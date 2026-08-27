package com.rashik.rashikmart.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

import java.io.IOException;

@WebFilter(urlPatterns = {
        "/buyer.jsp",
        "/seller.jsp",
        "/admin.jsp"
})
public class AuthFilter implements Filter {


    @Override
    public void init(
            FilterConfig filterConfig)
            throws ServletException {

        System.out.println(
                "AuthFilter initialized"
        );
    }


    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;


        // =====================================================
        // GET SESSION
        // =====================================================

        HttpSession session =
                httpRequest.getSession(false);


        // =====================================================
        // CHECK LOGIN
        // =====================================================

        boolean loggedIn =
                session != null
                        && session.getAttribute("userId") != null
                        && session.getAttribute("role") != null;


        if (!loggedIn) {

            httpResponse.sendRedirect(
                    httpRequest.getContextPath()
                            + "/login.jsp?error=Please+login+first"
            );

            return;
        }


        // =====================================================
        // GET ROLE
        // =====================================================

        String role =
                String.valueOf(
                        session.getAttribute("role")
                );


        String requestURI =
                httpRequest.getRequestURI();

        String contextPath =
                httpRequest.getContextPath();

        String path =
                requestURI.substring(
                        contextPath.length()
                );


        // =====================================================
        // BUYER PAGE
        // =====================================================

        if (path.equals("/buyer.jsp")) {

            if (!"BUYER".equalsIgnoreCase(role)) {

                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Buyer role required."
                );

                return;
            }
        }


        // =====================================================
        // SELLER PAGE
        // =====================================================

        if (path.equals("/seller.jsp")) {

            if (!"SELLER".equalsIgnoreCase(role)) {

                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Seller role required."
                );

                return;
            }
        }


        // =====================================================
        // ADMIN PAGE
        // =====================================================

        if (path.equals("/admin.jsp")) {

            if (!"ADMIN".equalsIgnoreCase(role)) {

                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Admin role required."
                );

                return;
            }
        }


        // =====================================================
        // ALLOW REQUEST
        // =====================================================

        chain.doFilter(
                request,
                response
        );
    }


    @Override
    public void destroy() {

        System.out.println(
                "AuthFilter destroyed"
        );
    }
}