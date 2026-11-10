package com.idamart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * What the client SENDS to create/update a category.
 * A Java 17 record: immutable, with constructor, getters, equals/hashCode built in.
 */
public record CategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 60, message = "Category name must be at most 60 characters")
        String name,

        @Size(max = 255, message = "Description must be at most 255 characters")
        String description
) {
}
