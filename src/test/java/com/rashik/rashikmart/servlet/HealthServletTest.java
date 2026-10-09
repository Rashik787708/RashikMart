package com.rashik.rashikmart.servlet;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class HealthServletTest {

    @Test
    void testDoGetReturnsHealthStatus() throws Exception {
        HealthServlet servlet = new HealthServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doGet(request, response);
        pw.flush();

        String responseBody = sw.toString();
        // Regardless of live DB connection state, response must contain status and db keys
        assertTrue(responseBody.contains("\"status\""));
        assertTrue(responseBody.contains("\"db\""));
    }
}
