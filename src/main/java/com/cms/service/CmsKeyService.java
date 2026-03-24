package com.cms.service;

import com.cms.domain.entity.CmsKey;
import com.cms.dto.request.CmsKeyRequest;
import com.cms.dto.response.CmsKeyResponse;
import com.cms.dto.response.CmsKeyValueResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CmsKeyService {
    CmsKeyResponse createKey(UUID projectId, CmsKeyRequest request);
    CmsKeyResponse updateKey(UUID keyId, CmsKeyRequest request);
    CmsKeyResponse getKey(UUID keyId);
    List<CmsKeyResponse> getKeysByProject(UUID projectId, String category);
    CmsKeyValueResponse getKeyValue(String projectCode, String key, String locale);
    Map<String, Object> getAllKeyValues(String projectCode, String locale);
    CmsKeyResponse toggleKeyStatus(UUID keyId);
    void deleteKey(UUID keyId);
    CmsKey getCmsKeyEntity(UUID keyId);
}
