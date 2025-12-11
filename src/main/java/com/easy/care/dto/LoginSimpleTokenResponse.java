package com.easy.care.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应简化信息")
public class LoginSimpleTokenResponse {

    @Schema(description = "令牌类型，例如 Bearer")
    private String tokenType;

    @Schema(description = "过期时间（秒）")
    private String expiresIn;
}
