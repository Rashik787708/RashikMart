package com.rashik.rashikmart.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Locale;

public class DatabaseConfig {

    private static final String DB_TYPE_H2 = "h2";
    private static final String DB_TYPE_POSTGRES = "postgres";

    private static final String activeDbType;
    private static final HikariDataSource dataSource;

    static {

        activeDbType = resolveDbType();

        HikariConfig config = new HikariConfig();

        if (DB_TYPE_POSTGRES.equals(activeDbType)) {
            configurePostgres(config);
        } else {
            configureH2(config);
        }

        dataSource = new HikariDataSource(config);

        System.out.println("=================================");
        System.out.println("Database type: " + activeDbType);
        System.out.println("Database connected successfully");
        System.out.println("=================================");
    }

    /**
     * Returns the active backend: {@code "h2"} (default / local) or
     * {@code "postgres"} (Render).
     *
     * <p>Resolution order:
     * <ol>
     *     <li>System property {@code db.type} (used by the Maven test suite
     *         to guarantee tests always run against in-memory H2).</li>
     *     <li>Environment variable {@code DB_TYPE}.</li>
     *     <li>Default {@code "h2"} so local development keeps working with
     *         the existing H2 database out of the box.</li>
     * </ol>
     */
    private static String resolveDbType() {

        String fromSysProp = System.getProperty("db.type");
        if (fromSysProp != null && !fromSysProp.trim().isEmpty()) {
            return fromSysProp.trim().toLowerCase(Locale.ROOT);
        }

        String fromEnv = System.getenv("DB_TYPE");
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim().toLowerCase(Locale.ROOT);
        }

        return DB_TYPE_H2;
    }

    /**
     * LOCAL development configuration (unchanged): a file-based H2 database
     * stored under ./data so the existing local database/data keeps working.
     *
     * <p>Overridable via {@code DB_URL} / {@code DB_USER} / {@code DB_PASSWORD}
     * environment variables, falling back to {@code db.url} / {@code db.user} /
     * {@code db.password} system properties and then the dev defaults.
     */
    private static void configureH2(HikariConfig config) {

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
    }

    /**
     * PRODUCTION (Render) configuration: PostgreSQL, connected through the
     * {@code DATABASE_URL} environment variable supplied by Render.
     *
     * <p>Accepts both {@code postgresql://user:pass@host/db?...} and
     * {@code jdbc:postgresql://user:pass@host/db?...}. Credentials embedded in
     * the URL are parsed out (percent-decoded, never logged) and handed to
     * HikariCP as separate settings; any SSL/query options such as
     * {@code sslmode=require} are preserved on the JDBC URL.
     */
    private static void configurePostgres(HikariConfig config) {

        String rawUrl = System.getenv("DATABASE_URL");

        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            throw new IllegalStateException(
                    "DB_TYPE=postgres requires the DATABASE_URL environment variable "
                            + "(Render internal PostgreSQL database URL)."
            );
        }

        PostgresConnectionInfo info =
                parsePostgresUrl(rawUrl.trim());

        config.setJdbcUrl(info.jdbcUrl);
        config.setDriverClassName("org.postgresql.Driver");

        if (info.username != null && !info.username.isEmpty()) {
            config.setUsername(info.username);
        } else {
            String username = System.getenv("DB_USER") != null
                    ? System.getenv("DB_USER")
                    : System.getProperty("db.user", "postgres");
            config.setUsername(username);
        }

        if (info.password != null && !info.password.isEmpty()) {
            config.setPassword(info.password);
        } else {
            String password = System.getenv("DB_PASSWORD") != null
                    ? System.getenv("DB_PASSWORD")
                    : System.getProperty("db.password", "");
            config.setPassword(password);
        }
    }

    private static PostgresConnectionInfo parsePostgresUrl(String rawUrl) {

        String rest;

        if (rawUrl.startsWith("jdbc:postgresql://")) {
            rest = rawUrl.substring("jdbc:postgresql://".length());
        } else if (rawUrl.startsWith("postgresql://")) {
            rest = rawUrl.substring("postgresql://".length());
        } else if (rawUrl.startsWith("postgres://")) {
            rest = rawUrl.substring("postgres://".length());
        } else {
            throw new IllegalArgumentException(
                    "DATABASE_URL must start with 'postgresql://' or 'jdbc:postgresql://'."
            );
        }

        if (rest.isEmpty()) {
            throw new IllegalArgumentException(
                    "DATABASE_URL is missing the host/database part."
            );
        }

        String username = null;
        String password = null;

        int at = rest.lastIndexOf('@');
        if (at >= 0) {
            String userInfo = rest.substring(0, at);
            rest = rest.substring(at + 1);

            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                username = percentDecode(userInfo.substring(0, colon));
                password = percentDecode(userInfo.substring(colon + 1));
            } else {
                username = percentDecode(userInfo);
            }
        }

        String jdbcUrl = "jdbc:postgresql://" + rest;

        return new PostgresConnectionInfo(jdbcUrl, username, password);
    }

    /**
     * Decodes percent-encoded URL user-info fragments without treating '+'
     * as a space (unlike {@code URLDecoder}), so passwords containing '+'
     * survive round-tripping.
     */
    private static String percentDecode(String value) {

        if (value.indexOf('%') < 0) {
            return value;
        }

        StringBuilder decoded = new StringBuilder(value.length());

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '%' && i + 2 < value.length()) {
                int hi = Character.digit(value.charAt(i + 1), 16);
                int lo = Character.digit(value.charAt(i + 2), 16);
                if (hi >= 0 && lo >= 0) {
                    decoded.append((char) ((hi << 4) + lo));
                    i += 2;
                    continue;
                }
            }
            decoded.append(c);
        }

        return decoded.toString();
    }

    public static String getDbType() {
        return activeDbType;
    }

    public static boolean isPostgres() {
        return DB_TYPE_POSTGRES.equals(activeDbType);
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

    private static final class PostgresConnectionInfo {
        private final String jdbcUrl;
        private final String username;
        private final String password;

        private PostgresConnectionInfo(
                String jdbcUrl,
                String username,
                String password) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
        }
    }
}