package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.ProductRequest;
import com.sun.wineshop.dto.request.ProductSearchRequest;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.mapper.ToEntityMappers;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.repository.CategoryRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ToDtoMappers::toProductResponse);
    }

    @Override
    public Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        return productRepository.searchProducts(
                request.name(),
                request.minPrice(),
                request.maxPrice(),
                request.minAlcoholPercentage(),
                request.maxAlcoholPercentage(),
                request.categoryId(),
                pageable
        ).map(ToDtoMappers::toProductResponse);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(ToDtoMappers::toProductResponse)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        List<Long> categoryIds = request.categoryIds();
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        if (categories.size() != categoryIds.size()) {
            throw new AppException(
                    ErrorCode.SOME_CATEGORY_NOT_FOUND
            );
        }
        Product product = ToEntityMappers.toProduct(request, categories);
        Product saved = productRepository.save(product);

        return ToDtoMappers.toProductResponse(saved);
    }
}
