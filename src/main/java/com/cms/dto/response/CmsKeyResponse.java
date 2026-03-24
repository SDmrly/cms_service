package com.cms.dto.response;

import com.cms.domain.enums.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsKeyResponse {

    private UUID id;
    private String key;
    private ValueType valueType;
    private String defaultValue;
    private String category;
    private String description;
    private boolean active;
    private List<TranslationResponse> translations;
    private LocalDateTime createdAt;
}
