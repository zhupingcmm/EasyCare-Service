package com.ocbc.ms.easy.care.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "令牌验证响应")
public class TokenValidationResponse {
    
    @Schema(description = "令牌是否有效")
    private boolean valid;
    
    @Schema(description = "验证结果消息")
    private String message;
}
