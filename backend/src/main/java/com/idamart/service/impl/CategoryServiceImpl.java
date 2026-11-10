package com.idamart.service.impl;

import com.idamart.dto.CategoryRequest;
import com.idamart.dto.CategoryResponse;
import com.idamart.entity.Category;
import com.idamart.exception.BadRequestException;
import com.idamart.exception.DuplicateResourceException;
import com.idamart.exception.ResourceNotFoundException;
import com.idamart.mapper.EntityMapper;
import com.idamart.repository.CategoryRepository;
import com.idamart.repository.ProductRepository;
import com.idamart.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business rules for categories.
 * @RequiredArgsConstructor (Lombok) creates a constructor for the final fields,
 * and Spring uses it to inject the repositories: constructor-based Dependency Injection.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll(Sort.by("name"))
                .stream()
                .map(EntityMapper::toCategoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return EntityMapper.toCategoryResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category '" + name + "' already exists");
        }
        Category category = Category.builder()
                .name(name)
                .description(request.description())
                .build();
        return EntityMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOrThrow(id);
        String newName = request.name().trim();

        // Only check for duplicates if the name actually changed
        if (!category.getName().equalsIgnoreCase(newName)
                && categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new DuplicateResourceException("Category '" + newName + "' already exists");
        }
        category.setName(newName);
        category.setDescription(request.description());
        // No save() needed: inside a transaction, Hibernate detects the change ("dirty checking")
        return EntityMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findOrThrow(id);
        if (productRepository.existsByCategoryId(id)) {
            throw new BadRequestException("Cannot delete category '" + category.getName()
                    + "' because it still has products");
        }
        categoryRepository.delete(category);
    }

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}
