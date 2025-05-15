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
    private final ReviewService reviewService;
    private final MessageUtil messageUtil;

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

    @PostMapping(ProductApiPaths.Endpoint.REVIEW)
    public ResponseEntity<BaseApiResponse<Void>> addReview(
            @PathVariable Long productId,
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = JwtUtil.extractUserIdFromJwt(jwt);
        reviewService.addReview(userId, productId, request);
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                messageUtil.getMessage("review.add.success")
        ));
    }
}
