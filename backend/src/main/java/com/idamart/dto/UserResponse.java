package com.idamart.dto;

/** Public view of a user. Note: no password field, ever. */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}
