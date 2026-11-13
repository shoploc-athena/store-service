package com.shoploc.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Test helper: creates one database per city in the shared PostgreSQL container
 * and starts one application instance per city, like the docker-compose does.
 */
public final class CityDatabases {

    private CityDatabases() {
    }

    private static String databaseName(String city) {
        return "store_" + city;
    }

    /** Creates an empty database named store_<city>. */
    public static void create(String city) {
        PostgreSQLContainer postgres = TestcontainersConfiguration.postgres();
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + databaseName(city));
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot create database for city " + city, e);
        }
    }

    /** Starts the application connected to the database of the given city. */
    public static ConfigurableApplicationContext startApplication(String city) {
        PostgreSQLContainer postgres = TestcontainersConfiguration.postgres();
        String url = "jdbc:postgresql://" + postgres.getHost() + ":"
                + postgres.getMappedPort(5432) + "/" + databaseName(city);
        return new SpringApplicationBuilder(StoreServiceApplication.class)
                .web(WebApplicationType.NONE)
                .run(
                        "--spring.datasource.url=" + url,
                        "--spring.datasource.username=" + postgres.getUsername(),
                        "--spring.datasource.password=" + postgres.getPassword(),
                        "--spring.jpa.hibernate.ddl-auto=create-drop");
    }
}
