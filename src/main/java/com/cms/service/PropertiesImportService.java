package com.cms.service;

import com.cms.dto.response.ImportResultResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface PropertiesImportService {
    ImportResultResponse importKeys(UUID projectId, String category, MultipartFile file);
}
