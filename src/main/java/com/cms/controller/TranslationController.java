package com.cms.controller;

import com.cms.dto.request.BulkTranslationRequest;
import com.cms.dto.request.TranslationRequest;
import com.cms.dto.response.CmsKeyValueResponse;
import com.cms.dto.response.TranslationResponse;
import com.cms.service.CmsKeyService;
import com.cms.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;
    private final CmsKeyService cmsKeyService;

    // --- Managing Translations ---

    @PostMapping("/keys/{keyId}/translations")
    @ResponseStatus(HttpStatus.OK)
    public TranslationResponse addOrUpdateTranslation(
            @PathVariable UUID keyId,
            @Valid @RequestBody TranslationRequest request) {
        return translationService.addOrUpdateTranslation(keyId, request);
    }

    @GetMapping("/keys/{keyId}/translations")
    public List<TranslationResponse> getTranslationsForKey(@PathVariable UUID keyId) {
        return translationService.getTranslations(keyId);
    }

    @DeleteMapping("/translations/{translationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTranslation(@PathVariable UUID translationId) {
        translationService.deleteTranslation(translationId);
    }

    @PostMapping("/projects/{projectId}/translations/bulk")
    @ResponseStatus(HttpStatus.OK)
    public List<TranslationResponse> bulkUpdateTranslations(
            @PathVariable UUID projectId,
            @RequestParam String locale,
            @Valid @RequestBody BulkTranslationRequest request) {
        return translationService.bulkUpdateTranslations(projectId, locale, request);
    }

    // --- Lookup / CDN Endpoints ---

    @GetMapping("/lookup/{projectCode}/{key}")
    public CmsKeyValueResponse getKeyValue(
            @PathVariable String projectCode,
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "en") String locale) {
        return cmsKeyService.getKeyValue(projectCode, key, locale);
    }

    @GetMapping("/lookup/{projectCode}")
    public Map<String, Object> getAllKeyValues(
            @PathVariable String projectCode,
            @RequestParam(required = false, defaultValue = "en") String locale) {
        return cmsKeyService.getAllKeyValues(projectCode, locale);
    }
}
