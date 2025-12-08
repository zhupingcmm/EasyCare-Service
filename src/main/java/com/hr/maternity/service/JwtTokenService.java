package com.hr.maternity.service;

import com.hr.maternity.dto.LoginResponse;
import com.hr.maternity.entity.Token;
import com.hr.maternity.entity.User;

public interface JwtTokenService {

    /**
     * 为用户生成JWT令牌并存储到数据库
     */
    LoginResponse.TokenInfo generateAndSaveToken(User user);

    /**
     * 验证JWT令牌
     */
    boolean validateToken(String token);

    /**
     * 撤销用户的所有令牌
     */
    void revokeAllUserTokens(String userId);

    /**
     * 刷新令牌
     */
    LoginResponse.TokenInfo refreshToken(String refreshToken);

    /**
     * 清理过期令牌
     */
    void cleanExpiredTokens();

    /**
     * 根据访问令牌获取用户信息
     */
    User getUserFromToken(String token);
}
