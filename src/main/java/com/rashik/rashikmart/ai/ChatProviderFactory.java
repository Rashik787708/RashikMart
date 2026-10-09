package com.rashik.rashikmart.ai;

/**
 * Factory pattern creating the active ChatProvider instance based on configuration.
 */
public final class ChatProviderFactory {

    private ChatProviderFactory() {
    }

    public static ChatProvider getProvider() {
        String provider = System.getenv("AI_CHATBOT_PROVIDER");
        if (provider == null || provider.isBlank()) {
            provider = System.getProperty("ai.chatbot.provider", "mock");
        }

        if ("gemini".equalsIgnoreCase(provider.trim())) {
            return new GeminiChatProvider();
        }
        return new MockChatProvider();
    }
}
