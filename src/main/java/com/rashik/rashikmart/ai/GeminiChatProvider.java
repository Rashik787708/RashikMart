package com.rashik.rashikmart.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Server-side Gemini AI chatbot provider (spec §11, §17).
 * Calls Gemini REST API with strict timeouts, zero PII, and automatic fallback.
 */
public class GeminiChatProvider implements ChatProvider {

    private static final Logger logger = LoggerFactory.getLogger(GeminiChatProvider.class);

    private static final String DEFAULT_MODEL = "gemini-3.1-flash-lite";
    private static final Duration TIMEOUT = Duration.ofSeconds(8);

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final MockChatProvider fallbackProvider;

    public GeminiChatProvider() {
        this(
                System.getenv("GEMINI_API_KEY"),
                System.getenv("GEMINI_MODEL") != null && !System.getenv("GEMINI_MODEL").isBlank()
                        ? System.getenv("GEMINI_MODEL")
                        : DEFAULT_MODEL,
                HttpClient.newBuilder().connectTimeout(TIMEOUT).build()
        );
    }

    public GeminiChatProvider(String apiKey, String model, HttpClient httpClient) {
        this.apiKey = apiKey != null ? apiKey.trim() : null;
        this.model = (model != null && !model.isBlank()) ? model.trim() : DEFAULT_MODEL;
        this.httpClient = httpClient;
        this.fallbackProvider = new MockChatProvider();
    }

    @Override
    public String getReply(String userMessage, String context) {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("GEMINI_API_KEY not set. Falling back to MockChatProvider.");
            return fallbackProvider.getReply(userMessage, context);
        }

        try {
            String systemInstructions = """
                    You are RashikMart Assistant, a polite e-commerce shopping assistant for the RashikMart online marketplace.
                    Rules:
                    1. ONLY answer questions related to RashikMart products, orders, shopping, cart, categories, returns, seller listings, and marketplace features.
                    2. If the user asks about anything unrelated to RashikMart or shopping (politics, weather, general trivia, coding, etc.), politely decline and steer them back to shopping on RashikMart.
                    3. Keep your answers concise, helpful, and under 3-4 sentences.
                    4. Never mention internal system details or credentials.
                    """;

            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append(systemInstructions).append("\n\n");
            if (context != null && !context.isBlank()) {
                promptBuilder.append("Current Catalog Context: ").append(context).append("\n\n");
            }
            promptBuilder.append("Customer Question: ").append(userMessage).append("\nAnswer:");

            JsonObject part = new JsonObject();
            part.addProperty("text", promptBuilder.toString());

            JsonArray parts = new JsonArray();
            parts.add(part);

            JsonObject content = new JsonObject();
            content.add("parts", parts);

            JsonArray contents = new JsonArray();
            contents.add(content);

            JsonObject requestBody = new JsonObject();
            requestBody.add("contents", contents);

            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    JsonObject candidateContent = firstCandidate.getAsJsonObject("content");
                    if (candidateContent != null) {
                        JsonArray respParts = candidateContent.getAsJsonArray("parts");
                        if (respParts != null && !respParts.isEmpty()) {
                            String reply = respParts.get(0).getAsJsonObject().get("text").getAsString();
                            if (reply != null && !reply.isBlank()) {
                                return reply.trim();
                            }
                        }
                    }
                }
            } else {
                logger.warn("Gemini API call failed with status: {}. Falling back to mock provider.", response.statusCode());
            }
        } catch (Exception e) {
            logger.warn("Exception during Gemini API call: {}. Falling back to mock provider.", e.getMessage());
        }

        return fallbackProvider.getReply(userMessage, context);
    }
}
