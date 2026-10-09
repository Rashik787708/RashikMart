package com.rashik.rashikmart.ai;

import com.rashik.rashikmart.dao.ProductDAO;
import com.rashik.rashikmart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Chatbot business logic service (spec §11, §17).
 * Handles input validation, per-session rate limiting, per-session response caching,
 * real catalog context extraction, and resilient fallback execution.
 */
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    public static final int MAX_MESSAGE_LENGTH = 500;
    public static final int RATE_LIMIT_MAX_REQUESTS = 10;
    public static final long RATE_LIMIT_WINDOW_MS = 60_000L; // 1 minute

    public static final String DEGRADED_FALLBACK_REPLY =
            "Our assistant is unavailable right now. You can continue shopping on the Marketplace or contact support@rashikmart.com.";

    private static final String SESSION_CHAT_CACHE = "CHAT_CACHE";
    private static final String SESSION_CHAT_TIMESTAMPS = "CHAT_TIMESTAMPS";

    private final ChatProvider provider;
    private final ProductDAO productDAO;

    public ChatService() {
        this(ChatProviderFactory.getProvider(), new ProductDAO());
    }

    public ChatService(ChatProvider provider, ProductDAO productDAO) {
        this.provider = provider != null ? provider : ChatProviderFactory.getProvider();
        this.productDAO = productDAO;
    }

    /**
     * Processes a user chat message with session tracking.
     */
    public String processMessage(String message, HttpSession session) {
        // 1. Input Validation
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        String trimmed = message.trim();
        if (trimmed.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message exceeds maximum length of " + MAX_MESSAGE_LENGTH + " characters");
        }

        // 2. Rate Limiting per session
        if (session != null) {
            checkRateLimit(session);
        }

        // 3. In-memory per-session cache check
        String cacheKey = trimmed.toLowerCase(Locale.ROOT);
        if (session != null) {
            Map<String, String> cache = getSessionCache(session);
            if (cache.containsKey(cacheKey)) {
                logger.debug("Returning cached chat reply for query: {}", cacheKey);
                return cache.get(cacheKey);
            }
        }

        // 4. Extract catalog context (zero PII, generic product info only)
        String context = buildProductContext(trimmed);

        // 5. Provider execution with failover
        String reply;
        try {
            reply = provider.getReply(trimmed, context);
            if (reply == null || reply.isBlank()) {
                reply = DEGRADED_FALLBACK_REPLY;
            }
        } catch (Exception e) {
            logger.warn("ChatProvider encountered an error: {}. Serving degraded reply.", e.getMessage());
            reply = DEGRADED_FALLBACK_REPLY;
        }

        // 6. Store in cache
        if (session != null) {
            Map<String, String> cache = getSessionCache(session);
            cache.put(cacheKey, reply);
        }

        return reply;
    }

    @SuppressWarnings("unchecked")
    private void checkRateLimit(HttpSession session) {
        synchronized (session) {
            List<Long> timestamps = (List<Long>) session.getAttribute(SESSION_CHAT_TIMESTAMPS);
            if (timestamps == null) {
                timestamps = new ArrayList<>();
                session.setAttribute(SESSION_CHAT_TIMESTAMPS, timestamps);
            }

            long now = System.currentTimeMillis();
            // Evict timestamps older than 1 minute
            timestamps.removeIf(ts -> now - ts > RATE_LIMIT_WINDOW_MS);

            if (timestamps.size() >= RATE_LIMIT_MAX_REQUESTS) {
                logger.warn("Rate limit exceeded for session: {}", session.getId());
                throw new RateLimitExceededException("Rate limit exceeded. Maximum 10 messages per minute allowed.");
            }

            timestamps.add(now);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getSessionCache(HttpSession session) {
        Map<String, String> cache = (Map<String, String>) session.getAttribute(SESSION_CHAT_CACHE);
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            session.setAttribute(SESSION_CHAT_CACHE, cache);
        }
        return cache;
    }

    private String buildProductContext(String userMessage) {
        if (productDAO == null) {
            return "";
        }
        try {
            List<Product> available = productDAO.findAllAvailable();
            if (available == null || available.isEmpty()) {
                return "";
            }

            String lowerQuery = userMessage.toLowerCase(Locale.ROOT);
            List<Product> matched = available.stream()
                    .filter(p -> lowerQuery.contains(p.getName().toLowerCase(Locale.ROOT))
                            || lowerQuery.contains(p.getCategory().toLowerCase(Locale.ROOT)))
                    .limit(3)
                    .collect(Collectors.toList());

            if (matched.isEmpty()) {
                // If no exact match, pass top 3 available items as sample catalog
                matched = available.stream().limit(3).collect(Collectors.toList());
            }

            return matched.stream()
                    .map(p -> p.getName() + " (" + p.getCategory() + " - $" + p.getPrice() + ")")
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            logger.warn("Failed to fetch product context: {}", e.getMessage());
            return "";
        }
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
