package com.sun.wineshop.controller.admin;

import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.configuration.SecurityConfig;
import com.sun.wineshop.dto.response.ImportTaskResponse;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.exception.GlobalExceptionHandler;
import com.sun.wineshop.service.ProductPoiService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
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
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductPoiController.class)
@Import({
        ProductPoiControllerTest.TestConfig.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class,
})
@AutoConfigureMockMvc(addFilters = false)
class ProductPoiControllerTest {

    @TestConfiguration
    public static class TestConfig {
        @Bean
        public ProductPoiService productPoiService() {
            return Mockito.mock(ProductPoiService.class);
        }

        @Bean
        public MessageSource messageSource() {
            ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
            source.setBasename("classpath:messages");
            source.setDefaultEncoding("UTF-8");
            return source;
        }

        @Bean
        public MessageUtil messageUtil(MessageSource source) {
            return new MessageUtil(source);
        }

        @Bean
        public CustomJwtDecoder customJwtDecoder() {
            return Mockito.mock(CustomJwtDecoder.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductPoiService productPoiService;

    @Autowired
    private MessageUtil messageUtil;

    @Test
    void exportProducts_shouldReturnExcelFile() throws Exception {
        ByteArrayInputStream fakeStream = new ByteArrayInputStream("test".getBytes());
        when(productPoiService.exportProducts()).thenReturn(fakeStream);

        mockMvc.perform(MockMvcRequestBuilders.get(AdminApiPaths.Poi.PRODUCTS + AdminApiPaths.Poi.EXCEL_EXPORT))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.xlsx"))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    void importProducts_validFile_shouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "products.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test".getBytes());

        doNothing().when(productPoiService).importInBackground(file);

        mockMvc.perform(multipart(AdminApiPaths.Poi.PRODUCTS + AdminApiPaths.Poi.EXCEL_IMPORT).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage("file.import.success")));
    }

    @Test
    void importProducts_whenFileEmpty_shouldReturn400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        mockMvc.perform(multipart(AdminApiPaths.Poi.PRODUCTS + AdminApiPaths.Poi.EXCEL_IMPORT).file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.EXCEL_IMPORT_FILE_EMPTY.getCode()))
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage(ErrorCode.EXCEL_IMPORT_FILE_EMPTY.getMessageKey())));
    }

    @Test
    void importProducts_whenServiceThrows_shouldReturnImportFail() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "products.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test".getBytes());

        doThrow(new RuntimeException("fail")).when(productPoiService).importInBackground(file);

        mockMvc.perform(multipart(AdminApiPaths.Poi.PRODUCTS + AdminApiPaths.Poi.EXCEL_IMPORT).file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.EXCEL_IMPORT_FAIL.getCode()))
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage(ErrorCode.EXCEL_IMPORT_FAIL.getMessageKey())));
    }

    @Test
    void getImportStatus_shouldReturnResponse() throws Exception {
        ImportTaskResponse mockResponse = new ImportTaskResponse(
                1L,
                "products.xlsx",
                "DONE",
                null,
                LocalDateTime.of(2024, 6, 1, 10, 0),
                LocalDateTime.of(2024, 6, 1, 10, 2)
        );

        when(productPoiService.getImportStatus(1L)).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(AdminApiPaths.Poi.PRODUCTS + AdminApiPaths.Poi.IMPORT_STATUS_BY_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.fileName").value("products.xlsx"))
                .andExpect(jsonPath("$.data.status").value("DONE"))
                .andExpect(jsonPath("$.data.errorMessage").doesNotExist())
                .andExpect(jsonPath("$.data.startedAt").value("2024-06-01T10:00:00"))
                .andExpect(jsonPath("$.data.finishedAt").value("2024-06-01T10:02:00"));
    }
}
