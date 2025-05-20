package com.sun.wineshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.configuration.SecurityConfig;
import com.sun.wineshop.dto.request.ProductSearchRequest;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.exception.GlobalExceptionHandler;
import com.sun.wineshop.service.ProductService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.ProductApiPaths;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        ProductControllerTest.ProductControllerTestConfig.class
})
public class ProductControllerTest {

    @TestConfiguration
    static class ProductControllerTestConfig {
        @Bean
        public ProductService productService() {
            return Mockito.mock(ProductService.class);
        }

        @Bean
        public MessageUtil messageUtil() {
            MessageUtil mockMessageUtil = Mockito.mock(MessageUtil.class);
            when(mockMessageUtil.getMessage(eq(ErrorCode.UNCATEGORIZED.getMessageKey())))
                    .thenReturn("An unexpected error occurred.");
            return mockMessageUtil;
        }

        @Bean
        public CustomJwtDecoder customJwtDecoder() {
            return Mockito.mock(CustomJwtDecoder.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Page<ProductResponse> emptyPaginatedProductResponse(Pageable pageable) {
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    private ProductResponse defaultProductResponse(Long id, String name) {
        List<CategoryResponse> categories = Collections.emptyList();
        return new ProductResponse(
                id, name, "description",
                "http://example.com/image.jpg",
                25.99, "Russian", 750, 100, 13.5,
                categories, LocalDateTime.now().minusDays(1), LocalDateTime.now()
        );
    }

    private Page<ProductResponse> defaultPaginatedProductResponse(Pageable pageable) {
        List<ProductResponse> productList = List.of(
                defaultProductResponse(1L, "Test Wine 1"),
                defaultProductResponse(2L, "Test Wine 2")
        );
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), productList.size());
        List<ProductResponse> pageContent = productList.subList(start, end);
        return new PageImpl<>(pageContent, pageable, productList.size());
    }

    private ProductSearchRequest defaultProductSearchRequest() {
        return new ProductSearchRequest(
                "Test Wine", 10.0, 100.0,
                11.5, 15.0, List.of(1L, 3L)
        );
    }

    private ProductSearchRequest emptyProductSearchRequest() {
        return new ProductSearchRequest(
                null, null, null,
                null, null, Collections.emptyList()
        );
    }

    @Test
    void getAllProducts_success_shouldReturnPageOfProducts() throws Exception {
        int pageNumber = 0;
        int pageSize = 2;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductResponse> paginatedResponse = defaultPaginatedProductResponse(pageable);

        when(productService.getAllProducts(any(Pageable.class)))
                .thenReturn(paginatedResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(ProductApiPaths.BASE)
                        .param("page", String.valueOf(pageNumber)) 
                        .param("size", String.valueOf(pageSize)) 
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) 
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(paginatedResponse.getContent().size())))
                .andExpect(jsonPath("$.content[0].id", is(paginatedResponse.getContent().getFirst().id().intValue())))
                .andExpect(jsonPath("$.content[0].name", is(paginatedResponse.getContent().getFirst().name())))
                .andExpect(jsonPath("$.totalPages", is(paginatedResponse.getTotalPages())))
                .andExpect(jsonPath("$.totalElements", is((int) paginatedResponse.getTotalElements())))
                .andExpect(jsonPath("$.number", is(paginatedResponse.getNumber()))); 
    }

    @Test
    void getAllProducts_whenNoProducts_shouldReturnEmptyPage() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductResponse> emptyPageResponse = emptyPaginatedProductResponse(pageable);

        when(productService.getAllProducts(any(Pageable.class)))
                .thenReturn(emptyPageResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(ProductApiPaths.BASE)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)))
                .andExpect(jsonPath("$.number", is(pageNumber)))
                .andExpect(jsonPath("$.empty", is(true)));
    }

    @Test
    void getProductById_success_shouldReturnProduct() throws Exception {
        long productId = 1;
        ProductResponse productResponse = defaultProductResponse(productId, "Specific Wine");
        when(productService.getProductById(eq(productId))).thenReturn(productResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(ProductApiPaths.BASE + "/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(productResponse.id().intValue())))
                .andExpect(jsonPath("$.name", is(productResponse.name())));
    }

    @Test
    void getProductById_notFound_shouldReturnErrorCode999() throws Exception {
        long productId = 99;
        String expectedUncategorizedMessage = "An unexpected error occurred.";

        when(productService.getProductById(eq(productId)))
                .thenThrow(new RuntimeException("Product Not Found."));

        mockMvc.perform(MockMvcRequestBuilders.get(ProductApiPaths.BASE + "/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(ErrorCode.UNCATEGORIZED.getCode()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(ErrorCode.UNCATEGORIZED.getCode())))
                .andExpect(jsonPath("$.message", is(expectedUncategorizedMessage)));
    }

    @Test
    void searchProducts_success_shouldReturnPageOfProducts() throws Exception {
        ProductSearchRequest searchRequest = defaultProductSearchRequest();
        int pageNumber = 0;
        int pageSize = 2;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductResponse> paginatedResponse = defaultPaginatedProductResponse(pageable);

        when(productService.searchProducts(eq(searchRequest), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        mockMvc.perform(MockMvcRequestBuilders.post(ProductApiPaths.BASE + ProductApiPaths.Endpoint.SEARCH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest))
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(paginatedResponse.getContent().size())))
                .andExpect(jsonPath("$.content[0].name", is(paginatedResponse.getContent().getFirst().name())));
    }

    @Test
    void searchProducts_success_withEmptyCriteria_shouldReturnPageOfProducts() throws Exception {
        ProductSearchRequest searchRequest = emptyProductSearchRequest();
        int pageNumber = 0;
        int pageSize = 2;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductResponse> paginatedResponse = defaultPaginatedProductResponse(pageable);

        when(productService.searchProducts(eq(searchRequest), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        mockMvc.perform(MockMvcRequestBuilders.post(ProductApiPaths.BASE + ProductApiPaths.Endpoint.SEARCH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest))
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(paginatedResponse.getContent().size())));
    }

    @Test
    void searchProducts_errorJsonBody_shouldReturnErrorCode999() throws Exception {
        String errorJsonRequest = "{\"name\": \"Test Wine\", \"minPrice\": \"not a double value\"}";
        String expectedUncategorizedMessage = "An unexpected error occurred.";

        mockMvc.perform(MockMvcRequestBuilders.post(ProductApiPaths.BASE + ProductApiPaths.Endpoint.SEARCH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(errorJsonRequest)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().is(ErrorCode.UNCATEGORIZED.getCode()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(ErrorCode.UNCATEGORIZED.getCode())))
                .andExpect(jsonPath("$.message", is(expectedUncategorizedMessage)));
    }
}
