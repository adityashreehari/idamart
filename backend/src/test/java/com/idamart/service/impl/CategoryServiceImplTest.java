package com.idamart.service.impl;

import com.idamart.TestData;
import com.idamart.dto.CategoryRequest;
import com.idamart.dto.CategoryResponse;
import com.idamart.entity.Category;
import com.idamart.exception.BadRequestException;
import com.idamart.exception.DuplicateResourceException;
import com.idamart.repository.CategoryRepository;
import com.idamart.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("create rejects a duplicate name (409) and saves nothing")
    void create_duplicate() {
        when(categoryRepository.existsByNameIgnoreCase("Figures")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(new CategoryRequest("Figures", null)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("create trims the name before saving")
    void create_trims() {
        when(categoryRepository.existsByNameIgnoreCase("Manga")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(3L);
            return c;
        });

        CategoryResponse response = categoryService.create(new CategoryRequest("  Manga  ", "Books"));

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.name()).isEqualTo("Manga");
    }

    @Test
    @DisplayName("update allows changing only the letter case of the same name")
    void update_sameNameDifferentCase() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(TestData.category(1, "Figures")));

        CategoryResponse response = categoryService.update(1L, new CategoryRequest("FIGURES", "new"));

        assertThat(response.name()).isEqualTo("FIGURES");
        verify(categoryRepository, never()).existsByNameIgnoreCase(anyString());
    }

    @Test
    @DisplayName("delete is blocked while the category still has products")
    void delete_withProducts() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(TestData.category(1, "Figures")));
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("still has products");
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete removes an empty category")
    void delete_empty() {
        Category empty = TestData.category(2, "Posters");
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(empty));
        when(productRepository.existsByCategoryId(2L)).thenReturn(false);

        categoryService.delete(2L);

        verify(categoryRepository).delete(empty);
    }
}
