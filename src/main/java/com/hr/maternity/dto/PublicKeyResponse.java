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
@Schema(description = "RSA公钥响应DTO")
public class PublicKeyResponse {

    @Schema(description = "模数（Base64编码）", required = true)
    private String modulus;

    @Schema(description = "指数（Base64编码）", required = true)
    private String exponent;
}
