package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.CategoryRequest;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    Page<CategoryResponse> getAllCategories(Pageable pageable);
    void updateCategory(Long id, CategoryRequest request);
    void deleteCategoryById(Long id);
    List<Category> findOrCreateByNames(List<String> names);
}
