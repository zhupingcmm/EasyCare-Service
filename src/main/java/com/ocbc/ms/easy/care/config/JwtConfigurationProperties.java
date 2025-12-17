package com.ocbc.ms.easy.care.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfigurationProperties {

    private String secret = "mySecretKey12345678901234567890123456789012";
    
    private String algorithm = "RS256";
    
    private String issuer = "HR";
    
    private String audience = "OCBC";
    
    private AccessToken accessToken = new AccessToken();
    
    private RefreshToken refreshToken = new RefreshToken();
    
    @Data
    public static class AccessToken {
        private Long expiration = 600L;
    }
    
    @Data
    public static class RefreshToken {
        private Long expiration = 600L;
    }
}
