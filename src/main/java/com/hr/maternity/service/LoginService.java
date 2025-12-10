package com.hr.maternity.service;

import com.hr.maternity.dto.LoginRequest;
import com.hr.maternity.dto.LoginResponse;

public interface LoginService {

    /**
     * 用户登录验证
     * 
     * @param loginRequest 登录请求
     * @param skipRsaDecryption 是否跳过RSA解密
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest, boolean skipRsaDecryption);

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
