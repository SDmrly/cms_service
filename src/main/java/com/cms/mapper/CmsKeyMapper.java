package com.cms.mapper;

import com.cms.domain.entity.CmsKey;
import com.cms.domain.entity.Translation;
import com.cms.dto.request.CmsKeyRequest;
import com.cms.dto.response.CmsKeyResponse;
import com.cms.dto.response.TranslationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CmsKeyMapper {

    public CmsKeyResponse toResponse(CmsKey entity) {
        if (entity == null) {
            return null;
        }

        return CmsKeyResponse.builder()
                .id(entity.getId())
                .key(entity.getKey())
                .valueType(entity.getValueType())
                .defaultValue(entity.getDefaultValue())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .translations(toTranslationResponseList(entity.getTranslations()))
                .build();
    }

    public List<CmsKeyResponse> toResponseList(List<CmsKey> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CmsKey toEntity(CmsKeyRequest request) {
        if (request == null) {
            return null;
        }

        return CmsKey.builder()
                .key(request.getKey())
                .valueType(request.getValueType())
                .defaultValue(request.getDefaultValue())
                .category(request.getCategory())
                .description(request.getDescription())
                .active(true)
                .build();
    }

    public void updateEntityFromRequest(CmsKeyRequest request, CmsKey entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setKey(request.getKey());
        entity.setValueType(request.getValueType());
        entity.setDefaultValue(request.getDefaultValue());
        entity.setCategory(request.getCategory());
        entity.setDescription(request.getDescription());
    }

    public TranslationResponse toTranslationResponse(Translation entity) {
        if (entity == null) {
            return null;
        }

        return TranslationResponse.builder()
                .id(entity.getId())
                .locale(entity.getLocale())
                .value(entity.getValue())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<TranslationResponse> toTranslationResponseList(List<Translation> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toTranslationResponse)
                .collect(Collectors.toList());
    }
}
