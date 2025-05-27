package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.ProductSearchRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.service.ProductExcelService;
import com.sun.wineshop.service.ProductService;
import com.sun.wineshop.utils.AppConstants;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.ProductApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping(ProductApiPaths.BASE)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductExcelService productExcelService;
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

    @GetMapping(ProductApiPaths.Endpoint.EXPORT)
    public ResponseEntity<InputStreamResource> exportProductsToExcel(){
        ByteArrayInputStream stream = productExcelService.exportToExcel(productService.getAllProductsForExport());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, AppConstants.EXPORT_PRODUCT_FILE_NAME)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    @PostMapping(ProductApiPaths.Endpoint.IMPORT)
    public ResponseEntity<BaseApiResponse<Void>> importProductsFromExcel(
            @RequestParam(AppConstants.IMPORT_TYPE) MultipartFile file
    ) throws IOException {
        int importedCount = productExcelService.importFromExcel(file.getInputStream());
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                messageUtil.getMessage("import.product.success", importedCount)
        ));
    }
}
