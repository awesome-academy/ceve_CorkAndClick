package com.sun.wineshop.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.configuration.SecurityConfig;
import com.sun.wineshop.dto.request.CategoryRequest;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.exception.GlobalExceptionHandler;
import com.sun.wineshop.service.CategoryService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({
        CategoryControllerTest.TestConfig.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class,
})
@AutoConfigureMockMvc(addFilters = false)
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
    @Autowired
    private MessageUtil messageUtil;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private CategoryRequest request;
    private CategoryResponse response;

    @BeforeEach
    void setUp() {
        request = new CategoryRequest("Wine", "Alcoholic beverage");
        response = new CategoryResponse(1L, "Wine", "Alcoholic beverage");
    }

    @Test
    void createCategory_valid_shouldReturnCreated() throws Exception {
        when(categoryService.createCategory(any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post(AdminApiPaths.Category.BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void createCategory_whenInvalidRequest_shouldReturn400() throws Exception {
        CategoryRequest invalidRequest = new CategoryRequest("", ""); // name trống

        mockMvc.perform(MockMvcRequestBuilders.post(AdminApiPaths.Category.BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateCategory_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(AdminApiPaths.Category.BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(categoryService).updateCategory(eq(1L), any());
    }

    @Test
    void updateCategory_whenNotFound_shouldReturnNotFound() throws Exception {
        doThrow(new AppException(ErrorCode.CATEGORY_NOT_FOUND)).when(categoryService)
                .updateCategory(eq(999L), any());

        mockMvc.perform(MockMvcRequestBuilders.put(AdminApiPaths.Category.BASE + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage(ErrorCode.CATEGORY_NOT_FOUND.getMessageKey())));
    }

    @Test
    void deleteCategory_success() throws Exception {
        doNothing().when(categoryService).deleteCategoryById(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete(AdminApiPaths.Category.BASE + "/1"))
                .andExpect(status().isOk());

        verify(categoryService, times(2)).deleteCategoryById(1L);
    }

    @Test
    void deleteCategory_whenInUse_shouldReturnInUse() throws Exception {
        doThrow(new AppException(ErrorCode.CATEGORY_IN_USE)).when(categoryService)
                .deleteCategoryById(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete(AdminApiPaths.Category.BASE + "/1"))
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_IN_USE.getCode()))
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage(ErrorCode.CATEGORY_IN_USE.getMessageKey())));
    }

    @Test
    void deleteCategory_whenNotFound_shouldReturnNotFound() throws Exception {
        doThrow(new AppException(ErrorCode.CATEGORY_NOT_FOUND)).when(categoryService)
                .deleteCategoryById(999L);

        mockMvc.perform(MockMvcRequestBuilders.delete(AdminApiPaths.Category.BASE + "/999"))
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage(ErrorCode.CATEGORY_NOT_FOUND.getMessageKey())));
    }
}