package com.cms.service;

import com.cms.domain.entity.CmsKey;
import com.cms.dto.request.CmsKeyRequest;
import com.cms.dto.response.CmsKeyResponse;
import com.cms.dto.response.CmsKeyValueResponse;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CmsKeyService {
    CmsKeyResponse createKey(@NonNull UUID projectId, CmsKeyRequest request);
    CmsKeyResponse updateKey(@NonNull UUID keyId, CmsKeyRequest request);
    CmsKeyResponse getKey(@NonNull UUID keyId);
    List<CmsKeyResponse> getKeysByProject(@NonNull UUID projectId, String category);
    CmsKeyValueResponse getKeyValue(String projectCode, String key, String locale);
    Map<String, Object> getAllKeyValues(String projectCode, String locale);
    CmsKeyResponse toggleKeyStatus(@NonNull UUID keyId);
    void deleteKey(@NonNull UUID keyId);
    CmsKey getCmsKeyEntity(@NonNull UUID keyId);
}
