package com.cms.dto.response;

import com.cms.domain.enums.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsKeyValueResponse {
    
    private String key;
    private Object value;
    private ValueType valueType;
}
