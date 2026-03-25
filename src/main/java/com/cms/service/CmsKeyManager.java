package com.cms.service;

import com.cms.domain.entity.CmsKey;
import com.cms.domain.entity.Project;
import com.cms.dto.request.CmsKeyRequest;
import com.cms.dto.response.CmsKeyResponse;
import com.cms.dto.response.CmsKeyValueResponse;
import com.cms.exception.DuplicateResourceException;
import com.cms.exception.ResourceNotFoundException;
import com.cms.mapper.CmsKeyMapper;
import com.cms.repository.CmsKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsKeyManager implements CmsKeyService {

    private final CmsKeyRepository cmsKeyRepository;
    private final ProjectService projectService;
    private final CmsKeyMapper cmsKeyMapper;

    @Override
    @Transactional
    @CacheEvict(value = "cms_project_keys", allEntries = true)
    public CmsKeyResponse createKey(@NonNull UUID projectId, CmsKeyRequest request) {
        log.info("Creating key '{}' for project: {}", request.getKey(), projectId);
        
        Project project = projectService.getProjectEntity(projectId);

        if (cmsKeyRepository.existsByProjectIdAndKey(projectId, request.getKey())) {
            throw new DuplicateResourceException("Key '" + request.getKey() + "' already exists in project " + project.getCode());
        }

        CmsKey cmsKey = cmsKeyMapper.toEntity(request);
        cmsKey.setProject(project);
        
        CmsKey savedKey = cmsKeyRepository.save(cmsKey);
        return cmsKeyMapper.toResponse(savedKey);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cms_project_keys", allEntries = true)
    public CmsKeyResponse updateKey(@NonNull UUID keyId, CmsKeyRequest request) {
        log.info("Updating key: {}", keyId);
        
        CmsKey cmsKey = getCmsKeyEntity(keyId);

        // Check uniqueness if key is being renamed
        if (!cmsKey.getKey().equals(request.getKey()) && 
            cmsKeyRepository.existsByProjectIdAndKey(cmsKey.getProject().getId(), request.getKey())) {
            throw new DuplicateResourceException("Key '" + request.getKey() + "' already exists in project " + cmsKey.getProject().getCode());
        }

        cmsKeyMapper.updateEntityFromRequest(request, cmsKey);
        CmsKey updatedKey = cmsKeyRepository.save(cmsKey);
        
        return cmsKeyMapper.toResponse(updatedKey);
    }

    @Override
    @Transactional(readOnly = true)
    public CmsKeyResponse getKey(@NonNull UUID keyId) {
        log.info("Fetching key: {}", keyId);
        return cmsKeyMapper.toResponse(getCmsKeyEntity(keyId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CmsKeyResponse> getKeysByProject(@NonNull UUID projectId, String category) {
        log.info("Fetching keys for project: {}, category: {}", projectId, category);
        
        List<CmsKey> keys;
        if (category != null && !category.trim().isEmpty()) {
            keys = cmsKeyRepository.findByProjectIdAndCategory(projectId, category);
        } else {
            keys = cmsKeyRepository.findByProjectId(projectId);
        }
        
        return cmsKeyMapper.toResponseList(keys);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cms_project_keys", key = "#projectCode + '_' + #key + '_' + #locale")
    public CmsKeyValueResponse getKeyValue(String projectCode, String key, String locale) {
        log.info("Fetching value for project: {}, key: {}, locale: {}", projectCode, key, locale);
        
        CmsKey cmsKey = cmsKeyRepository.findByProjectCodeAndKey(projectCode, key)
                .orElseThrow(() -> new ResourceNotFoundException("Key '" + key + "' not found in project '" + projectCode + "'"));
                
        if (!cmsKey.isActive()) {
            throw new ResourceNotFoundException("Key '" + key + "' is inactive in project '" + projectCode + "'");
        }

        Object evaluatedValue = extractValue(cmsKey, locale);
        
        return CmsKeyValueResponse.builder()
                .key(cmsKey.getKey())
                .value(evaluatedValue)
                .valueType(cmsKey.getValueType())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cms_project_keys", key = "#projectCode + '_' + #locale")
    public Map<String, Object> getAllKeyValues(String projectCode, String locale) {
        log.info("Fetching all values for project: {}, locale: {}", projectCode, locale);
        
        List<CmsKey> activeKeys = cmsKeyRepository.findByProjectCode(projectCode).stream()
                .filter(CmsKey::isActive)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        for (CmsKey key : activeKeys) {
            result.put(key.getKey(), extractValue(key, locale));
        }
        
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = "cms_project_keys", allEntries = true)
    public CmsKeyResponse toggleKeyStatus(@NonNull UUID keyId) {
        log.info("Toggling status for key: {}", keyId);
        
        CmsKey cmsKey = getCmsKeyEntity(keyId);
        cmsKey.setActive(!cmsKey.isActive());
        
        CmsKey updatedKey = cmsKeyRepository.save(cmsKey);
        return cmsKeyMapper.toResponse(updatedKey);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cms_project_keys", allEntries = true)
    public void deleteKey(@NonNull UUID keyId) {
        log.info("Deleting key: {}", keyId);
        
        if (!cmsKeyRepository.existsById(keyId)) {
            throw new ResourceNotFoundException("Key not found with ID: " + keyId);
        }
        cmsKeyRepository.deleteById(keyId);
    }

    // --- Helper Methods ---

    @Override
    public CmsKey getCmsKeyEntity(@NonNull UUID keyId) {
        return cmsKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResourceNotFoundException("Key not found with ID: " + keyId));
    }

    private Object extractValue(CmsKey cmsKey, String locale) {
        // Find matching locale translation
        String rawValue = cmsKey.getTranslations().stream()
                .filter(t -> t.getLocale().equalsIgnoreCase(locale))
                .findFirst()
                .map(t -> t.getValue())
                .orElse(cmsKey.getDefaultValue());

        // Process value based on Type
        if (rawValue == null) {
            return null;
        }

        try {
            switch (cmsKey.getValueType()) {
                case BOOLEAN:
                    return Boolean.parseBoolean(rawValue);
                case NUMBER:
                    return Double.parseDouble(rawValue);
                case LONG_TEXT:
                case TEXT:
                default:
                    return rawValue;
            }
        } catch (Exception e) {
            log.warn("Failed to parse value '{}' as {} for key '{}'", rawValue, cmsKey.getValueType(), cmsKey.getKey());
            // Fallback to string representation if parsing fails
            return rawValue;
        }
    }
}
