package com.cms.dto.request;

import com.cms.domain.enums.ValueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsKeyRequest {

    @NotBlank(message = "Key is required")
    private String key;

    @NotNull(message = "ValueType is required")
    private ValueType valueType;

    private String defaultValue;

    @NotBlank(message = "Category is required")
    private String category;

    private String description;
}
