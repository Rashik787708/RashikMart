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
    public void init(FilterConfig filterConfig)
            throws ServletException {

        System.out.println("=================================");
        System.out.println("AuthFilter initialized");
        System.out.println("=================================");
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


        /*
         * ========================================================
         * GET EXISTING SESSION
         * ========================================================
         */

        HttpSession session =
                httpRequest.getSession(false);


        /*
         * ========================================================
         * CHECK WHETHER USER IS LOGGED IN
         * ========================================================
         */

        if (session == null
                || session.getAttribute("user") == null
                || session.getAttribute("userId") == null
                || session.getAttribute("role") == null) {

            httpResponse.sendRedirect(
                    httpRequest.getContextPath()
                            + "/login.jsp?error=Please+login+first"
            );

            return;
        }


        /*
         * ========================================================
         * GET USER ROLE
         * ========================================================
         */

        String role =
                String.valueOf(
                        session.getAttribute("role")
                );


        /*
         * ========================================================
         * GET REQUEST PATH
         * ========================================================
         */

        String requestURI =
                httpRequest.getRequestURI();

        String contextPath =
                httpRequest.getContextPath();

        String path =
                requestURI.substring(
                        contextPath.length()
                );


        /*
         * ========================================================
         * BUYER ACCESS
         * ========================================================
         */

        if (path.startsWith("/buyer/")) {

            if (!"BUYER".equalsIgnoreCase(role)) {

                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Buyer role required."
                );

                return;
            }
        }


        /*
         * ========================================================
         * SELLER ACCESS
         * ========================================================
         */

        if (path.startsWith("/seller/")) {

            if (!"SELLER".equalsIgnoreCase(role)) {

                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Seller role required."
                );

                return;
            }
        }


        /*
         * ========================================================
         * ADMIN ACCESS
         * ========================================================
         */

        if (path.startsWith("/admin/")) {

            if (!"ADMIN".equalsIgnoreCase(role)) {

                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Admin role required."
                );

                return;
            }
        }


        /*
         * ========================================================
         * USER IS AUTHORIZED
         * ========================================================
         */

        chain.doFilter(request, response);
    }


    @Override
    public void destroy() {

        System.out.println("AuthFilter destroyed");

    }
}