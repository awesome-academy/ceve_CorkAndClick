package com.sun.wineshop.service.impl;

import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.service.CategoryService;
import com.sun.wineshop.service.ProductExcelService;
import com.sun.wineshop.service.ProductService;
import com.sun.wineshop.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductExcelServiceImpl implements ProductExcelService {

    private final CategoryService categoryService;
    private final ProductService productService;

    public ByteArrayInputStream exportToExcel(List<Product> products) {
        String[] headers = {"ID", "Name", "Description", "Price", "Stock", "Alcohol %", "Volume", "Origin", "Image URL", "Created At"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Products");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstants.DATETIME_FORMAT);
            int rowIdx = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getName());
                row.createCell(2).setCellValue(p.getDescription());
                row.createCell(3).setCellValue(p.getPrice());
                row.createCell(4).setCellValue(p.getStockQuantity());
                row.createCell(5).setCellValue(p.getAlcoholPercentage());
                row.createCell(6).setCellValue(p.getVolume());
                row.createCell(7).setCellValue(p.getOrigin());
                row.createCell(8).setCellValue(p.getImageUrl());
                row.createCell(9).setCellValue(p.getCreatedAt().format(formatter));
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new AppException(ErrorCode.EXPORT_PRODUCT_FAIL);
        }
    }

    @Override
    public int importFromExcel(InputStream is) {
        List<Product> products = new ArrayList<>();
        List<String> failedRows = new ArrayList<>();
        int batchSize = 100;

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new AppException(ErrorCode.IMPORT_FILE_EMPTY);
            }

            Row headerRow = sheet.getRow(0);
            validateHeaders(headerRow);
            Map<String, Integer> headerMap = mapHeaders(headerRow);

            List<String> allCategoryNames = extractAllCategoryNames(sheet, headerMap);
            Map<String, Category> categoryMap = categoryService.findOrCreateByNames(new ArrayList<>(new HashSet<>(allCategoryNames))).stream()
                    .collect(Collectors.toMap(Category::getName, c -> c));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Product product = parseProductFromRow(row, headerMap, categoryMap);
                    products.add(product);
                } catch (Exception e) {
                    String errorMsg = String.format("Error at row %d: %s", i + 1, e.getMessage());
                    log.error(errorMsg, e);
                    failedRows.add(errorMsg);
                }
            }

            if (products.isEmpty()) {
                throw new AppException(ErrorCode.IMPORT_FILE_EMPTY);
            }

            saveProductBatches(products, failedRows, batchSize);

            if (!failedRows.isEmpty()) {
                log.warn("There are {} rows with errors when importing:", failedRows.size());
                failedRows.forEach(log::warn);
            }

            int successCount = products.size() - failedRows.size();
            log.info("Import completed: {} products saved successfully, {} errors", successCount, failedRows.size());

            return successCount;
        } catch (Exception e) {
            log.error("Error when importing excel: " + e.getMessage(), e);
            throw new AppException(ErrorCode.IMPORT_PRODUCT_FAIL);
        }
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private double getNumericValue(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC) return 0;
        return cell.getNumericCellValue();
    }

    private void validateHeaders(Row headerRow) {
        if (headerRow == null) {
            throw new AppException(ErrorCode.IMPORT_FILE_EMPTY);
        }

        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = getStringValue(cell)
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z]", "");
            headerMap.put(headerName, cell.getColumnIndex());
        }

        List<String> requiredHeaders = Arrays.asList(
                "name", "description", "price", "stock", "alcohol", "volume", "origin", "imageurl", "categories"
        );

        for (String requiredHeader : requiredHeaders) {
            if (!headerMap.containsKey(requiredHeader)) {
                throw new AppException(ErrorCode.IMPORT_MISSING_COLUMN, requiredHeader);
            }
        }
    }

    private Map<String, Integer> mapHeaders(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = getStringValue(cell)
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z]", "");
            headerMap.put(headerName, cell.getColumnIndex());
        }
        return headerMap;
    }

    private Product parseProductFromRow(Row row, Map<String, Integer> headerMap, Map<String, Category> categoryMap) {
        String name = getStringValue(row.getCell(headerMap.get("name")));
        String description = getStringValue(row.getCell(headerMap.get("description")));
        double price = getNumericValue(row.getCell(headerMap.get("price")));
        int stock = (int) getNumericValue(row.getCell(headerMap.get("stock")));
        double alcohol = getNumericValue(row.getCell(headerMap.get("alcohol")));
        int volume = (int) getNumericValue(row.getCell(headerMap.get("volume")));
        String origin = getStringValue(row.getCell(headerMap.get("origin")));
        String imageUrl = getStringValue(row.getCell(headerMap.get("imageurl")));
        String rawCategory = getStringValue(row.getCell(headerMap.get("categories")));

        List<Category> categories = parseCategories(rawCategory, categoryMap);

        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stockQuantity(stock)
                .alcoholPercentage(alcohol)
                .volume(volume)
                .origin(origin)
                .imageUrl(imageUrl)
                .categories(categories)
                .build();
    }

    private List<Category> parseCategories(String rawCategory, Map<String, Category> categoryMap) {
        if (rawCategory == null || rawCategory.isEmpty()) return new ArrayList<>();

        return Arrays.stream(rawCategory.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty() && categoryMap.containsKey(name))
                .map(categoryMap::get)
                .collect(Collectors.toList());
    }

    private List<String> extractAllCategoryNames(Sheet sheet, Map<String, Integer> headerMap) {
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String rawCategory = getStringValue(row.getCell(headerMap.get("categories")));
            if (!rawCategory.isEmpty()) {
                names.addAll(Arrays.stream(rawCategory.split(","))
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .toList());
            }
        }
        return names;
    }

    private void saveProductBatches(List<Product> products, List<String> failedRows, int batchSize) {
        for (int i = 0; i < products.size(); i += batchSize) {
            int end = Math.min(i + batchSize, products.size());
            List<Product> batch = products.subList(i, end);
            try {
                productService.saveAll(batch);
            } catch (Exception e) {
                String errorMsg = String.format("Failed to save batch from index %d to %d: %s", i, end, e.getMessage());
                log.error(errorMsg, e);
                failedRows.add(errorMsg);
            }
        }
    }
}
