package com.rashik.rashikmart.ai;

import com.rashik.rashikmart.dao.ProductDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private ChatProvider mockProvider;
    private ProductDAO mockProductDAO;
    private ChatService chatService;
    private HttpSession session;
    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        mockProvider = mock(ChatProvider.class);
        mockProductDAO = mock(ProductDAO.class);
        chatService = new ChatService(mockProvider, mockProductDAO);

        session = mock(HttpSession.class);
        sessionAttributes = new HashMap<>();

        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return sessionAttributes.get(key);
        }).when(session).getAttribute(anyString());

        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            sessionAttributes.put(key, value);
            return null;
        }).when(session).setAttribute(anyString(), any());
    }

    @Test
    void testValidationRejectsEmptyMessage() {
        assertThrows(IllegalArgumentException.class, () -> chatService.processMessage("", session));
        assertThrows(IllegalArgumentException.class, () -> chatService.processMessage("   ", session));
        assertThrows(IllegalArgumentException.class, () -> chatService.processMessage(null, session));
    }

    @Test
    void testValidationRejectsOverlongMessage() {
        String longMessage = "a".repeat(501);
        assertThrows(IllegalArgumentException.class, () -> chatService.processMessage(longMessage, session));
    }

    @Test
    void testRateLimitEnforcedAfter10MessagesInOneMinute() {
        when(mockProvider.getReply(anyString(), any())).thenReturn("Mock reply");

        // 10 requests should succeed
        for (int i = 0; i < 10; i++) {
            assertNotNull(chatService.processMessage("Question " + i, session));
        }

        // 11th request must trigger RateLimitExceededException
        assertThrows(ChatService.RateLimitExceededException.class, () ->
                chatService.processMessage("Question 11", session)
        );
    }

    @Test
    void testInMemorySessionCacheReturnsCachedReplyWithoutInvokingProvider() {
        when(mockProvider.getReply(eq("how to order"), any())).thenReturn("Order reply 1");

        String reply1 = chatService.processMessage("how to order", session);
        assertEquals("Order reply 1", reply1);
        verify(mockProvider, times(1)).getReply(eq("how to order"), any());

        // Repeated identical question (case insensitive) should hit cache
        String reply2 = chatService.processMessage("HOW TO ORDER", session);
        assertEquals("Order reply 1", reply2);
        // Provider must not be called a second time
        verify(mockProvider, times(1)).getReply(anyString(), any());
    }

    @Test
    void testFallbackOnProviderException() {
        when(mockProvider.getReply(anyString(), any())).thenThrow(new RuntimeException("Simulated API failure"));

        String reply = chatService.processMessage("Any question", session);
        assertEquals(ChatService.DEGRADED_FALLBACK_REPLY, reply);
    }
}
