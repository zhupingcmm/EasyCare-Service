package com.hr.maternity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nonce响应DTO")
public class NonceResponse {

    @Schema(description = "nonce值", example = "abc123xyz456", required = true)
    private String nonce;

    @Schema(description = "过期时间戳（毫秒）", example = "1702123456789", required = true)
    private Long expiresAt;

    @Schema(description = "有效期（秒）", example = "300", required = true)
    private Integer expiresIn;
}
