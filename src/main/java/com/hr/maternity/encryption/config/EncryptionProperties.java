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
     * 加密算法
     */
    private String algorithm = "AES";
}
