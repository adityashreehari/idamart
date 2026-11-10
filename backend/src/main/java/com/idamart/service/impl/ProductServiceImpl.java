package com.idamart.service.impl;

import com.idamart.dto.PageResponse;
import com.idamart.dto.ProductRequest;
import com.idamart.dto.ProductResponse;
import com.idamart.entity.Category;
import com.idamart.entity.Product;
import com.idamart.exception.ResourceNotFoundException;
import com.idamart.mapper.EntityMapper;
import com.idamart.repository.CategoryRepository;
import com.idamart.repository.ProductRepository;
import com.idamart.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, Long categoryId, String anime,
                                                int page, int size, String sort) {
        // Guard against silly values like page=-1 or size=10000
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, toSort(sort));

        Page<ProductResponse> result = productRepository
                .search(blankToNull(keyword), categoryId, blankToNull(anime), pageable)
                .map(EntityMapper::toProductResponse);

        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::getActive)               // hidden products look "not found"
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return EntityMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAnimeSeries() {
        return productRepository.findDistinctAnimeSeries();
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        product.setActive(true);
        return EntityMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findActiveOrThrow(id);
        applyRequest(product, request);
        return EntityMapper.toProductResponse(product);
    }

    /**
     * Soft delete: we hide the product instead of removing the row,
     * because old orders still point to it (foreign key in order_items).
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findActiveOrThrow(id);
        product.setActive(false);
    }

    // ---------------- helpers ----------------

    private void applyRequest(Product product, ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setAnimeSeries(request.animeSeries().trim());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        product.setCategory(category);
    }

    private Product findActiveOrThrow(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    /** Only allow known sort options, so clients can't sort by internal fields. */
    private Sort toSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");   // "newest" and anything unknown
        };
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
