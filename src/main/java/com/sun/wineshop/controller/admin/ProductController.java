package com.sun.wineshop.controller.admin;

import com.sun.wineshop.dto.request.ProductRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.service.ProductService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController(AdminApiPaths.Product.ADMIN_PRODUCT_CONTROLLER)
@RequiredArgsConstructor
@RequestMapping(AdminApiPaths.Product.BASE)
public class ProductController {

    private final ProductService productService;
    private final MessageUtil messageUtil;

    @PostMapping
    private ResponseEntity<BaseApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new BaseApiResponse<>(
                        HttpStatus.OK.value(),
                        productService.createProduct(request)
                )
        );
    }

    @PutMapping(AdminApiPaths.Product.BY_ID)
    public ResponseEntity<BaseApiResponse<ProductResponse>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.status(HttpStatus.OK).body(
                new BaseApiResponse<>(
                        HttpStatus.OK.value(),
                        productService.updateProduct(id, request)
                )
        );
    }

    @DeleteMapping(AdminApiPaths.Product.BY_ID)
    public ResponseEntity<BaseApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);

        return ResponseEntity.status(HttpStatus.OK).body(
                new BaseApiResponse<>(
                        HttpStatus.OK.value(),
                        messageUtil.getMessage("product.delete.success")
                )
        );
    }
}
