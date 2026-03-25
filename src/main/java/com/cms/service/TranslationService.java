package com.cms.service;

import com.cms.dto.request.BulkTranslationRequest;
import com.cms.dto.request.TranslationRequest;
import com.cms.dto.response.TranslationResponse;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

public interface TranslationService {
    TranslationResponse addOrUpdateTranslation(@NonNull UUID keyId, TranslationRequest request);
    List<TranslationResponse> bulkUpdateTranslations(@NonNull UUID projectId, String locale, BulkTranslationRequest request);
    List<TranslationResponse> getTranslations(@NonNull UUID keyId);
    void deleteTranslation(@NonNull UUID translationId);
}
