package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.CreateProductRequest;
import com.sun.wineshop.dto.response.ImportTaskResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.mapper.ToEntityMappers;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.ImportTask;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.model.enums.ImportStatus;
import com.sun.wineshop.repository.CategoryRepository;
import com.sun.wineshop.repository.ImportTaskRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.service.ProductPoiService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.sun.wineshop.utils.ExcelCellUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPoiServiceImpl implements ProductPoiService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImportTaskRepository importTaskRepository;

    @Override
    public ByteArrayInputStream exportProducts() {
        List<Product> products = productRepository.findAll();
        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("Products");

            // Header
            String[] headers = {
                    "ID", "Name", "Description", "Price", "Stock Quantity",
                    "Alcohol %", "Volume", "Origin", "Image URL", "Categories"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowIdx++);
                List<Object> cellValues = List.of(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStockQuantity(),
                        product.getAlcoholPercentage(),
                        product.getVolume(),
                        product.getOrigin(),
                        product.getImageUrl(),
                        product.getCategories().stream()
                                .map(Category::getName)
                                .collect(Collectors.joining(","))
                );

                for (int i = 0; i < cellValues.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object value = cellValues.get(i);
                    if (value instanceof Number number) {
                        cell.setCellValue(number.doubleValue());
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new AppException(ErrorCode.EXCEL_EXPORT_FAIL);
        }
    }

    private void importProducts(MultipartFile file) {
        try (
                Workbook workbook = new XSSFWorkbook(file.getInputStream());
                ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
        ) {
            Validator validator = factory.getValidator();
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                try {
                    // Parse Categories from cell
                    String categoryCell = getString(row, 9); // ex: "Wine, Beer"
                    List<String> categoryNames = Arrays.stream(categoryCell.split(","))
                            .map(String::trim)
                            .filter(name -> !name.isEmpty())
                            .toList();

                    // Fetch categories by name
                    List<Category> categories = findDistinctByName(categoryNames);

                    if (categories.size() != categoryNames.size()) {
                        List<String> foundNames = categories.stream().map(Category::getName).toList();
                        List<String> missingNames = categoryNames.stream()
                                .filter(name -> !foundNames.contains(name))
                                .toList();

                        log.warn("❌ Skipped row {} due to missing category names: {}", i + 1, missingNames);
                        continue;
                    }

                    // Create request to validate
                    CreateProductRequest request = new CreateProductRequest(
                            getString(row, 1), // name
                            getString(row, 2), // description
                            getString(row, 8), // imageUrl
                            getDouble(row, 3), // price
                            getString(row, 7), // origin
                            getInteger(row, 6), // volume
                            getInteger(row, 4), // stockQuantity
                            getDouble(row, 5), // alcoholPercentage
                            categories.stream().map(Category::getId).toList()
                    );

                    Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
                    if (!violations.isEmpty()) {
                        String reasons = violations.stream()
                                .map(v -> v.getPropertyPath() + " - " + v.getMessage())
                                .collect(Collectors.joining("; "));
                        log.warn("❌ Skipped row {} due to validation errors: {}", i + 1, reasons);
                        continue;
                    }

                    Product product = ToEntityMappers.toProduct(request, categories);
                    productRepository.save(product);

                } catch (NumberFormatException e) {
                    log.warn("❌ Skipped row {} due to invalid category IDs format: {}", i + 1, e.getMessage());
                } catch (Exception e) {
                    log.warn("❌ Skipped row {} due to unexpected error: {}", i + 1, e.getMessage());
                }
            }

        } catch (IOException e) {
            log.error("❌ Import failed due to IO error: ", e);
            throw new AppException(ErrorCode.EXCEL_IMPORT_FAIL);
        }
    }

    @Override
    public void importInBackground(MultipartFile file) {
        ImportTask task = new ImportTask();
        task.setFileName(file.getOriginalFilename());
        task.setStatus(ImportStatus.PROCESSING);
        task.setStartedAt(LocalDateTime.now());
        task = importTaskRepository.save(task);

        try {
            importProducts(file);
            task.setStatus(ImportStatus.SUCCESS);
        } catch (Exception e) {
            task.setStatus(ImportStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            log.error("Import failed: ", e);
        }

        task.setFinishedAt(LocalDateTime.now());
        importTaskRepository.save(task);
    }

    @Override
    public ImportTaskResponse getImportStatus(Long id) {
        ImportTask task = importTaskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        return ToDtoMappers.toImportTaskResponse(task);
    }

    private List<Category> findDistinctByName(List<String> names) {
        List<Category> categories = categoryRepository.findByNameIn(names);

        Map<String, Category> distinctMap = new LinkedHashMap<>();
        for (Category category : categories) {
            distinctMap.putIfAbsent(category.getName(), category);
        }

        return new ArrayList<>(distinctMap.values());
    }
}
