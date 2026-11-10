package com.idamart.dto;

/** What the API RETURNS for a category. */
public record CategoryResponse(
        Long id,
        String name,
        String description
) {
}
