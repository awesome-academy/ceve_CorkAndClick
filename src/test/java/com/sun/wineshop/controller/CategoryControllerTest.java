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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        CategoryControllerTest.CategoryTestConfig.class
})
public class CategoryControllerTest {

    @TestConfiguration
    static class CategoryTestConfig {
        @Bean
        public CategoryService categoryService() {
            return Mockito.mock(CategoryService.class);
        }

        @Bean
        public MessageUtil messageUtil() {
            MessageUtil mockMessageUtil = Mockito.mock(MessageUtil.class);
            when(mockMessageUtil.getMessage(ErrorCode.UNCATEGORIZED.getMessageKey()))
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
    private CategoryService categoryService;

    @Test
    void getCategories_success_shouldReturnPageOfCategories() throws Exception {
        int pageNumber = 0;
        int pageSize = 2;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<CategoryResponse> paginatedResponse = defaultPaginatedCategoryResponse(pageable);

        when(categoryService.getAllCategories(any(Pageable.class)))
                .thenReturn(paginatedResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(CategoryApiPaths.BASE)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(paginatedResponse.getContent().size())))
                .andExpect(jsonPath("$.content[0].id", is(paginatedResponse.getContent().getFirst().id().intValue())))
                .andExpect(jsonPath("$.content[0].name", is(paginatedResponse.getContent().getFirst().name())))
                .andExpect(jsonPath("$.totalPages", is(paginatedResponse.getTotalPages())))
                .andExpect(jsonPath("$.totalElements", is((int) paginatedResponse.getTotalElements())))
                .andExpect(jsonPath("$.number", is(paginatedResponse.getNumber())));
    }

    @Test
    void getCategories_empty_shouldReturnEmptyPage() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<CategoryResponse> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(categoryService.getAllCategories(any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(MockMvcRequestBuilders.get(CategoryApiPaths.BASE)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)))
                .andExpect(jsonPath("$.number", is(pageNumber)))
                .andExpect(jsonPath("$.empty", is(true)));
    }

    private Page<CategoryResponse> defaultPaginatedCategoryResponse(Pageable pageable) {
        List<CategoryResponse> categoryList = List.of(
                new CategoryResponse(1L, "Red Wine", "description"),
                new CategoryResponse(2L, "White Wine", "description")
        );
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), categoryList.size());
        List<CategoryResponse> pageContent = categoryList.subList(start, end);
        return new PageImpl<>(pageContent, pageable, categoryList.size());
    }
}
