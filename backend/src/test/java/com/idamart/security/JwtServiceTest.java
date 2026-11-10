package com.idamart.security;

import com.idamart.TestData;
import com.idamart.entity.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Uses the REAL JwtService (no mocks) with a test-only secret. */
class JwtServiceTest {

    // 48 random bytes, Base64 encoded. Only used in tests.
    private static final String TEST_SECRET =
            "q0VnZk7Yw3oW2b6m8N1xS4tR5uE9iP0aL7kJ3hG2fD1sA6zX8cV4bN2mQ5wE7rT9";

    private final AppUserPrincipal customer = new AppUserPrincipal(TestData.user(7, Role.CUSTOMER));

    @Test
    @DisplayName("A token we create can be read back to the same email")
    void roundTrip() {
        JwtService jwt = new JwtService(TEST_SECRET, 60_000);

        String token = jwt.generateToken(customer);

        assertThat(token.split("\\.")).hasSize(3);                  // header.payload.signature
        assertThat(jwt.extractEmail(token)).isEqualTo("user7@test.com");
    }

    @Test
    @DisplayName("A token that was edited is rejected (signature no longer matches)")
    void tamperedToken() {
        JwtService jwt = new JwtService(TEST_SECRET, 60_000);
        String token = jwt.generateToken(customer);
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        assertThatThrownBy(() -> jwt.extractEmail(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("An expired token is rejected")
    void expiredToken() {
        JwtService jwt = new JwtService(TEST_SECRET, -1_000);        // already expired when created

        String token = jwt.generateToken(customer);

        assertThatThrownBy(() -> jwt.extractEmail(token)).isInstanceOf(ExpiredJwtException.class);
    }
}
