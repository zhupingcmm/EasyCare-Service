package com.hr.maternity.service.impl;

import com.hr.maternity.dto.LoginResponse;
import com.hr.maternity.entity.Token;
import com.hr.maternity.entity.User;
import com.hr.maternity.repository.TokenRepository;
import com.hr.maternity.repository.UserRepository;
import com.hr.maternity.service.JwtTokenService;
import com.hr.maternity.util.JwtUtil;
import com.hr.maternity.util.RandomGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Override
    @Transactional
    public LoginResponse.TokenInfo generateAndSaveToken(User user) {
        log.info("开始为用户生成JWT令牌，用户ID: {}", user.getId());

        try {
            // 先撤销用户的所有现有令牌
            revokeAllUserTokens(user.getId());

            // 生成新的JWT令牌
            String accessToken = jwtUtil.generateAccessToken(user.getLanId(), user.getId(), issuer, audience);
            String refreshToken = jwtUtil.generateRefreshToken(user.getLanId(), user.getId(), issuer, audience);

            // 生成操作令牌（短版本用于数据库存储和查询），使用安全随机Base64字符串
            String opAccToken = RandomGeneratorUtil.generateRandomBase64String(opaqueCodeLength);
            String opRefToken = RandomGeneratorUtil.generateRandomBase64String(opaqueCodeLength);

            // 计算并保存访问令牌过期时间
            LocalDateTime expTime = jwtUtil.getAccessTokenExpiry();
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

            log.info("JWT令牌生成成功，用户ID: {}", user.getId());
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
        log.info("开始刷新令牌");

        try {
            // 验证刷新令牌
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new RuntimeException("刷新令牌无效");
            }

            // 从刷新令牌中提取用户信息
            String userId = jwtUtil.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

            // 检查令牌是否在数据库中且未撤销
            Token tokenRecord = tokenRepository.findByOpRefTokenAndRevokedFalse(refreshToken)
                    .orElseThrow(() -> new RuntimeException("刷新令牌不存在或已撤销"));

            // 生成新的令牌
            return generateAndSaveToken(user);

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
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("令牌无效");
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        } catch (Exception e) {
            log.error("从令牌获取用户信息失败: {}", e.getMessage());
            throw new RuntimeException("从令牌获取用户信息失败: " + e.getMessage(), e);
        }
    }
}
