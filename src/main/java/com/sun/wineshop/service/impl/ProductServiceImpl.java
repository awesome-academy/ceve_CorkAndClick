package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.CreateProductRequest;
import com.sun.wineshop.dto.request.ProductSearchRequest;
import com.sun.wineshop.dto.request.UpdateProductRequest;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.mapper.ToEntityMappers;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.repository.CategoryRepository;
import com.sun.wineshop.repository.OrderItemRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAllByDeletedAtIsNull(pageable)
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
                request.categoryIds(),
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
    public ProductResponse createProduct(CreateProductRequest request) {
        List<Long> categoryIds = request.categoryIds();
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        if (categories.size() != categoryIds.size()) {
            throw new AppException(
                    ErrorCode.CATEGORY_NOT_FOUND
            );
        }
        Product product = ToEntityMappers.toProduct(request, categories);
        Product saved = productRepository.save(product);

        return ToDtoMappers.toProductResponse(saved);
    }

    @Override
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.categoryIds() != null) {
            List<Long> categoryIdsRequest = request.categoryIds();
            List<Category> categories = categoryRepository.findAllById(categoryIdsRequest);

            if (categoryIdsRequest.size() != categories.size()) {
                List<Long> foundIds = categories.stream().map(Category::getId).toList();
                List<Long> missingIds = categoryIdsRequest.stream()
                        .filter(idCategory -> !foundIds.contains(idCategory))
                        .toList();

                String idsString = missingIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));

                throw new AppException(ErrorCode.CATEGORY_SOME_NOT_FOUND, idsString);
            }

            product.setCategories(categories);
        }

        product.setName(request.name());
        product.setDescription(request.description());
        product.setImageUrl(request.imageUrl());
        product.setPrice(request.price());
        product.setOrigin(request.origin());
        product.setVolume(request.volume());
        product.setStockQuantity(request.stockQuantity());
        product.setAlcoholPercentage(request.alcoholPercentage());

        Product saved = productRepository.save(product);

        return ToDtoMappers.toProductResponse(saved);
    }

    @Override
    public void deleteProduct(Long id, boolean permanent) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (permanent) {
            if (orderItemRepository.existsByProductId(id)) {
                throw new AppException(ErrorCode.PRODUCT_IN_USE);
            }
            productRepository.delete(product);
        } else {
            if (product.getDeletedAt() == null) {
                product.setDeletedAt(LocalDateTime.now());
                productRepository.save(product);
            }
        }
    }
}
