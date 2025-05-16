package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.ProductSearchRequest;
import com.sun.wineshop.dto.request.ReviewRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.service.ProductService;
import com.sun.wineshop.service.ReviewService;
import com.sun.wineshop.utils.JwtUtil;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.ProductApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ProductApiPaths.BASE)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @PostMapping(ProductApiPaths.Endpoint.SEARCH)
    public ResponseEntity<Page<ProductResponse>> searchProducts(
        @RequestBody ProductSearchRequest request,
        Pageable pageable
    ) {
        return ResponseEntity.ok(productService.searchProducts(request, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
