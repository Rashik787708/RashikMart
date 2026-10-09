package com.rashik.rashikmart.ai;

import java.util.Locale;

/**
 * Mock chatbot provider with deterministic canned responses for at least 10 FAQ topics (spec §11, §17).
 */
public class MockChatProvider implements ChatProvider {

    @Override
    public String getReply(String userMessage, String context) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Hello! How can I assist you with RashikMart today?";
        }

        String msg = userMessage.toLowerCase(Locale.ROOT).trim();

        // 1. How to order
        if (msg.contains("order") && (msg.contains("how") || msg.contains("place") || msg.contains("buy"))) {
            return "To order, browse products on the Marketplace, click 'Add to Cart', navigate to your Cart, and click 'Proceed to Checkout'. Your order will be placed instantly!";
        }

        // 2. Payment details
        if (msg.contains("payment") || msg.contains("card") || msg.contains("pay") || msg.contains("mock")) {
            return "RashikMart uses a simulated mock payment gateway for capstone testing. No real credit card or bank charges will be processed.";
        }

        // 3. Returns and refunds
        if (msg.contains("return") || msg.contains("refund") || msg.contains("cancel")) {
            return "RashikMart offers a simulated 7-day return policy on eligible purchases. To request a return, check your Orders page or email support@rashikmart.com.";
        }

        // 4. Seller listing
        if (msg.contains("seller") && (msg.contains("list") || msg.contains("add") || msg.contains("sell") || msg.contains("product"))) {
            return "Sellers can log in to their Seller Dashboard and click 'Add New Product'. Fill in the product title, category, price, stock quantity, and upload a photo.";
        }

        // 5. Categories
        if (msg.contains("categor") || msg.contains("genre") || msg.contains("department")) {
            return "RashikMart features products across Electronics, Fashion, Home & Kitchen, Books, Spices, and Grocery.";
        }

        // 6. Reviews
        if (msg.contains("review") || msg.contains("rating") || msg.contains("feedback") || msg.contains("star")) {
            return "Buyers can leave a rating (1-5 stars) and a written review for items from their delivered orders. One review is permitted per buyer per product.";
        }

        // 7. Order status meanings
        if (msg.contains("status") || msg.contains("track") || msg.contains("shipped") || msg.contains("delivered")) {
            return "Order statuses proceed through: PENDING (placed), CONFIRMED (payment verified), SHIPPED (dispatched by seller), and DELIVERED (received by buyer).";
        }

        // 8. Contact & support
        if (msg.contains("contact") || msg.contains("support") || msg.contains("help") || msg.contains("email")) {
            return "Need assistance? You can reach RashikMart customer support 24/7 at support@rashikmart.com or through our online help desk.";
        }

        // 9. Shipping & delivery
        if (msg.contains("shipping") || msg.contains("delivery") || msg.contains("dispatch") || msg.contains("days")) {
            return "Standard delivery takes approximately 3-5 business days. Once your seller marks your order SHIPPED, your package is on its way!";
        }

        // 10. Account creation & registration
        if (msg.contains("account") || msg.contains("register") || msg.contains("signup") || msg.contains("sign up") || msg.contains("login")) {
            return "You can register for a RashikMart account as a BUYER to shop or a SELLER to list items. Simply click 'Register' at the top right of the page.";
        }

        // Off-topic refusal check
        if (msg.contains("weather") || msg.contains("president") || msg.contains("football") || msg.contains("capital of")) {
            return "I am RashikMart Assistant, specialized only in helping you with our products, shopping, and marketplace orders. How can I help you shop today?";
        }

        // Incorporate context if available
        if (context != null && !context.isBlank()) {
            return "Here is what I found in our catalog: " + context + ". Would you like help adding any of these to your cart?";
        }

        return "I can help you with placing orders, payment information, seller listings, product categories, returns, and order status. What would you like to know?";
    }
}
