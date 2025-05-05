package com.sun.wineshop.service;

import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ToDtoMappers::toProductResponse);
    }
}
