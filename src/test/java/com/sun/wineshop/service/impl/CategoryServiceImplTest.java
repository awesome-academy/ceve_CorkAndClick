package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.CategoryRequest;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Red Wine")
                .description("Dry red wine")
                .deletedAt(null)
                .build();
    }

    @Test
    void createCategory_shouldReturnResponse() {
        CategoryRequest request = new CategoryRequest("Red Wine", "Dry red wine");

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(request);

        assertEquals("Red Wine", response.name());
        assertEquals("Dry red wine", response.description());
    }

    @Test
    void getAllCategories_shouldReturnPagedResult() {
        Page<Category> page = new PageImpl<>(List.of(category));
        when(categoryRepository.findAllByDeletedAtIsNull(any(Pageable.class))).thenReturn(page);

        Page<CategoryResponse> result = categoryService.getAllCategories(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Red Wine", result.getContent().getFirst().name());
    }

    @Test
    void updateCategory_whenCategoryExists_shouldUpdate() {
        CategoryRequest request = new CategoryRequest("Updated", "Updated Desc");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.updateCategory(1L, request);

        verify(categoryRepository).save(category);
        assertEquals("Updated", category.getName());
        assertEquals("Updated Desc", category.getDescription());
    }

    @Test
    void updateCategory_whenNotFound_shouldThrow() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        CategoryRequest request = new CategoryRequest("Any", "Any");

        AppException ex = assertThrows(AppException.class,
                () -> categoryService.updateCategory(1L, request));

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void deleteCategory_whenCategoryHasProducts_shouldThrow() {
        category.setProducts(List.of(new com.sun.wineshop.model.entity.Product()));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        AppException ex = assertThrows(AppException.class,
                () -> categoryService.deleteCategoryById(1L));

        assertEquals(ErrorCode.CATEGORY_IN_USE, ex.getErrorCode());
    }

    @Test
    void deleteCategory_whenValid_shouldSetDeletedAt() {
        category.setProducts(Collections.emptyList());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategoryById(1L);

        assertNotNull(category.getDeletedAt());
        verify(categoryRepository).save(category);
    }

    @Test
    void deleteCategory_whenNotFound_shouldThrow() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> categoryService.deleteCategoryById(1L));

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, ex.getErrorCode());
    }
}
