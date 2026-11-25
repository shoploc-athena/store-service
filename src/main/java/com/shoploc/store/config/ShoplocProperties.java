package com.shoploc.store.config;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings read from application.properties (shoploc.*).
 * The service refuses to start if the city or the JWT secret is missing or too short.
 */
@ConfigurationProperties(prefix = "shoploc")
public record ShoplocProperties(String city, JwtProperties jwt) {

    private static final int MIN_SECRET_BYTES = 32;

    public record JwtProperties(String secret) {
    }

    public ShoplocProperties {
        if (city == null || city.isBlank()) {
            throw new IllegalStateException("CITY must be set");
        }
        if (jwt == null || jwt.secret() == null
                || jwt.secret().getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET must be set and contain at least 32 bytes");
        }
    }
}
