package com.idamart.service;

import com.idamart.dto.CategoryRequest;
import com.idamart.dto.CategoryResponse;

import java.util.List;

/**
 * The contract (what) is separated from the implementation (how).
 * Controllers depend on this interface, not the class: loose coupling,
 * and easy to mock in unit tests (Session 5).
 */
public interface CategoryService {

    List<CategoryResponse> getAll();

    CategoryResponse getById(Long id);

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
