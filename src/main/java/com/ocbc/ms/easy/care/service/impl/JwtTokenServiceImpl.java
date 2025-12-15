package com.ocbc.ms.easy.care.service.impl;

import com.ocbc.ms.easy.care.dto.LoginResponse;
import com.ocbc.ms.easy.care.entity.Token;
import com.ocbc.ms.easy.care.entity.User;
import com.ocbc.ms.easy.care.repository.TokenRepository;
import com.ocbc.ms.easy.care.repository.UserRepository;
import com.ocbc.ms.easy.care.service.JwtTokenService;
import com.ocbc.ms.easy.care.util.JwtUtil;
import com.ocbc.ms.easy.care.util.RandomGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.issuer:HR}")
    private String issuer;

    @Value("${jwt.audience:OCBC}")
    private String audience;

    @Value("${jwt.opaque-code-length:24}")
    private int opaqueCodeLength;

    @Value("${jwt.multi-device-login.enabled:false}")
    private boolean multiDeviceLoginEnabled;

    @Override
    @Transactional
    public LoginResponse.TokenInfo generateAndSaveToken(User user) {
        log.info("开始为用户生成JWT令牌，用户ID: {}，多设备登录: {}", user.getId(), multiDeviceLoginEnabled);

        try {
            // 根据配置决定是否支持多设备登录
            if (!multiDeviceLoginEnabled) {
                // 单设备模式：撤销用户的所有现有令牌
                revokeAllUserTokens(user.getId());
                log.info("单设备登录模式，已撤销用户所有现有令牌，用户ID: {}", user.getId());
            }

            // 生成新的JWT令牌
            String accessToken = jwtUtil.generateAccessToken(user.getLanId(), user.getId(), issuer, audience);
            String refreshToken = jwtUtil.generateRefreshToken(user.getLanId(), user.getId(), issuer, audience);

            // 生成操作令牌（短版本用于数据库存储和查询），使用安全随机Base64字符串
            String opAccToken = RandomGeneratorUtil.generateRandomBase64String(opaqueCodeLength);
            String opRefToken = RandomGeneratorUtil.generateRandomBase64String(opaqueCodeLength);

            // 统一使用较长的过期时间（支持滚动窗口刷新）
            LocalDateTime expTime = jwtUtil.getRefreshTokenExpiry();
            
            // 保存令牌到数据库
            Token token = Token.builder()
                    .id(UUID.randomUUID())
                    .userId(user.getId())
                    .opAccToken(opAccToken)
                    .opRefToken(opRefToken)
                    .accToken(accessToken)
                    .refToken(refreshToken)
                    .expTime(expTime)
                    .revoked(false)
                    .createdBy(user.getId())
                    .build();

            tokenRepository.save(token);

            // 构建响应对象：对外返回操作令牌（op_acc_token / op_ref_token），并使用刚持久化的过期时间
            LoginResponse.TokenInfo tokenInfo = LoginResponse.TokenInfo.builder()
                    .accessToken(opAccToken)
                    .refreshToken(opRefToken)
                    .tokenType("Bearer")
                    .accessTokenExpiry(token.getExpTime())
                    .refreshTokenExpiry(token.getExpTime())
                    .build();

            log.info("JWT令牌生成成功，用户ID: {}，模式: {}", user.getId(), 
                    multiDeviceLoginEnabled ? "多设备登录" : "单设备登录");
            return tokenInfo;

        } catch (Exception e) {
            log.error("生成JWT令牌失败，用户ID: {}", user.getId(), e);
            throw new RuntimeException("生成JWT令牌失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            log.error("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(String userId) {
        try {
            tokenRepository.revokeAllUserTokens(userId, LocalDateTime.now());
            log.info("撤销用户所有令牌成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("撤销用户令牌失败，用户ID: {}", userId, e);
            throw new RuntimeException("撤销用户令牌失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public LoginResponse.TokenInfo refreshToken(String refreshToken) {
        log.info("开始刷新令牌，实施滚动窗口 Token 晋升策略");

        try {
            // 步骤 1: 验证 opaque refresh token 在数据库中的存在性和有效性
            Token oldTokenRecord = tokenRepository.findByOpRefTokenAndRevokedFalse(refreshToken)
                    .orElseThrow(() -> {
                        log.warn("刷新令牌不存在或已被撤销");
                        return new RuntimeException("刷新令牌无效或已过期");
                    });

            // 步骤 2: 检查 Token 是否过期
            if (oldTokenRecord.getExpTime() != null && 
                oldTokenRecord.getExpTime().isBefore(LocalDateTime.now())) {
                log.warn("令牌已过期，用户ID: {}", oldTokenRecord.getUserId());
                throw new RuntimeException("令牌已过期，请重新登录");
            }

            // 步骤 3: 验证底层 JWT 签名
            if (!jwtUtil.validateToken(oldTokenRecord.getRefToken())) {
                log.warn("Refresh Token JWT 签名验证失败，用户ID: {}", oldTokenRecord.getUserId());
                throw new RuntimeException("令牌签名无效");
            }

            // 步骤 4: 撤销旧的 Token 记录
            oldTokenRecord.setRevoked(true);
            oldTokenRecord.setUpdatedAt(LocalDateTime.now());
            tokenRepository.save(oldTokenRecord);
            log.info("旧 Token 记录已撤销，Token ID: {}", oldTokenRecord.getId());

            // 步骤 5: 查找用户
            User user = userRepository.findById(oldTokenRecord.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + oldTokenRecord.getUserId()));

            // 步骤 6: 传入的 refreshToken（op_ref_token）晋升为新的 accessToken（op_acc_token）
            // 传入的 refreshToken 直接成为新的 accessToken

            // 步骤 7: 对应的 JWT 也同步晋升：旧的 refToken JWT 成为新的 accToken JWT
            String newAccToken = oldTokenRecord.getRefToken();
            
            // 步骤 8: 生成全新的 op_ref_token
            String newOpRefToken = RandomGeneratorUtil.generateRandomBase64String(opaqueCodeLength);
            
            // 步骤 9: 生成全新的 ref_token JWT
            String newRefToken = jwtUtil.generateRefreshToken(user.getLanId(), user.getId(), issuer, audience);
            
            // 步骤 10: 延续过期时间（滚动窗口）
            LocalDateTime newExpTime = jwtUtil.getRefreshTokenExpiry();
            
            // 步骤 11: 保存新的 Token 记录
            Token newTokenRecord = Token.builder()
                    .id(UUID.randomUUID())
                    .userId(user.getId())
                    .opAccToken(refreshToken)      // 传入的 refreshToken 晋升为 accessToken
                    .opRefToken(newOpRefToken)      // 新生成的 refreshToken
                    .accToken(newAccToken)          // 旧的 refToken JWT 晋升为 accToken JWT
                    .refToken(newRefToken)          // 新生成的 refToken JWT
                    .expTime(newExpTime)
                    .revoked(false)
                    .createdBy(user.getId())
                    .build();
            
            tokenRepository.save(newTokenRecord);
            
            // 步骤 12: 构建响应
            LoginResponse.TokenInfo tokenInfo = LoginResponse.TokenInfo.builder()
                    .accessToken(refreshToken)     // 返回晋升后的 accessToken（原 refreshToken）
                    .refreshToken(newOpRefToken)    // 返回新生成的 refreshToken
                    .tokenType("Bearer")
                    .accessTokenExpiry(newTokenRecord.getExpTime())
                    .refreshTokenExpiry(newTokenRecord.getExpTime())
                    .build();
            
            log.info("令牌刷新成功，用户ID: {}，传入的 refreshToken 已晋升为新 accessToken", user.getId());
            return tokenInfo;

        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw new RuntimeException("刷新令牌失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void cleanExpiredTokens() {
        try {
            tokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("清理过期令牌完成");
        } catch (Exception e) {
            log.error("清理过期令牌失败: {}", e.getMessage());
        }
    }

    @Override
    public User getUserFromToken(String token) {
        try {
            String userId = jwtUtil.getUserIdFromToken(token);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));
        } catch (Exception e) {
            log.error("从令牌获取用户信息失败: {}", e.getMessage());
            throw new RuntimeException("从令牌获取用户信息失败: " + e.getMessage(), e);
        }
    }
}
