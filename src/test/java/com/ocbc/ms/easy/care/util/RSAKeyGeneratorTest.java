package com.ocbc.ms.easy.care.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * RSA密钥生成工具测试类
 * 用于从私钥生成公钥的modulus和exponent
 * 运行此测试以获取配置值
 */
@SpringBootTest
public class RSAKeyGeneratorTest {

    @Value("${encryption.rsa-private-key-pem:}")
    private String rsaPrivateKeyPem;

    @Test
    public void generatePublicKeyConfig() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("从私钥生成公钥配置");
        System.out.println("=".repeat(80));

        String privateKeyPEM = rsaPrivateKeyPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        if (!(privateKey instanceof RSAPrivateCrtKey)) {
            throw new RuntimeException("私钥不是RSA CRT格式");
        }

        RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey;
        BigInteger modulus = rsaPrivateKey.getModulus();
        BigInteger publicExponent = rsaPrivateKey.getPublicExponent();

        String modulusBase64 = Base64.getEncoder().encodeToString(modulus.toByteArray());
        String exponentBase64 = Base64.getEncoder().encodeToString(publicExponent.toByteArray());

        System.out.println("\n请将以下配置添加到 application-dev.properties:");
        System.out.println("-".repeat(80));
        System.out.println("encryption.rsa-public-modulus=" + modulusBase64);
        System.out.println("encryption.rsa-public-exponent=" + exponentBase64);
        System.out.println("-".repeat(80));
        System.out.println("\n模数长度: " + modulusBase64.length() + " 字符");
        System.out.println("指数长度: " + exponentBase64.length() + " 字符");
        System.out.println("指数值(十进制): " + publicExponent.toString(10));
        System.out.println("指数值(十六进制): " + publicExponent.toString(16));
        System.out.println("=".repeat(80));
    }
}
