package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.CategoryRequest;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.repository.CategoryRepository;
import com.sun.wineshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.sun.wineshop.mapper.ToDtoMappers.toCategoryResponse;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        Category saved = categoryRepository.save(category);
        return toCategoryResponse(saved);
    }

    @Override
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<Category> users = categoryRepository.findAll(pageable);

        return users.map(ToDtoMappers::toCategoryResponse);
    }
}
