package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.CategoryRequest;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryRequest categoryRequest;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryRequest = new CategoryRequest("Wine", "Alcoholic beverage");

        category = Category.builder()
                .id(1L)
                .name("Wine")
                .description("Alcoholic beverage")
                .products(new ArrayList<>())
                .build();
    }

    @Test
    void createCategory_shouldReturnCategoryResponse() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(categoryRequest);

        assertEquals("Wine", response.name());
        assertEquals("Alcoholic beverage", response.description());
    }

    @Test
    void getAllCategories_shouldReturnPagedCategoryResponses() {
        Page<Category> page = new PageImpl<>(List.of(category));
        when(categoryRepository.findAllByDeletedAtIsNull(any())).thenReturn(page);

        Page<CategoryResponse> result = categoryService.getAllCategories(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Wine", result.getContent().getFirst().name());
    }

    @Test
    void updateCategory_whenCategoryExists_shouldUpdateSuccessfully() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryRequest updatedRequest = new CategoryRequest("Red Wine", "Updated desc");
        categoryService.updateCategory(1L, updatedRequest);

        assertEquals("Red Wine", category.getName());
        assertEquals("Updated desc", category.getDescription());
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_whenCategoryNotFound_shouldThrowAppException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> categoryService.updateCategory(99L, categoryRequest));

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void deleteCategory_whenInUse_shouldThrowAppException() {
        category.setProducts(List.of(new Product()));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        AppException ex = assertThrows(AppException.class,
                () -> categoryService.deleteCategoryById(1L));

        assertEquals(ErrorCode.CATEGORY_IN_USE, ex.getErrorCode());
    }

    @Test
    void deleteCategory_whenNotInUse_shouldSetDeletedAt() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategoryById(1L);

        assertNotNull(category.getDeletedAt());
        verify(categoryRepository).save(category);
    }

    @Test
    void deleteCategory_whenNotFound_shouldThrowAppException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> categoryService.deleteCategoryById(999L));

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, ex.getErrorCode());
    }
}
