package com.rashik.rashikmart.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockChatProviderTest {

    private MockChatProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MockChatProvider();
    }

    @Test
    void testEmptyMessage() {
        String reply = provider.getReply("", "");
        assertNotNull(reply);
        assertTrue(reply.contains("How can I assist you"));
    }

    @Test
    void test1HowToOrder() {
        String reply = provider.getReply("How do I place an order?", null);
        assertTrue(reply.toLowerCase().contains("checkout"));
    }

    @Test
    void test2Payment() {
        String reply = provider.getReply("Is the payment real or mock?", null);
        assertTrue(reply.toLowerCase().contains("mock"));
    }

    @Test
    void test3Returns() {
        String reply = provider.getReply("What is your refund policy?", null);
        assertTrue(reply.toLowerCase().contains("return") || reply.toLowerCase().contains("refund"));
    }

    @Test
    void test4SellerListing() {
        String reply = provider.getReply("How can a seller list a product?", null);
        assertTrue(reply.toLowerCase().contains("dashboard") || reply.toLowerCase().contains("seller"));
    }

    @Test
    void test5Categories() {
        String reply = provider.getReply("What categories are available?", null);
        assertTrue(reply.toLowerCase().contains("electronics") || reply.toLowerCase().contains("category"));
    }

    @Test
    void test6Reviews() {
        String reply = provider.getReply("How do reviews and star ratings work?", null);
        assertTrue(reply.toLowerCase().contains("review") || reply.toLowerCase().contains("rating"));
    }

    @Test
    void test7OrderStatus() {
        String reply = provider.getReply("What does shipped status mean?", null);
        assertTrue(reply.toLowerCase().contains("shipped") || reply.toLowerCase().contains("pending"));
    }

    @Test
    void test8ContactSupport() {
        String reply = provider.getReply("How to contact customer support?", null);
        assertTrue(reply.toLowerCase().contains("support@rashikmart.com"));
    }

    @Test
    void test9Shipping() {
        String reply = provider.getReply("What is the shipping and delivery time?", null);
        assertTrue(reply.toLowerCase().contains("delivery") || reply.toLowerCase().contains("days"));
    }

    @Test
    void test10Registration() {
        String reply = provider.getReply("How do I register a new account?", null);
        assertTrue(reply.toLowerCase().contains("register") || reply.toLowerCase().contains("buyer"));
    }

    @Test
    void testOffTopicRefusal() {
        String reply = provider.getReply("What is the weather in Paris today?", null);
        assertTrue(reply.toLowerCase().contains("specialized only in helping you"));
    }

    @Test
    void testCatalogContextIncluded() {
        String reply = provider.getReply("Tell me about organic honey", "Organic Forest Honey ($15.00)");
        assertTrue(reply.contains("Organic Forest Honey"));
    }
}
