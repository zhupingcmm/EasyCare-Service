package com.ocbc.ms.easy.care.config;

import com.ocbc.ms.easy.care.entity.Token;
import com.ocbc.ms.easy.care.repository.TokenRepository;
import com.ocbc.ms.easy.care.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;

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
                String method = request.getMethod();
                log.debug("拦截请求: {} {}", method, uri);

                // 检查是否来自微信小程序
                String sourceId = request.getHeader("x-source-id");
                String appId = request.getHeader("x-app-id");
                log.debug("请求头信息: {} {}, x-source-id={}, x-app-id={}", method, uri, sourceId, appId);
                
                if ("wechat-miniprogram".equals(sourceId) && "wxd04c483b41ba7caf".equals(appId)) {
                    log.info("微信小程序请求放行: {} {}, appId={}", method, uri, appId);
                    request.setAttribute("fromMiniProgram", true);
                    request.setAttribute("wechatAppId", appId);
                    return true;
                }

                // 登录接口和健康检查接口直接放行
                if ("/api/auth/login".equals(uri)
                    || "/api/auth/generateNonce".equals(uri)
                    || "/api/auth/publicKey".equals(uri)
                    || "/health/alive".equals(uri)
                    || "/health/ready".equals(uri)
                    || "/health/info".equals(uri)
                    || "/actuator/health".equals(uri)
                    || "/actuator/info".equals(uri)) {
                    log.debug("公开接口放行: {} {}", method, uri);
                    return true;
                }

                String opHeader = request.getHeader("x-acc-op");
                if (opHeader == null || opHeader.isEmpty()) {
                    log.warn("请求缺少认证头: {} {}", method, uri);
                    writeUnauthorized(response, "未登录");
                    return false;
                }

                // 校验 token 是否存在且未被撤销、未过期
                Optional<Token> tokenOpt = tokenRepository.findByOpAccTokenAndRevokedFalse(opHeader);
                if (tokenOpt.isEmpty()) {
                    log.warn("Token不存在或已撤销: {} {}", method, uri);
                    writeUnauthorized(response, "未登录");
                    return false;
                }

                Token token = tokenOpt.get();
                if (token.getExpTime() != null && token.getExpTime().isBefore(LocalDateTime.now())) {
                    log.warn("Token已过期: {} {}, expTime={}", method, uri, token.getExpTime());
                    writeUnauthorized(response, "登录已过期");
                    return false;
                }

                // 从 acc_token(JWT) 中解析 lanId，并放入 request attribute 供后续使用
                try {
                    String lanId = extractLanIdFromToken(token);
                    if (lanId == null || lanId.isEmpty()) {
                        log.warn("无法从Token解析lanId: {} {}", method, uri);
                        writeUnauthorized(response, "未登录");
                        return false;
                    }
                    request.setAttribute("lanId", lanId);
                    log.debug("认证成功: {} {}, lanId={}", method, uri, lanId);
                } catch (Exception ex) {
                    log.error("Token解析失败: {} {}, error={}", method, uri, ex.getMessage());
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
