package com.cms.service;

import com.cms.dto.response.ImportResultResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.lang.NonNull;
import java.util.UUID;

public interface PropertiesImportService {
    ImportResultResponse importKeys(@NonNull UUID projectId, String category, MultipartFile file);
}
