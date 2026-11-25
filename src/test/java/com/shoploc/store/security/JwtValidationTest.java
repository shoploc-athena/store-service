package com.shoploc.store.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import com.shoploc.store.config.SecurityConfig;
import com.shoploc.store.config.ShoplocProperties;
import com.shoploc.store.config.ShoplocProperties.JwtProperties;

/** Checks the rules of the decoder: signature, expiration, city, and start-up settings. */
class JwtValidationTest {

    private final JwtDecoder decoder = new SecurityConfig()
            .jwtDecoder(new ShoplocProperties("lille", new JwtProperties(TestTokens.SECRET)));

    private final UUID user = UUID.randomUUID();

    @Test
    void validTokenIsAccepted() {
        String token = TestTokens.valid(user, "MERCHANT");

        assertThat(decoder.decode(token).getSubject()).isEqualTo(user.toString());
    }

    @Test
    void expiredTokenIsRejected() {
        String token = TestTokens.token("lille", TestTokens.SECRET, user, "MERCHANT",
                Instant.now().minus(1, ChronoUnit.HOURS));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = TestTokens.token("lille", "another-secret-another-secret-12345", user, "MERCHANT",
                Instant.now().plus(10, ChronoUnit.MINUTES));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void modifiedTokenIsRejected() {
        String token = TestTokens.valid(user, "MERCHANT");
        // We change the first character of the signature part.
        int lastDot = token.lastIndexOf('.');
        char first = token.charAt(lastDot + 1);
        String modified = token.substring(0, lastDot + 1) + (first == 'A' ? 'B' : 'A') + token.substring(lastDot + 2);

        assertThatThrownBy(() -> decoder.decode(modified)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenFromAnotherCityIsRejected() {
        String token = TestTokens.token("paris", TestTokens.SECRET, user, "MERCHANT",
                Instant.now().plus(10, ChronoUnit.MINUTES));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void serviceRefusesToStartWithAShortSecret() {
        JwtProperties shortSecret = new JwtProperties("too-short");

        assertThatThrownBy(() -> new ShoplocProperties("lille", shortSecret))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void serviceRefusesToStartWithoutCity() {
        JwtProperties jwt = new JwtProperties(TestTokens.SECRET);

        assertThatThrownBy(() -> new ShoplocProperties("", jwt))
                .isInstanceOf(IllegalStateException.class);
    }
}