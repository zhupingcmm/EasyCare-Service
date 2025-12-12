package com.ocbc.ms.easy.care.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应DTO")
public class LoginResponse {

    @Schema(description = "令牌信息")
    private TokenInfo tokenInfo;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "令牌信息")
    public static class TokenInfo {
        
        @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String accessToken;

        @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String refreshToken;

        @Schema(description = "令牌类型", example = "Bearer")
        private String tokenType = "Bearer";

        @Schema(description = "访问令牌过期时间")
        private LocalDateTime accessTokenExpiry;

        @Schema(description = "刷新令牌过期时间")
        private LocalDateTime refreshTokenExpiry;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfo {
        
        @Schema(description = "用户ID")
        private String userId;
        
        @Schema(description = "LAN账号")
        private String lanId;
        
        @Schema(description = "用户名")
        private String userName;
        
        @Schema(description = "显示名称")
        private String displayName;
        
        @Schema(description = "邮箱地址")
        private String email;
        
        @Schema(description = "用户角色列表")
        private List<RoleInfo> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色信息")
    public static class RoleInfo {
        
        @Schema(description = "角色ID")
        private Integer roleId;
        
        @Schema(description = "角色名称")
        private String roleName;
        
        @Schema(description = "规范化角色名称")
        private String normalizedName;
    }
}
