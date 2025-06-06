package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.CreateProductRequest;
import com.sun.wineshop.dto.request.ProductSearchRequest;
import com.sun.wineshop.dto.request.UpdateProductRequest;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.repository.CategoryRepository;
import com.sun.wineshop.repository.OrderItemRepository;
import com.sun.wineshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Wine")
                .build();

        product = Product.builder()
                .id(1L)
                .name("Red Wine")
                .description("Fine red wine")
                .price(100.0)
                .stockQuantity(10)
                .alcoholPercentage(12.5)
                .volume(750)
                .origin("France")
                .imageUrl("http://image.url")
                .categories(List.of(category))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllProducts_shouldReturnPageOfProducts() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAllByDeletedAtIsNull(any(Pageable.class))).thenReturn(page);

        Page<ProductResponse> result = productService.getAllProducts(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchProducts_shouldReturnFilteredPage() {
        ProductSearchRequest request = new ProductSearchRequest("Red", null, null, null, null, List.of(1L));
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.searchProducts(any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        Page<ProductResponse> result = productService.searchProducts(request, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProductById_whenFound_shouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertEquals("Red Wine", response.name());
    }

    @Test
    void getProductById_whenNotFound_shouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> productService.getProductById(1L));
        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void createProduct_whenValid_shouldReturnProductResponse() {
        CreateProductRequest request = new CreateProductRequest(
                "Red Wine",
                "Fine red wine",
                "http://image.url",
                100.0,
                "France",
                750,
                10,
                12.5,
                List.of(1L)
        );

        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertEquals("Red Wine", response.name());
    }

    @Test
    void createProduct_whenCategoryNotFound_shouldThrowException() {
        CreateProductRequest request = new CreateProductRequest(
                "Red Wine",
                "Fine red wine",
                "http://image.url",
                100.0,
                "France",
                750,
                10,
                12.5,
                List.of(1L)
        );

        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of());

        AppException ex = assertThrows(AppException.class, () -> productService.createProduct(request));
        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void updateProduct_whenFound_shouldUpdateFields() {
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Wine",
                "Updated description",
                "http://new.image.url",
                120.0,
                "Italy",
                700,
                15,
                14.0,
                List.of(1L)
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.updateProduct(1L, request);

        assertEquals("Updated Wine", response.name());
    }

    @Test
    void updateProduct_whenCategorySomeNotFound_shouldThrow() {
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Wine",
                "Updated description",
                "http://new.image.url",
                120.0,
                "Italy",
                700,
                15,
                14.0,
                List.of(1L, 2L)
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(category));

        AppException ex = assertThrows(AppException.class, () -> productService.updateProduct(1L, request));
        assertEquals(ErrorCode.CATEGORY_SOME_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void deleteProduct_whenPermanentAndInUse_shouldThrow() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(1L)).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> productService.deleteProduct(1L, true));
        assertEquals(ErrorCode.PRODUCT_IN_USE, ex.getErrorCode());
    }

    @Test
    void deleteProduct_whenPermanentAndNotInUse_shouldDelete() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(1L)).thenReturn(false);

        productService.deleteProduct(1L, true);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_whenSoftDelete_shouldMarkDeletedAt() {
        product.setDeletedAt(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L, false);

        verify(productRepository).save(any(Product.class));
        assertNotNull(product.getDeletedAt());
    }
}
