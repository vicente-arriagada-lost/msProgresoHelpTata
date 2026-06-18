package com.Progreso.ms.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        String afterLastSlash = url.substring(url.lastIndexOf('/') + 1);
        String dbName = afterLastSlash.contains("?")
                ? afterLastSlash.substring(0, afterLastSlash.indexOf('?'))
                : afterLastSlash;
        String adminUrl = url.substring(0, url.lastIndexOf('/')) + "/postgres";

        try (Connection conn = DriverManager.getConnection(adminUrl, username, password)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM pg_database WHERE datname = ?")) {
                ps.setString(1, dbName);
                if (!ps.executeQuery().next()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("CREATE DATABASE \"" + dbName + "\"");
                        System.out.println("[HelpTata] Base de datos creada: " + dbName);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[HelpTata] Advertencia al inicializar BD '" + dbName + "': " + e.getMessage());
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }
}
