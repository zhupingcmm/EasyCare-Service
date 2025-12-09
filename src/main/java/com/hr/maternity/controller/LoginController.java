package com.hr.maternity.controller;

import com.hr.maternity.common.ApiResponse;
import com.hr.maternity.dto.LoginRequest;
import com.hr.maternity.dto.LoginResponse;
import com.hr.maternity.dto.LoginSimpleTokenResponse;
import com.hr.maternity.dto.NonceRequest;
import com.hr.maternity.dto.NonceResponse;
import com.hr.maternity.dto.PublicKeyResponse;
import com.hr.maternity.dto.RefreshTokenRequest;
import com.hr.maternity.dto.TokenValidationResponse;
import com.hr.maternity.service.LoginService;
import com.hr.maternity.util.RSAUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、登出、令牌刷新等认证相关接口")
public class LoginController {

    private final LoginService loginService;
    private final RSAUtil rsaUtil;

    @Value("${jwt.access-token.expiration:600}")
    private int accessTokenExpirationSeconds;

    @Value("${encryption.nonce-expiration-minutes:5}")
    private int nonceExpirationMinutes;

    @Value("${app.dev.extract-key-enabled:false}")
    private boolean extractKeyEnabled;

    @GetMapping("/publicKey")
    @Operation(
        summary = "获取RSA公钥",
        description = "获取用于前端加密的RSA公钥信息（modulus和exponent）"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "公钥获取成功",
        content = @Content(schema = @Schema(implementation = com.hr.maternity.common.ApiResponse.class))
    )
    public ResponseEntity<ApiResponse<PublicKeyResponse>> getPublicKey() {
        log.info("收到获取公钥请求");

        java.util.Map<String, String> publicKeyInfo = rsaUtil.getPublicKey();

        PublicKeyResponse response = PublicKeyResponse.builder()
                .modulus(publicKeyInfo.get("modulusBase64"))
                .exponent(publicKeyInfo.get("exponentBase64"))
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/generateNonce")
    @Operation(
        summary = "生成nonce",
        description = "为用户生成用于加密登录的nonce值，防止重放攻击"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "nonce生成成功",
        content = @Content(schema = @Schema(implementation = com.hr.maternity.common.ApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "请求参数错误"
    )
    public ResponseEntity<ApiResponse<NonceResponse>> generateNonce(
            @Valid @RequestBody NonceRequest nonceRequest) {

        log.info("收到生成nonce请求，用户ID: {}", nonceRequest.getUsername());

        String nonce = rsaUtil.generateNonce(nonceRequest.getUsername());
        long expiresAtMillis = System.currentTimeMillis() + (nonceExpirationMinutes * 60 * 1000L);

        NonceResponse response = NonceResponse.builder()
                .nonce(nonce)
                .expiresAt(expiresAtMillis)
                .expiresIn(nonceExpirationMinutes * 60)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/login")
    @Operation(
        summary = "用户登录",
        description = "用户使用用户名和密码进行登录认证，成功后返回JWT令牌和用户信息"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "登录成功",
        content = @Content(schema = @Schema(implementation = com.hr.maternity.common.ApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400", 
        description = "请求参数错误"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401", 
        description = "用户名或密码错误"
    )
    public ResponseEntity<LoginSimpleTokenResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        log.info("收到登录请求，用户名: {}", loginRequest.getUsername());

        LoginResponse loginResponse = loginService.login(loginRequest);
        LoginResponse.TokenInfo tokenInfo = loginResponse.getTokenInfo();

        LoginSimpleTokenResponse bodyData = LoginSimpleTokenResponse.builder()
                .tokenType(tokenInfo.getTokenType())
                .expiresIn(String.valueOf(accessTokenExpirationSeconds))
                .build();

        return ResponseEntity
                .ok()
                .header("x-acc-op", tokenInfo.getAccessToken())
                .header("x-ref-token", tokenInfo.getRefreshToken())
                .body(bodyData);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "用户登出",
        description = "撤销用户的所有有效令牌，实现安全登出"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "登出成功"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400", 
        description = "令牌无效"
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "Authorization头中的Bearer令牌", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        log.info("收到登出请求");
        
        String token = extractTokenFromAuthorization(authorization);
        loginService.logout(token);
        
        return ResponseEntity.ok(ApiResponse.success(null, "登出成功"));
    }

    @PostMapping("/refresh-token")
    @Operation(
        summary = "刷新令牌",
        description = "使用刷新令牌获取新的访问令牌"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "刷新成功",
        content = @Content(schema = @Schema(implementation = com.hr.maternity.common.ApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400", 
        description = "刷新令牌无效或已过期"
    )
    public ResponseEntity<ApiResponse<LoginResponse.TokenInfo>> refreshToken(
            @Parameter(description = "刷新令牌", required = true)
            @RequestBody RefreshTokenRequest refreshTokenRequest) {
        
        log.info("收到令牌刷新请求");
        
        LoginResponse.TokenInfo tokenInfo = loginService.refreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(tokenInfo));
    }

    @GetMapping("/validate-token")
    @Operation(
        summary = "验证令牌",
        description = "验证JWT令牌是否有效"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "令牌有效"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401", 
        description = "令牌无效或已过期"
    )
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(
            @Parameter(description = "Authorization头中的Bearer令牌", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        log.info("收到令牌验证请求");
        
        String token = extractTokenFromAuthorization(authorization);
        
        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(true)
                .message("令牌有效")
                .build();
                
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/dev/extract-public-key")
    @Operation(
        summary = "从私钥提取公钥信息（开发环境专用）",
        description = "从配置的RSA私钥中提取公钥的modulus和exponent，用于更新配置文件。此接口默认关闭，仅在开发环境启用。"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "公钥提取成功",
        content = @Content(schema = @Schema(implementation = com.hr.maternity.common.ApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "此接口在当前环境中已禁用"
    )
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> extractPublicKeyFromPrivateKey() {
        log.warn("收到提取公钥请求（开发环境专用接口）");

        if (!extractKeyEnabled) {
            log.error("提取公钥接口已禁用，请在配置文件中设置 app.dev.extract-key-enabled=true");
            return ResponseEntity
                    .status(403)
                    .body(ApiResponse.error(403, "此接口在当前环境中已禁用，请在配置文件中启用"));
        }

        try {
            java.util.Map<String, String> publicKeyInfo = rsaUtil.extractPublicKeyFromPrivateKey();
            
            log.info("公钥提取成功");
            log.info("请将以下配置更新到 application.properties:");
            log.info("encryption.rsa-public-modulus={}", publicKeyInfo.get("modulusBase64"));
            log.info("encryption.rsa-public-exponent={}", publicKeyInfo.get("exponentBase64"));
            
            return ResponseEntity.ok(ApiResponse.success(publicKeyInfo, "公钥提取成功，请查看日志获取配置信息"));
            
        } catch (Exception e) {
            log.error("提取公钥失败", e);
            return ResponseEntity
                    .status(500)
                    .body(ApiResponse.error(500, "提取公钥失败: " + e.getMessage()));
        }
    }

    /**
     * 从Authorization头中提取令牌（当前直接返回，未来可扩展Bearer前缀处理）
     */
    private String extractTokenFromAuthorization(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new RuntimeException("Authorization头不能为空");
        }
        return authorization.trim();
    }

}
