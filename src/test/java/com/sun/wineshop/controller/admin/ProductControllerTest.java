package com.sun.wineshop.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.configuration.SecurityConfig;
import com.sun.wineshop.dto.request.CreateProductRequest;
import com.sun.wineshop.dto.request.UpdateProductRequest;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.exception.GlobalExceptionHandler;
import com.sun.wineshop.service.ProductService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(ProductController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        ProductControllerTest.TestConfig.class
})
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageUtil messageUtil;

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
    }

    @Test
    void createProduct_shouldReturnCreated() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "Wine A",
                "Red wine from France",
                "image.jpg",
                45.5,
                "France",
                750,
                50,
                13.5,
                List.of(1L)
        );

        ProductResponse response = sampleProductResponse();

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post(AdminApiPaths.Product.BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Wine A"));

        verify(productService).createProduct(any(CreateProductRequest.class));
    }

    @Test
    void createProduct_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        // Missing required fields: name is null, price negative, categoryIds empty
        CreateProductRequest invalidRequest = new CreateProductRequest(
                null,
                "desc",
                "", // invalid imageUrl (blank)
                -10.0, // invalid price (negative)
                "", // invalid origin (blank)
                10, // invalid volume (<50)
                -1, // invalid stockQuantity (<0)
                150.0, // invalid alcoholPercentage (>100)
                List.of() // empty categoryIds
        );

        mockMvc.perform(post(AdminApiPaths.Product.BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists()); // assuming GlobalExceptionHandler returns validation errors in errors field
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Wine",
                "Updated desc",
                "updated.jpg",
                50.0,
                "Italy",
                700,
                40,
                14.0,
                List.of(2L)
        );

        ProductResponse response = sampleProductResponse();
        response = new ProductResponse(
                response.id(),
                "Updated Wine",
                response.description(),
                response.imageUrl(),
                response.price(),
                response.origin(),
                response.volume(),
                response.stockQuantity(),
                response.alcoholPercentage(),
                response.categories(),
                response.createdAt(),
                response.updatedAt()
        );

        when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class))).thenReturn(response);

        mockMvc.perform(put(AdminApiPaths.Product.BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Wine"));

        verify(productService).updateProduct(eq(1L), any(UpdateProductRequest.class));
    }

    @Test
    void updateProduct_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        UpdateProductRequest invalidRequest = new UpdateProductRequest(
                "", // invalid name blank
                "desc",
                "", // invalid imageUrl blank
                null, // null price
                null, // null origin
                10, // invalid volume <50
                -10, // invalid stockQuantity <0
                -5.0, // invalid alcoholPercentage <0
                null // null categoryIds (allowed for update)
        );

        mockMvc.perform(put(AdminApiPaths.Product.BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteProduct_shouldReturnSuccessMessage() throws Exception {
        doNothing().when(productService).deleteProduct(1L, false);

        mockMvc.perform(delete(AdminApiPaths.Product.BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage("product.delete.success")));

        verify(productService).deleteProduct(1L, false);
    }

    @Test
    void deleteProduct_withPermanentTrue_shouldCallServiceWithPermanentFlag() throws Exception {
        doNothing().when(productService).deleteProduct(1L, true);

        mockMvc.perform(delete(AdminApiPaths.Product.BASE + "/1")
                        .param("permanent", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage("product.delete.success")));

        verify(productService).deleteProduct(1L, true);
    }

    private String asJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
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
