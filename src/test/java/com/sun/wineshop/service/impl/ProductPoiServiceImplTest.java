package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.response.ImportTaskResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.ImportTask;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.model.enums.ImportStatus;
import com.sun.wineshop.repository.CategoryRepository;
import com.sun.wineshop.repository.ImportTaskRepository;
import com.sun.wineshop.repository.ProductRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPoiServiceImplTest {

    @InjectMocks
    private ProductPoiServiceImpl productPoiService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ImportTaskRepository importTaskRepository;

    @Test
    void exportProducts_shouldReturnExcelStream() throws IOException {
        Category category = Category.builder()
                .id(1L)
                .name("Wine")
                .description("Red Wine")
                .deletedAt(null)
                .products(new ArrayList<>())
                .build();

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Wine");
        product.setDescription("Red wine");
        product.setPrice(10.5);
        product.setStockQuantity(50);
        product.setAlcoholPercentage(13.0);
        product.setVolume(750);
        product.setOrigin("France");
        product.setImageUrl("http://image.com/wine.jpg");
        product.setCategories(List.of(category));

        when(productRepository.findAll()).thenReturn(List.of(product));

        ByteArrayInputStream result = productPoiService.exportProducts();
        assertNotNull(result);

        try (Workbook workbook = new XSSFWorkbook(result)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            assertEquals("ID", headerRow.getCell(0).getStringCellValue());
        }
    }

    @Test
    void importInBackground_shouldCreateSuccessTask() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "products.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{}
        );

        when(importTaskRepository.save(any())).thenAnswer(invocation -> {
            ImportTask task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });

        productPoiService.importInBackground(file);

        verify(importTaskRepository, atLeastOnce()).save(any());
    }

    @Test
    void getImportStatus_shouldReturnResponse() {
        ImportTask task = new ImportTask();
        task.setId(1L);
        task.setFileName("file.xlsx");
        task.setStatus(ImportStatus.SUCCESS);
        task.setStartedAt(LocalDateTime.now().minusMinutes(2));
        task.setFinishedAt(LocalDateTime.now());

        when(importTaskRepository.findById(1L)).thenReturn(Optional.of(task));

        ImportTaskResponse response = productPoiService.getImportStatus(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("file.xlsx", response.fileName());
    }

    @Test
    void getImportStatus_shouldThrow_whenNotFound() {
        when(importTaskRepository.findById(anyLong())).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> {
            productPoiService.getImportStatus(99L);
        });

        assertEquals(ErrorCode.TASK_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void exportProducts_shouldThrow_whenIOExceptionOccurs() {
        Product product = mock(Product.class);
        when(productRepository.findAll()).thenReturn(List.of(product));
        doThrow(new AppException(ErrorCode.EXCEL_EXPORT_FAIL)).when(product).getId();

        assertThrows(AppException.class, () -> productPoiService.exportProducts());
    }
}
