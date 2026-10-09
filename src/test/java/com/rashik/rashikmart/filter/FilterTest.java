package com.rashik.rashikmart.filter;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilterTest {

    @Test
    void testEncodingFilterSetsUtf8() throws Exception {
        EncodingFilter filter = new EncodingFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getCharacterEncoding()).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
        verify(chain).doFilter(request, response);
    }

    @Test
    void testRequestIdFilterGeneratesUuidWhenHeaderMissing() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("X-Request-Id")).thenReturn(null);

        doAnswer(invocation -> {
            assertNotNull(MDC.get("requestId"));
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq("X-Request-Id"), captor.capture());
        assertNotNull(captor.getValue());
        assertFalse(captor.getValue().isBlank());
        // MDC must be cleaned up after request
        assertNull(MDC.get("requestId"));
    }

    @Test
    void testRequestIdFilterPreservesExistingHeader() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("X-Request-Id")).thenReturn("client-req-1234");

        filter.doFilter(request, response, chain);

        verify(response).setHeader("X-Request-Id", "client-req-1234");
        assertNull(MDC.get("requestId"));
    }
}
