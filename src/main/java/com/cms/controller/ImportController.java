package com.cms.controller;

import com.cms.dto.response.ImportResultResponse;
import com.cms.service.PropertiesImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.lang.NonNull;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ImportController {

    private final PropertiesImportService importService;

    @PostMapping(value = "/{projectId}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResultResponse importProperties(
            @PathVariable @NonNull UUID projectId,
            @RequestParam(required = false, defaultValue = "CONFIG") String category,
            @RequestPart("file") MultipartFile file) {
            
        return importService.importKeys(projectId, category, file);
    }
}
