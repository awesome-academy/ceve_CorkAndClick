package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.CreateProductRequest;
import com.sun.wineshop.dto.request.ProductSearchRequest;
import com.sun.wineshop.dto.request.UpdateProductRequest;
import com.sun.wineshop.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id, boolean permanent);
}
