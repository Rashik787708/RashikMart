package com.rashik.rashikmart.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConfig {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();

        String dbPath = System.getProperty("catalina.base")
                + "\\data\\rashikmart";

        System.out.println("H2 Database: " + dbPath);

        config.setJdbcUrl("jdbc:h2:file:" + dbPath);
        config.setUsername("sa");
        config.setPassword("WE");
        config.setDriverClassName("org.h2.Driver");

        dataSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}