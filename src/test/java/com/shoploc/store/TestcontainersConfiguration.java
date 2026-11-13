package com.shoploc.store;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Starts one disposable PostgreSQL container for the whole test run and
 * connects Spring to it automatically.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    // "static" + start() in a static block: the container is started only once
    // and shared by every test class (faster than one container per test).
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    public static PostgreSQLContainer postgres() {
        return POSTGRES;
    }

    // @ServiceConnection tells Spring Boot: "use this container as the datasource".
    // No URL, user or password to write: Spring reads them from the container.
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return POSTGRES;
    }
}
