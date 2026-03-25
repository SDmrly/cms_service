package com.cms.service;

import com.cms.domain.entity.CmsKey;
import com.cms.domain.entity.Project;
import com.cms.domain.enums.ValueType;
import com.cms.dto.response.ImportResultResponse;
import com.cms.repository.CmsKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import org.springframework.lang.NonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertiesImportManager implements PropertiesImportService {

    private final ProjectService projectService;
    private final CmsKeyRepository cmsKeyRepository;

    @Override
    @Transactional
    @CacheEvict(value = "cms_project_keys", allEntries = true)
    public ImportResultResponse importKeys(@NonNull UUID projectId, String category, MultipartFile file) {
        log.info("Starting properties import for project: {}, category: {}", projectId, category);
        
        Project project = projectService.getProjectEntity(projectId);
        
        int created = 0;
        int updated = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        Properties properties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            properties.load(reader);
            
            for (String keyName : properties.stringPropertyNames()) {
                String defaultValue = properties.getProperty(keyName);
                
                try {
                    Optional<CmsKey> existingKeyOpt = cmsKeyRepository.findByProjectCodeAndKey(project.getCode(), keyName);
                    
                    if (existingKeyOpt.isPresent()) {
                        CmsKey existingKey = existingKeyOpt.get();
                        existingKey.setDefaultValue(defaultValue);
                        
                        // We do NOT update the ValueType if it already exists, to prevent breaking data
                        cmsKeyRepository.save(Objects.requireNonNull(existingKey));
                        updated++;
                        log.debug("Updated existing key: {}", keyName);
                    } else {
                        CmsKey newKey = CmsKey.builder()
                                .project(project)
                                .key(keyName)
                                .valueType(detectValueType(defaultValue))
                                .defaultValue(defaultValue)
                                .category(category)
                                .active(true)
                                .build();
                                
                        cmsKeyRepository.save(Objects.requireNonNull(newKey));
                        created++;
                        log.debug("Created new key: {}", keyName);
                    }
                } catch (Exception e) {
                    log.error("Error processing key '{}': {}", keyName, e.getMessage());
                    errors.add("Key '" + keyName + "': " + e.getMessage());
                    skipped++;
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse properties file", e);
            throw new RuntimeException("Failed to parse properties file: " + e.getMessage());
        }

        return ImportResultResponse.builder()
                .totalKeys(properties.size())
                .created(created)
                .updated(updated)
                .skipped(skipped)
                .errors(errors)
                .build();
    }

    private ValueType detectValueType(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ValueType.TEXT;
        }
        
        String trimmed = value.trim();
        
        // Check Boolean
        if (trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("false")) {
            return ValueType.BOOLEAN;
        }
        
        // Check Number (Integer or Double)
        try {
            Double.parseDouble(trimmed);
            return ValueType.NUMBER;
        } catch (NumberFormatException e) {
            // Not a number, fallback to text
        }
        
        return ValueType.TEXT;
    }
}
