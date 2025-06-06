package com.sun.wineshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.configuration.SecurityConfig;
import com.sun.wineshop.dto.request.ProductSearchRequest;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.exception.AppException;
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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({
        ProductControllerTest.TestConfig.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class ProductControllerTest {

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public MessageSource messageSource() {
            ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
            messageSource.setBasename("classpath:messages");
            messageSource.setDefaultEncoding("UTF-8");
            return messageSource;
        }

        @Bean
        public MessageUtil messageUtil(MessageSource messageSource) {
            return new MessageUtil(messageSource);
        }

        @Bean
        public CustomJwtDecoder customJwtDecoder() {
            return Mockito.mock(CustomJwtDecoder.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void getAllProducts_shouldReturnList() throws Exception {
        Page<ProductResponse> mockPage = new PageImpl<>(List.of(
                sampleProductResponse()
        ));

        when(productService.getAllProducts(any())).thenReturn(mockPage);

        mockMvc.perform(MockMvcRequestBuilders.get(ProductApiPaths.BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Wine A"));
    }

    @Test
    void searchProducts_shouldReturnResults() throws Exception {
        Page<ProductResponse> mockPage = new PageImpl<>(List.of(
                sampleProductResponse()
        ));

        ProductSearchRequest request = new ProductSearchRequest("Wine", 10.0, 100.0, null, null, List.of(1L));

        when(productService.searchProducts(any(), any())).thenReturn(mockPage);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(ProductApiPaths.BASE + ProductApiPaths.Endpoint.SEARCH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Wine A"));
    }

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        ProductResponse productResponse = sampleProductResponse();

        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(ProductApiPaths.BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Wine A"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getProductById_shouldReturnNotFound_whenProductMissing() throws Exception {
        when(productService.getProductById(anyLong()))
                .thenThrow(new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.get(ProductApiPaths.BASE + "/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.PRODUCT_NOT_FOUND.getCode()));
    }

    private ProductResponse sampleProductResponse() {
        return new ProductResponse(
                1L,
                "Wine A",
                "Red wine from France",
                "image.jpg",
                45.5,
                "France",
                750,
                50,
                13.5,
                List.of(new CategoryResponse(1L, "Red", "Wine")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
