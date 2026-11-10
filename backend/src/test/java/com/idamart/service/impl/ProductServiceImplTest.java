package com.idamart.service.impl;

import com.idamart.TestData;
import com.idamart.dto.ProductRequest;
import com.idamart.dto.ProductResponse;
import com.idamart.entity.Product;
import com.idamart.exception.ResourceNotFoundException;
import com.idamart.repository.CategoryRepository;
import com.idamart.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests: the service is tested ALONE.
 * The repositories are Mockito "fakes" (@Mock), so no database is needed
 * and every test runs in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequest validRequest() {
        return new ProductRequest("  Goku Figure  ", "desc", " Dragon Ball ",
                new BigDecimal("199.00"), 10, "", 1L);
    }

    @Test
    @DisplayName("getById returns an active product")
    void getById_active() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(TestData.product(5, "Goku Figure", "199.00", 10)));

        ProductResponse response = productService.getById(5L);

        assertThat(response.name()).isEqualTo("Goku Figure");
        assertThat(response.categoryName()).isEqualTo("Figures");
        assertThat(response.inStock()).isTrue();
    }

    @Test
    @DisplayName("getById hides soft-deleted products (404)")
    void getById_inactive() {
        Product hidden = TestData.product(5, "Old Figure", "10.00", 1);
        hidden.setActive(false);
        when(productRepository.findById(5L)).thenReturn(Optional.of(hidden));

        assertThatThrownBy(() -> productService.getById(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getById throws 404 for an unknown id")
    void getById_missing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create trims text, links the category and saves")
    void create_valid() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(TestData.category(1, "Figures")));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductResponse response = productService.create(validRequest());

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Goku Figure");
        assertThat(response.animeSeries()).isEqualTo("Dragon Ball");
        assertThat(response.categoryName()).isEqualTo("Figures");
    }

    @Test
    @DisplayName("create fails with 404 if the category doesn't exist, and saves nothing")
    void create_unknownCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete is a soft delete: the product is hidden, not removed")
    void delete_soft() {
        Product product = TestData.product(5, "Goku Figure", "199.00", 10);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        productService.delete(5L);

        assertThat(product.getActive()).isFalse();
        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("search caps the page size at 50 and treats blank keywords as 'no filter'")
    void search_guardsInput() {
        when(productRepository.search(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        productService.search("   ", null, "", 0, 1000, "price_asc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).search(isNull(), isNull(), isNull(), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
        assertThat(captor.getValue().getSort().getOrderFor("price")).isNotNull();
    }
}
