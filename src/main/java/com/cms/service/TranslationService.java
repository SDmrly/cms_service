package com.cms.service;

import com.cms.dto.request.BulkTranslationRequest;
import com.cms.dto.request.TranslationRequest;
import com.cms.dto.response.TranslationResponse;

import java.util.List;
import java.util.UUID;

public interface TranslationService {
    TranslationResponse addOrUpdateTranslation(UUID keyId, TranslationRequest request);
    List<TranslationResponse> bulkUpdateTranslations(UUID projectId, String locale, BulkTranslationRequest request);
    List<TranslationResponse> getTranslations(UUID keyId);
    void deleteTranslation(UUID translationId);
}
