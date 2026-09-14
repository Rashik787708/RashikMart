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
}