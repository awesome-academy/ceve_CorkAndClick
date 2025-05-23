package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.CreateProductRequest;
import com.sun.wineshop.dto.request.UpdateProductRequest;
import com.sun.wineshop.dto.request.ProductSearchRequest;
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
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private CreateProductRequest createProductRequest;
    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Red Wine")
                .build();

        createProductRequest = CreateProductRequest.builder()
                .name("Wine A")
                .description("Description")
                .price(99.99)
                .imageUrl("https://image.png")
                .origin("France")
                .volume(750)
                .alcoholPercentage(13.5)
                .stockQuantity(100)
                .categoryIds(List.of(1L))
                .build();

        product = Product.builder()
                .id(1L)
                .name("Wine A")
                .description("Description")
                .price(99.99)
                .imageUrl("https://image.png")
                .origin("France")
                .volume(750)
                .alcoholPercentage(13.5)
                .stockQuantity(100)
                .categories(List.of(category))
                .build();
    }

    @Test
    void createProduct_withValidRequest_shouldReturnProductResponse() {
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(createProductRequest);

        assertEquals("Wine A", response.name());
        assertEquals("France", response.origin());
    }

    @Test
    void createProduct_withInvalidCategory_shouldThrowAppException() {
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of());

        AppException ex = assertThrows(AppException.class,
                () -> productService.createProduct(createProductRequest));

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getAllProducts_shouldReturnPageOfProductResponse() {
        Page<Product> products = new PageImpl<>(List.of(product));
        when(productRepository.findAllByDeletedAtIsNull(any(Pageable.class))).thenReturn(products);

        Page<ProductResponse> result = productService.getAllProducts(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Wine A", result.getContent().getFirst().name());
    }

    @Test
    void getProductById_whenProductExists_shouldReturnResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertEquals("Wine A", response.name());
    }

    @Test
    void getProductById_whenProductNotFound_shouldThrowAppException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> productService.getProductById(99L));

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void updateProduct_whenValidRequest_shouldReturnUpdatedResponse() {
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Wine",
                "Updated description",
                "https://updated.jpg",
                249.99,
                "Italy",
                750,
                80,
                14.0,
                List.of(1L)
        );

        Category category = new Category();
        category.setId(1L);
        category.setName("Red");

        Product product = new Product();
        product.setId(1L);
        product.setName("Old Wine");
        product.setCategories(List.of(category));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse response = productService.updateProduct(1L, request);

        assertEquals("Updated Wine", response.name());
        assertEquals(249.99, response.price());
    }

    @Test
    void updateProduct_whenSomeCategoryIdsNotFound_shouldThrowAppException() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Old Name");

        List<Long> requestedIds = List.of(1L, 2L);
        UpdateProductRequest request = new UpdateProductRequest(
                "New Name",
                "Updated Description",
                "https://updated.jpg",
                150.0,
                "France",
                750,
                10,
                13.5,
                requestedIds
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Category foundCategory = new Category();
        foundCategory.setId(1L);
        when(categoryRepository.findAllById(requestedIds)).thenReturn(List.of(foundCategory));

        AppException ex = assertThrows(AppException.class, () -> productService.updateProduct(1L, request));
        assertEquals(ErrorCode.CATEGORY_SOME_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void deleteProduct_whenPermanentAndInUse_shouldThrowException() {
        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(1L)).thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> productService.deleteProduct(1L, true));

        assertEquals(ErrorCode.PRODUCT_IN_USE, ex.getErrorCode());
    }

    @Test
    void deleteProduct_whenPermanentAndNotInUse_shouldDelete() {
        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(1L)).thenReturn(false);

        productService.deleteProduct(1L, true);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_whenSoftDelete_shouldSetDeletedAt() {
        Product product = new Product();
        product.setId(1L);
        product.setDeletedAt(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L, false);

        assertNotNull(product.getDeletedAt());
        verify(productRepository).save(product);
    }

    @Test
    void searchProducts_shouldReturnMatchingResults() {
        ProductSearchRequest request = new ProductSearchRequest(
                "wine",
                100.0,
                300.0,
                10.0,
                15.0,
                List.of(1L)
        );

        Category category = new Category();
        category.setId(1L);
        category.setName("Red");

        Product product = new Product();
        product.setId(1L);
        product.setName("Red Wine");
        product.setPrice(200.0);
        product.setCategories(List.of(category));

        Page<Product> products = new PageImpl<>(List.of(product));

        when(productRepository.searchProducts(
                eq("wine"),
                eq(100.0),
                eq(300.0),
                eq(10.0),
                eq(15.0),
                eq(List.of(1L)),
                any(Pageable.class)
        )).thenReturn(products);

        Page<ProductResponse> result = productService.searchProducts(request, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Red Wine", result.getContent().getFirst().name());
    }
}
