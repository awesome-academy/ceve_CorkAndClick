package com.sun.wineshop.controller.admin;

import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.ImportTaskResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.service.ProductPoiService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

import static com.sun.wineshop.utils.AppConstants.EXCEL_MEDIA_TYPE;

@RestController(AdminApiPaths.Poi.ADMIN_POI_CONTROLLER)
@RequiredArgsConstructor
@RequestMapping(AdminApiPaths.Poi.PRODUCTS)
@Slf4j
public class ProductPoiController {

    private final ProductPoiService productPoiService;
    private final MessageUtil messageUtil;

    @GetMapping(AdminApiPaths.Poi.EXCEL_EXPORT)
    public ResponseEntity<InputStreamResource> exportProducts() {
        ByteArrayInputStream in = productPoiService.exportProducts();
        InputStreamResource resource = new InputStreamResource(in);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.xlsx")
                .contentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE))
                .body(resource);
    }

    @PostMapping(AdminApiPaths.Poi.EXCEL_IMPORT)
    public ResponseEntity<BaseApiResponse<String>> importProducts(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.EXCEL_IMPORT_FILE_EMPTY);
        }

        try {
            productPoiService.importInBackground(file);
            return ResponseEntity.ok(
                    new BaseApiResponse<>(
                            HttpStatus.OK.value(),
                            messageUtil.getMessage("file.import.success")
                    ));
        } catch (Exception e) {
            log.error("Import failed", e);
            throw new AppException(ErrorCode.EXCEL_IMPORT_FAIL);
        }
    }

    @GetMapping(AdminApiPaths.Poi.IMPORT_STATUS_BY_ID)
    public ResponseEntity<BaseApiResponse<ImportTaskResponse>> getImportStatus(@PathVariable Long id) {
        return ResponseEntity.ok(
                new BaseApiResponse<>(
                        HttpStatus.OK.value(),
                        productPoiService.getImportStatus(id)
                ));
    }
}
