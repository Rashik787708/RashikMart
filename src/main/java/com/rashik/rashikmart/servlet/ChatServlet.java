package com.rashik.rashikmart.servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rashik.rashikmart.ai.ChatService;
import com.rashik.rashikmart.dto.ApiResponse;
import com.rashik.rashikmart.util.CsrfUtil;
import com.rashik.rashikmart.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Chatbot API endpoint per spec §11, §17.
 * Mapped to POST /api/v1/chat and alias /api/chat.
 */
@WebServlet(urlPatterns = {"/api/v1/chat", "/api/chat"})
public class ChatServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ChatServlet.class);

    private ChatService chatService;

    public ChatServlet() {
        this.chatService = new ChatService();
    }

    public ChatServlet(ChatService chatService) {
        this.chatService = chatService != null ? chatService : new ChatService();
    }

    @Override
    public void init() throws ServletException {
        if (this.chatService == null) {
            this.chatService = new ChatService();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(true);
        String csrfToken = CsrfUtil.getOrCreateToken(session);

        Map<String, String> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("csrfToken", csrfToken);

        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.success(data));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        CsrfUtil.getOrCreateToken(session);

        // 1. CSRF Verification
        if (!CsrfUtil.isValid(request)) {
            logger.warn("CSRF check failed for chat request from session: {}", session.getId());
            JsonUtil.writeJson(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    ApiResponse.error("FORBIDDEN", "Invalid or missing CSRF token")
            );
            return;
        }

        // 2. Extract message from parameter or JSON body
        String message = request.getParameter("message");
        if (message == null || message.isBlank()) {
            try {
                String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                if (!body.isBlank()) {
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    if (json.has("message") && !json.get("message").isJsonNull()) {
                        message = json.get("message").getAsString();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 3. Process via ChatService
        try {
            String reply = chatService.processMessage(message, session);
            Map<String, String> responseData = new LinkedHashMap<>();
            responseData.put("reply", reply);

            JsonUtil.writeJson(
                    response,
                    HttpServletResponse.SC_OK,
                    ApiResponse.success(responseData)
            );
        } catch (IllegalArgumentException e) {
            logger.info("Chat validation error: {}", e.getMessage());
            JsonUtil.writeJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("BAD_REQUEST", e.getMessage())
            );
        } catch (ChatService.RateLimitExceededException e) {
            logger.warn("Chat rate limit exceeded: {}", e.getMessage());
            JsonUtil.writeJson(
                    response,
                    429, // SC_TOO_MANY_REQUESTS
                    ApiResponse.error("RATE_LIMIT_EXCEEDED", e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Chat error: {}", e.getMessage(), e);
            JsonUtil.writeJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred")
            );
        }
    }
}
