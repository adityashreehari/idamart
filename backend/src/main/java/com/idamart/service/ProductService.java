package com.idamart.service;

import com.idamart.dto.PageResponse;
import com.idamart.dto.ProductRequest;
import com.idamart.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    /**
     * Paginated search. Every filter is optional.
     *
     * @param sort one of: newest, price_asc, price_desc, name
     */
    PageResponse<ProductResponse> search(String keyword, Long categoryId, String anime,
                                         int page, int size, String sort);

    ProductResponse getById(Long id);

    List<String> getAnimeSeries();

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
