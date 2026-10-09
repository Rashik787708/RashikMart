package com.rashik.rashikmart.util;

import com.rashik.rashikmart.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    @Test
    void testSerializationAndDeserialization() {
        Map<String, String> data = Map.of("status", "UP", "db", "UP");
        String json = JsonUtil.toJson(data);
        assertNotNull(json);
        assertTrue(json.contains("\"status\":\"UP\""));

        Map<?, ?> parsed = JsonUtil.fromJson(json, Map.class);
        assertEquals("UP", parsed.get("status"));
        assertEquals("UP", parsed.get("db"));
    }

    @Test
    void testApiResponseSerialization() {
        ApiResponse<String> success = ApiResponse.success("hello");
        String json = JsonUtil.toJson(success);
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"data\":\"hello\""));
        assertTrue(json.contains("\"error\":null"));

        ApiResponse<Void> error = ApiResponse.error("BAD_REQUEST", "Invalid input");
        String errJson = JsonUtil.toJson(error);
        assertTrue(errJson.contains("\"success\":false"));
        assertTrue(errJson.contains("\"code\":\"BAD_REQUEST\""));
    }
}
