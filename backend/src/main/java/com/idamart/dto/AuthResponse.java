package com.idamart.dto;

/**
 * Returned after register / login.
 * The frontend stores the token and sends it back on every request:
 *   Authorization: Bearer <token>
 */
public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {
}
