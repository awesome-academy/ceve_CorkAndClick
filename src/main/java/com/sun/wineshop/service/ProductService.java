package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.ProductRequest;
import com.sun.wineshop.dto.request.ProductSearchRequest;
import com.sun.wineshop.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest request);
}
