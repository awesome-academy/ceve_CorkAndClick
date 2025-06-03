package com.sun.wineshop.controller;

import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.configuration.SecurityConfig;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.exception.GlobalExceptionHandler;
import com.sun.wineshop.service.CategoryService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.CategoryApiPaths;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({
        CategoryControllerTest.TestConfig.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class,
})
class CategoryControllerTest {

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public CategoryService categoryService() {
            return Mockito.mock(CategoryService.class);
        }

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

    @Autowired
    private CategoryService categoryService;

    @Test
    void getCategories_shouldReturnList() throws Exception {
        Page<CategoryResponse> mockPage = new PageImpl<>(List.of(
                new CategoryResponse(1L, "Wine", "Alcoholic")
        ));

        when(categoryService.getAllCategories(any())).thenReturn(mockPage);

        mockMvc.perform(MockMvcRequestBuilders.get(CategoryApiPaths.BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Wine"));
    }

    @Test
    void getCategories_withPaginationParams_shouldReturnPagedResult() throws Exception {
        Page<CategoryResponse> mockPage = new PageImpl<>(List.of(
                new CategoryResponse(1L, "Red Wine", "Alcohol"),
                new CategoryResponse(2L, "White Wine", "Alcohol")
        ));

        when(categoryService.getAllCategories(any())).thenReturn(mockPage);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(CategoryApiPaths.BASE)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Red Wine"));
    }

    @Test
    void getCategories_shouldReturnEmptyList() throws Exception {
        Page<CategoryResponse> mockPage = new PageImpl<>(List.of());

        when(categoryService.getAllCategories(any())).thenReturn(mockPage);

        mockMvc.perform(MockMvcRequestBuilders.get(CategoryApiPaths.BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getCategories_shouldReturnInternalServerError_whenServiceFails() throws Exception {
        when(categoryService.getAllCategories(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(MockMvcRequestBuilders.get(CategoryApiPaths.BASE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.UNCATEGORIZED.getCode()))
                .andExpect(jsonPath("$.message").exists());
    }
}