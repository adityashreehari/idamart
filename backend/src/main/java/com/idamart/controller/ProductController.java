package com.idamart.controller;

import com.idamart.dto.PageResponse;
import com.idamart.dto.ProductRequest;
import com.idamart.dto.ProductResponse;
import com.idamart.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for products.
 * Security (see SecurityConfig): GET is public, POST/PUT/DELETE are ADMIN only.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products
     * GET /api/products?keyword=goku&categoryId=1&anime=Naruto&page=0&size=12&sort=price_asc
     */
    @GetMapping
    public PageResponse<ProductResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String anime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return productService.search(keyword, categoryId, anime, page, size, sort);
    }

    // GET /api/products/5
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    // GET /api/products/anime-series  -> ["Demon Slayer", "Naruto", "One Piece"]
    @GetMapping("/anime-series")
    public List<String> getAnimeSeries() {
        return productService.getAnimeSeries();
    }

    // POST /api/products
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    // PUT /api/products/5
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    // DELETE /api/products/5  (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
