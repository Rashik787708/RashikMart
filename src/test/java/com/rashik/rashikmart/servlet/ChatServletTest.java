package com.rashik.rashikmart.servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rashik.rashikmart.ai.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServletTest {

    private ChatService mockChatService;
    private ChatServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        mockChatService = mock(ChatService.class);
        servlet = new ChatServlet(mockChatService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getSession(true)).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
    }

    @Test
    void testRejectsRequestWithoutValidCsrfToken() throws Exception {
        when(session.getAttribute("csrfToken")).thenReturn("valid-session-token");
        when(request.getHeader("X-CSRF-Token")).thenReturn("wrong-token");

        servlet.doPost(request, response);
        printWriter.flush();

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        JsonObject json = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean());
        assertEquals("FORBIDDEN", json.getAsJsonObject("error").get("code").getAsString());
    }

    @Test
    void testReturns400OnInvalidMessage() throws Exception {
        when(session.getAttribute("csrfToken")).thenReturn("valid-token");
        when(request.getHeader("X-CSRF-Token")).thenReturn("valid-token");
        when(request.getParameter("message")).thenReturn("");
        when(mockChatService.processMessage(any(), any())).thenThrow(new IllegalArgumentException("Message cannot be empty"));

        servlet.doPost(request, response);
        printWriter.flush();

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject json = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean());
        assertEquals("BAD_REQUEST", json.getAsJsonObject("error").get("code").getAsString());
    }

    @Test
    void testReturns429OnRateLimitExceeded() throws Exception {
        when(session.getAttribute("csrfToken")).thenReturn("valid-token");
        when(request.getHeader("X-CSRF-Token")).thenReturn("valid-token");
        when(request.getParameter("message")).thenReturn("Hello");
        when(mockChatService.processMessage(eq("Hello"), any()))
                .thenThrow(new ChatService.RateLimitExceededException("Rate limit exceeded"));

        servlet.doPost(request, response);
        printWriter.flush();

        verify(response).setStatus(429);
        JsonObject json = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean());
        assertEquals("RATE_LIMIT_EXCEEDED", json.getAsJsonObject("error").get("code").getAsString());
    }

    @Test
    void testReturns200WithReplyEnvelopeOnSuccess() throws Exception {
        when(session.getAttribute("csrfToken")).thenReturn("valid-token");
        when(request.getHeader("X-CSRF-Token")).thenReturn("valid-token");
        when(request.getParameter("message")).thenReturn("How do I order?");
        when(mockChatService.processMessage(eq("How do I order?"), any()))
                .thenReturn("Click Add to Cart and checkout.");

        servlet.doPost(request, response);
        printWriter.flush();

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject json = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertTrue(json.get("success").getAsBoolean());
        assertTrue(json.get("error").isJsonNull());
        assertEquals("Click Add to Cart and checkout.", json.getAsJsonObject("data").get("reply").getAsString());
    }
}
