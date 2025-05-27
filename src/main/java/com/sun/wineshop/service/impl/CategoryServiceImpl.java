package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.CategoryRequest;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.repository.CategoryRepository;
import com.sun.wineshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        Page<Category> categories = categoryRepository.findAllByDeletedAtIsNull(pageable);

        return categories.map(ToDtoMappers::toCategoryResponse);
    }

    @Override
    public void updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        category.setName(request.name());
        category.setDescription(request.description());
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getProducts().isEmpty()) {
            throw new AppException(ErrorCode.CATEGORY_IN_USE);
        }

        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    public List<Category> findOrCreateByNames(List<String> names) {
        List<Category> result = new ArrayList<>();

        for (String name : names) {
            Category category = categoryRepository.findByName(name)
                    .orElseGet(() -> categoryRepository.save(
                            Category.builder().name(name).build()
                    ));
            result.add(category);
        }

        return result;
    }
}
