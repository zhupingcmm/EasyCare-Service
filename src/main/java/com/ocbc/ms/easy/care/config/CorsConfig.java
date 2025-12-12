package com.ocbc.ms.easy.care.config;

import com.ocbc.ms.easy.care.entity.Token;
import com.ocbc.ms.easy.care.repository.TokenRepository;
import com.ocbc.ms.easy.care.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.security.login-validation-enabled:true}")
    private boolean loginValidationEnabled;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                String uri = request.getRequestURI();

                // 登录接口和健康检查接口直接放行
                if ("/api/auth/login".equals(uri)
                    || "/health/alive".equals(uri)
                    || "/health/ready".equals(uri)
                    || "/health/info".equals(uri)) {
                    return true;
                }

                // 如果登录校验未启用，直接放行
                if (!loginValidationEnabled) {
                    return true;
                }

                String opHeader = request.getHeader("x-acc-op");
                if (opHeader == null || opHeader.isEmpty()) {
                    writeUnauthorized(response, "未登录");
                    return false;
                }

                // 校验 token 是否存在且未被撤销、未过期
                Optional<Token> tokenOpt = tokenRepository.findByOpAccTokenAndRevokedFalse(opHeader);
                if (tokenOpt.isEmpty()) {
                    writeUnauthorized(response, "未登录");
                    return false;
                }

                Token token = tokenOpt.get();
                if (token.getExpTime() != null && token.getExpTime().isBefore(LocalDateTime.now())) {
                    writeUnauthorized(response, "登录已过期");
                    return false;
                }

                // 从 acc_token(JWT) 中解析 lanId，并放入 request attribute 供后续使用
                try {
                    String lanId = extractLanIdFromToken(token);
                    if (lanId == null || lanId.isEmpty()) {
                        writeUnauthorized(response, "未登录");
                        return false;
                    }
                    request.setAttribute("lanId", lanId);
                } catch (Exception ex) {
                    writeUnauthorized(response, "未登录");
                    return false;
                }

                return true;
            }

            private String extractLanIdFromToken(Token token) {
                String jwt = token.getAccToken();
                return jwtUtil.getLanIdFromToken(jwt);
            }

            private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                // 与全局 ApiResponse 结构保持一致
                String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
                response.getWriter().write(body);
            }
        });
    }
}
