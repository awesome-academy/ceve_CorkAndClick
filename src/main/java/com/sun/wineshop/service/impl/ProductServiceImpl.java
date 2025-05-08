package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.ProductSearchRequest;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

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
}
