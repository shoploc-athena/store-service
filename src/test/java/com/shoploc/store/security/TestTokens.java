package com.shoploc.store.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/** Test helper: builds tokens like the user-service does (same claims, HS256). */
final class TestTokens {

    static final String SECRET = "test-secret-test-secret-test-secret-test";

    private TestTokens() {
    }

    static String token(String city, String secret, UUID subject, String role, Instant expiresAt) {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject.toString())
                .claim("city", city)
                .claim("role", role)
                .claim("permissions", List.of())
                .issuedAt(expiresAt.minus(15, ChronoUnit.MINUTES))
                .expiresAt(expiresAt)
                .build();

        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    /** A valid token for the city "lille", valid for 10 more minutes. */
    static String valid(UUID subject, String role) {
        return token("lille", SECRET, subject, role, Instant.now().plus(10, ChronoUnit.MINUTES));
    }
}
