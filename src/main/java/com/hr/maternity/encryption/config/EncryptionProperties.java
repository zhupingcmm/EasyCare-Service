package com.hr.maternity.encryption.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 加密配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "encryption")
public class EncryptionProperties {

    /**
     * 加密密钥
     */
    private String secretKey = "MySecretKey12345";

    /**
     * RSA 公钥 PEM 内容（当 algorithm=RSA 时使用）
     */
    private String rsaPublicKeyPem = "-----BEGIN PUBLIC KEY-----CHANGE_ME_PUBLIC_KEY-----END PUBLIC KEY-----";

    /**
     * RSA 私钥 PEM 内容（当 algorithm=RSA 时使用）
     */
    private String rsaPrivateKeyPem = "-----BEGIN PRIVATE KEY-----CHANGE_ME_PRIVATE_KEY-----END PRIVATE KEY-----";
}
