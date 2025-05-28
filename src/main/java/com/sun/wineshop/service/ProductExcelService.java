package com.sun.wineshop.service;

import com.sun.wineshop.model.entity.Product;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface ProductExcelService {
    ByteArrayInputStream exportToExcel(List<Product> products);
    int importFromExcel(InputStream inputStream);
}
