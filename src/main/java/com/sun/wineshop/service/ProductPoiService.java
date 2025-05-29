package com.sun.wineshop.service;

import com.sun.wineshop.dto.response.ImportTaskResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;


public interface ProductPoiService {
    ByteArrayInputStream exportProducts();
    void importInBackground(MultipartFile file);
    ImportTaskResponse getImportStatus(Long id);
}
