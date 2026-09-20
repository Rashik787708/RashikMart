package com.rashik.rashikmart.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConfig {

    private static final HikariDataSource dataSource;

    static {

        HikariConfig config = new HikariConfig();

        String jdbcUrl = System.getenv("DB_URL") != null 
                ? System.getenv("DB_URL") 
                : System.getProperty("db.url", "jdbc:h2:./data/rashikmart");
        String username = System.getenv("DB_USER") != null 
                ? System.getenv("DB_USER") 
                : System.getProperty("db.user", "sa");
        String password = System.getenv("DB_PASSWORD") != null 
                ? System.getenv("DB_PASSWORD") 
                : System.getProperty("db.password", "WE");

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.h2.Driver");

        dataSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Resolves the directory used to store uploaded product images.
     *
     * Resolution order (first non-blank value wins):
     * 1. Environment variable RASHIKMART_UPLOAD_DIR
     * 2. System property rashikmart.upload.dir
     * 3. Default: ./data/product-images (relative to the working directory
     *    of the application server process, keeping the dev experience unchanged)
     *
     * The configured directory is expected to live OUTSIDE the exploded WAR so
     * that product photos survive re-deploys. The returned path is never the
     * servlet context's real path; it must be paired with ProductImageServlet
     * which serves the files back to the browser.
     */
    public static String getUploadDir() {

        String fromEnv = System.getenv("RASHIKMART_UPLOAD_DIR");
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }

        String fromProp =
                System.getProperty(
                        "rashikmart.upload.dir"
                );

        if (fromProp != null
                && !fromProp.trim().isEmpty()) {

            return fromProp.trim();
        }

        return "./data/product-images";
    }
}