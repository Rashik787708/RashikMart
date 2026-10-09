package com.rashik.rashikmart.filter;

import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filter that generates or propagates a unique X-Request-Id header,
 * sets SLF4J MDC context, and ensures every response carries the header.
 */
@WebFilter(filterName = "RequestIdFilter", urlPatterns = "/*")
public class RequestIdFilter implements Filter {

    public static final String MDC_REQUEST_ID_KEY = "requestId";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestId = null;
        if (request instanceof HttpServletRequest) {
            requestId = ((HttpServletRequest) request).getHeader(HEADER_REQUEST_ID);
        }
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader(HEADER_REQUEST_ID, requestId);
        }

        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    @Override
    public void destroy() {
    }
}
