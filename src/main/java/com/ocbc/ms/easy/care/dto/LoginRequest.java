package com.ocbc.ms.easy.care.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录请求DTO")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 256, message = "用户名长度不能超过256个字符")
    @Schema(description = "用户名", example = "AXXXXXXX", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 1024, message = "密码长度不能超过1024个字符")
    @Schema(description = "RSA加密后的密码（原始密码+nonce）", required = true)
    private String password;

    @NotBlank(message = "nonce不能为空")
    @Size(max = 256, message = "nonce长度不能超过256个字符")
    @Schema(description = "客户端生成的随机字符串，用于防止重放攻击", required = true)
    private String nonce;
}
