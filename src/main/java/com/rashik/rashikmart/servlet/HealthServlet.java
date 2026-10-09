package com.rashik.rashikmart.servlet;

import com.rashik.rashikmart.config.DatabaseConfig;
import com.rashik.rashikmart.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check endpoint per spec §13, §18.
 * GET /api/v1/health -> { "status":"UP", "db":"UP" } (200)
 * On DB probe failure: { "status":"DOWN", "db":"DOWN" } (503)
 */
@WebServlet(urlPatterns = {"/api/v1/health", "/api/health"})
public class HealthServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(HealthServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean dbUp = false;
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                dbUp = true;
            }
        } catch (Exception e) {
            logger.error("Health check database probe failed: {}", e.getMessage());
        }

        Map<String, String> health = new LinkedHashMap<>();
        if (dbUp) {
            health.put("status", "UP");
            health.put("db", "UP");
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, health);
        } else {
            health.put("status", "DOWN");
            health.put("db", "DOWN");
            JsonUtil.writeJson(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, health);
        }
    }
}
