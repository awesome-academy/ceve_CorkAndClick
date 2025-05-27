package com.sun.wineshop.service.impl;

import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.service.CategoryService;
import com.sun.wineshop.service.ProductExcelService;
import com.sun.wineshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
                row.createCell(9).setCellValue(p.getCreatedAt().toString());
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
        List<String> allCategoryNames = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new AppException(ErrorCode.IMPORT_FILE_EMPTY);
            }

            Row headerRow = sheet.getRow(0);
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

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String rawCategory = getStringValue(row.getCell(headerMap.get("categories")));
                if (!rawCategory.isEmpty()) {
                    List<String> names = Arrays.stream(rawCategory.split(","))
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .toList();
                    allCategoryNames.addAll(names);
                }
            }

            Map<String, Category> categoryMap = categoryService.findOrCreateByNames(allCategoryNames).stream()
                    .collect(Collectors.toMap(Category::getName, c -> c));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name = getStringValue(row.getCell(headerMap.get("name")));
                    String description = getStringValue(row.getCell(headerMap.get("description")));
                    double price = getNumericValue(row.getCell(headerMap.get("price")));
                    int stock = (int) getNumericValue(row.getCell(headerMap.get("stock")));
                    double alcohol = getNumericValue(row.getCell(headerMap.get("alcohol")));
                    int volume = (int) getNumericValue(row.getCell(headerMap.get("volume")));
                    String origin = getStringValue(row.getCell(headerMap.get("origin")));
                    String imageUrl = getStringValue(row.getCell(headerMap.get("imageurl")));
                    String rawCategory = getStringValue(row.getCell(headerMap.get("categories")));

                    List<Category> categories = new ArrayList<>();
                    if (!rawCategory.isEmpty()) {
                        List<String> categoryNames = Arrays.stream(rawCategory.split(","))
                            .map(String::trim)
                            .filter(n -> !n.isEmpty())
                            .toList();

                        for (String catName : categoryNames) {
                            if (categoryMap.containsKey(catName)) {
                                categories.add(categoryMap.get(catName));
                            }
                        }
                    }

                    Product product = Product.builder()
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

                    products.add(product);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    throw new AppException(ErrorCode.IMPORT_PRODUCT_FAIL);
                }
            }

            if (products.isEmpty()) {
                throw new AppException(ErrorCode.IMPORT_FILE_EMPTY);
            }

            productService.saveAll(products);
            return products.size();
        } catch (Exception e) {
            log.error(e.getMessage());
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
}
