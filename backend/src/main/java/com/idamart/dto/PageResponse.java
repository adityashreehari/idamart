package com.idamart.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A clean, stable JSON shape for paginated lists.
 * Generic type T = demonstrates Java generics.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
