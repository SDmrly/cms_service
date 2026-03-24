package com.cms.service;

import com.cms.domain.entity.CmsKey;
import com.cms.domain.entity.Translation;
import com.cms.dto.request.BulkTranslationRequest;
import com.cms.dto.request.TranslationRequest;
import com.cms.dto.response.TranslationResponse;
import com.cms.exception.ResourceNotFoundException;
import com.cms.mapper.CmsKeyMapper;
import com.cms.repository.TranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationManager implements TranslationService {

    private final TranslationRepository translationRepository;
    private final CmsKeyService cmsKeyService;
    private final CmsKeyMapper cmsKeyMapper;

    @Override
    @Transactional
    public TranslationResponse addOrUpdateTranslation(UUID keyId, TranslationRequest request) {
        log.info("Adding/Updating translation for key: {}, locale: {}", keyId, request.getLocale());
        
        CmsKey cmsKey = cmsKeyService.getCmsKeyEntity(keyId);
        
        Optional<Translation> existingTranslationOpt = translationRepository.findByCmsKeyIdAndLocale(keyId, request.getLocale());
        
        Translation translation;
        if (existingTranslationOpt.isPresent()) {
            translation = existingTranslationOpt.get();
            translation.setValue(request.getValue());
        } else {
            translation = Translation.builder()
                    .cmsKey(cmsKey)
                    .locale(request.getLocale())
                    .value(request.getValue())
                    .build();
        }
        
        Translation savedTranslation = translationRepository.save(translation);
        return cmsKeyMapper.toTranslationResponse(savedTranslation);
    }

    @Override
    @Transactional
    public List<TranslationResponse> bulkUpdateTranslations(UUID projectId, String locale, BulkTranslationRequest request) {
        log.info("Bulk updating translations for project: {}, locale: {}", projectId, locale);
        
        List<TranslationResponse> responses = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : request.getTranslations().entrySet()) {
            String keyName = entry.getKey();
            String value = entry.getValue();
            
            try {
                // Find the core Key by project ID and Key Name
                Optional<CmsKey> cmsKeyOpt = cmsKeyService.getKeysByProject(projectId, null).stream()
                        .filter(k -> k.getKey().equals(keyName))
                        .findFirst()
                        .map(k -> cmsKeyService.getCmsKeyEntity(k.getId()));

                if (cmsKeyOpt.isEmpty()) {
                    log.warn("Key '{}' not found in project {}, skipping translation", keyName, projectId);
                    continue;
                }

                CmsKey cmsKey = cmsKeyOpt.get();

                Optional<Translation> existingOpt = translationRepository.findByCmsKeyIdAndLocale(cmsKey.getId(), locale);
                
                Translation translation;
                if (existingOpt.isPresent()) {
                    translation = existingOpt.get();
                    translation.setValue(value);
                } else {
                    translation = Translation.builder()
                            .cmsKey(cmsKey)
                            .locale(locale)
                            .value(value)
                            .build();
                }
                
                Translation saved = translationRepository.save(translation);
                responses.add(cmsKeyMapper.toTranslationResponse(saved));
                
            } catch (Exception e) {
                log.warn("Failed to process translation for key '{}': {}", keyName, e.getMessage());
            }
        }
        
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TranslationResponse> getTranslations(UUID keyId) {
        log.info("Fetching translations for key: {}", keyId);
        if (!cmsKeyService.getCmsKeyEntity(keyId).getId().equals(keyId)) {
            throw new ResourceNotFoundException("Key not found"); // Simple check
        }
        return cmsKeyMapper.toTranslationResponseList(translationRepository.findByCmsKeyId(keyId));
    }

    @Override
    @Transactional
    public void deleteTranslation(UUID translationId) {
        log.info("Deleting translation: {}", translationId);
        if (!translationRepository.existsById(translationId)) {
            throw new ResourceNotFoundException("Translation not found with ID: " + translationId);
        }
        translationRepository.deleteById(translationId);
    }
}
