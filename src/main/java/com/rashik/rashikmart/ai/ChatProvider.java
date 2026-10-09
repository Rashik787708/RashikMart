package com.rashik.rashikmart.ai;

/**
 * Strategy interface for RashikMart AI chatbot providers (spec §11, §17).
 */
public interface ChatProvider {

    /**
     * Generates a conversational reply for the user query with optional catalog context.
     *
     * @param userMessage sanitized user inquiry
     * @param context optional product catalog or domain context (no PII)
     * @return reply text to display to user
     */
    String getReply(String userMessage, String context);
}
