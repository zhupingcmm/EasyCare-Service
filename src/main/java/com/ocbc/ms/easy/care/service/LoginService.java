package com.ocbc.ms.easy.care.service;

import com.ocbc.ms.easy.care.dto.LoginRequest;
import com.ocbc.ms.easy.care.dto.LoginResponse;

public interface LoginService {

    /**
     * 用户登录验证
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 刷新令牌
     */
    LoginResponse.TokenInfo refreshToken(String refreshToken);

    /**
     * 验证用户凭据（Mock实现）
     */
    boolean validateUserCredentials(String username, String password);
}
