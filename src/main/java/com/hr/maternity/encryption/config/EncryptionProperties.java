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
     * AES加密密钥
     */
    private String aesSecretKey = "MySecretKey12345";

    /**
     * RSA 公钥 PEM 内容
     */
    private String rsaPublicKeyPem = "-----BEGIN PUBLIC KEY-----CHANGE_ME_PUBLIC_KEY-----END PUBLIC KEY-----";

    /**
     * RSA 私钥 PEM 内容
     */
    private String rsaPrivateKeyPem = "";

    /**
     * RSA 公钥模数（Hex格式）
     */
    private String rsaPublicModulus = "";

    /**
     * RSA 公钥指数（Hex格式）
     */
    private String rsaPublicExponent = "";

    /**
     * Nonce过期时间（分钟）
     */
    private int nonceExpirationMinutes = 5;

    /**
     * Nonce字节长度
     */
    private int nonceByteLength = 10;

    /**
     * RSA算法
     */
    private String rsaAlgorithm = "RSA";

    /**
     * RSA转换方式
     */
    private String rsaTransformation = "RSA/ECB/PKCS1Padding";

    /**
     * RSA填充模式
     */
    private String rsaPadding = "PKCS1";

    /**
     * OAEP哈希算法
     */
    private String rsaOaepHash = "SHA-256";

    /**
     * OAEP MGF算法
     */
    private String rsaOaepMgf = "MGF1";

    /**
     * OAEP MGF哈希算法
     */
    private String rsaOaepMgfHash = "SHA-256";

    /**
     * RSA最小密钥长度（位）
     */
    private int rsaMinKeySize = 2048;
}
