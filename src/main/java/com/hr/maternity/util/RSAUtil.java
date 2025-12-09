package com.hr.maternity.util;

import com.hr.maternity.dto.LoginRequest;
import com.hr.maternity.entity.Nonce;
import com.hr.maternity.repository.NonceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RSAUtil {

    private final NonceRepository nonceRepository;

    @Value("${encryption.rsa-private-key-pem:}")
    private String rsaPrivateKeyPem;

    @Value("${encryption.rsa-public-modulus:}")
    private String rsaPublicModulus;

    @Value("${encryption.rsa-public-exponent:}")
    private String rsaPublicExponent;

    @Value("${encryption.nonce-separator:XXX_Z123}")
    private String nonceSeparator;

    @Value("${encryption.nonce-expiration-minutes:5}")
    private int nonceExpirationMinutes;

    @Value("${encryption.nonce-byte-length:10}")
    private int nonceByteLength;

    @Value("${encryption.rsa-algorithm:RSA}")
    private String rsaAlgorithm;

    @Value("${encryption.rsa-transformation:RSA/ECB/PKCS1Padding}")
    private String rsaTransformation;

    @Value("${encryption.rsa-padding:PKCS1}")
    private String rsaPadding;

    @Value("${encryption.rsa-oaep-hash:SHA-256}")
    private String rsaOaepHash;

    @Value("${encryption.rsa-oaep-mgf:MGF1}")
    private String rsaOaepMgf;

    @Value("${encryption.rsa-oaep-mgf-hash:SHA-256}")
    private String rsaOaepMgfHash;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成nonce
     * 
     * @param userId 用户ID
     * @return 生成的nonce值
     */
    @Transactional
    public String generateNonce(String userId) {
        log.info("为用户生成nonce，用户ID: {}", userId);

        nonceRepository.findByNonceValueAndUserId(userId, userId)
            .ifPresent(existingNonce -> {
                if (!existingNonce.getUsed() && existingNonce.getExpiresAt().isAfter(LocalDateTime.now())) {
                    log.debug("用户已有未使用的nonce，删除旧nonce，用户ID: {}", userId);
                    nonceRepository.delete(existingNonce);
                }
            });

        String nonceValue = generateSecureNonce();
        
        Nonce nonce = Nonce.builder()
            .nonceValue(nonceValue)
            .userId(userId)
            .used(false)
            .expiresAt(LocalDateTime.now().plusMinutes(nonceExpirationMinutes))
            .build();

        nonceRepository.save(nonce);
        log.info("nonce生成成功，用户ID: {}, nonce: {}, 过期时间: {}", userId, nonceValue, nonce.getExpiresAt());

        return nonceValue;
    }

    /**
     * 生成安全的随机nonce字符串（十六进制格式）
     * 
     * @return 随机nonce字符串（十六进制）
     */
    private String generateSecureNonce() {
        byte[] randomBytes = new byte[nonceByteLength];
        SECURE_RANDOM.nextBytes(randomBytes);
        String hexNonce = bytesToHex(randomBytes);
        log.debug("生成nonce，字节长度: {}, 十六进制长度: {}", nonceByteLength, hexNonce.length());
        return hexNonce;
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 解密登录请求中的密码
     * 前端加密流程：密码 → Base64编码 → RSA加密
     * 后端解密流程：RSA解密 → Base64解码 → 密码
     * 
     * @param loginRequest 登录请求对象
     * @return 解密后的原始密码
     * @throws RuntimeException 解密失败或nonce验证失败时抛出异常
     */
    @Transactional
    public String decryptLogin(LoginRequest loginRequest) {
        log.info("开始解密登录请求，用户名: {}", loginRequest.getUsername());

        try {
            String encryptedPassword = loginRequest.getPassword();
            String nonce = loginRequest.getNonce();

            validateNonce(loginRequest.getUsername(), nonce);

            String decryptedPassword = decryptWithPrivateKey(encryptedPassword);
            log.info("密码解密成功，用户名: {}", loginRequest.getUsername());

            markNonceAsUsed(loginRequest.getUsername(), nonce);

            return decryptedPassword;

        } catch (Exception e) {
            log.error("解密登录请求失败，用户名: {}", loginRequest.getUsername(), e);
            throw new RuntimeException("密码解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证nonce是否有效
     * 
     * @param userId 用户ID
     * @param nonce nonce值
     * @throws RuntimeException nonce验证失败时抛出异常
     */
    @Transactional
    public void validateNonce(String userId, String nonce) {
        log.debug("开始验证nonce，用户ID: {}, nonce: {}", userId, nonce);

        if (nonce == null || nonce.isBlank()) {
            log.warn("nonce为空，用户ID: {}", userId);
            throw new RuntimeException("nonce不能为空");
        }

        Optional<Nonce> existingNonce = nonceRepository.findByNonceValueAndUserId(nonce, userId);

        if (existingNonce.isPresent()) {
            Nonce nonceEntity = existingNonce.get();

            if (nonceEntity.getUsed()) {
                log.warn("nonce已被使用，防止重放攻击，用户ID: {}, nonce: {}", userId, nonce);
                throw new RuntimeException("nonce已被使用，请重新登录");
            }

            if (nonceEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("nonce已过期，用户ID: {}, nonce: {}, 过期时间: {}", 
                    userId, nonce, nonceEntity.getExpiresAt());
                throw new RuntimeException("nonce已过期，请重新登录");
            }

            log.debug("nonce验证通过，用户ID: {}", userId);
        } else {
            log.debug("nonce不存在，创建新nonce记录，用户ID: {}", userId);
            createNonceRecord(userId, nonce);
        }
    }

    /**
     * 使用RSA私钥解密数据
     * 
     * @param encryptedData Base64编码的加密数据
     * @return 解密后的字符串
     * @throws Exception 解密失败时抛出异常
     */
    private String decryptWithPrivateKey(String encryptedData) throws Exception {
        PrivateKey privateKey = getPrivateKey();
        
        if (privateKey instanceof RSAPrivateCrtKey) {
            RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey;
            log.info("=== RSA密钥信息 ===");
            log.info("密钥长度: {} bits", rsaPrivateKey.getModulus().bitLength());
            log.info("Modulus (Hex前32字符): {}", rsaPrivateKey.getModulus().toString(16).substring(0, 32).toUpperCase());
            log.info("Public Exponent: {}", rsaPrivateKey.getPublicExponent().toString(16).toUpperCase());
        }
        
        log.info("=== RSA解密配置 ===");
        log.info("Transformation: {}", rsaTransformation);
        log.info("Padding: {}", rsaPadding);
        log.info("OAEP Hash: {}", rsaOaepHash);
        log.info("OAEP MGF: {}", rsaOaepMgf);
        log.info("OAEP MGF Hash: {}", rsaOaepMgfHash);
        
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        log.info("加密数据长度: {} bytes, Base64长度: {}", encryptedBytes.length, encryptedData.length());
        log.debug("加密数据(前50字符): {}", encryptedData.substring(0, Math.min(50, encryptedData.length())));
        
        Cipher cipher = Cipher.getInstance(rsaTransformation);
        
        if ("OAEP".equalsIgnoreCase(rsaPadding)) {
            MGF1ParameterSpec mgfSpec = getMgfParameterSpec(rsaOaepMgfHash);
            
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                rsaOaepHash,
                rsaOaepMgf,
                mgfSpec,
                PSource.PSpecified.DEFAULT
            );
            
            log.info("初始化OAEP解密器");
            cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
        } else {
            log.info("使用默认填充模式: {}", rsaPadding);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        }
        
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        log.info("RSA解密成功，解密数据长度: {} bytes", decryptedBytes.length);
        
        String base64EncodedPassword = new String(decryptedBytes, StandardCharsets.UTF_8);

        return new String(Base64.getDecoder().decode(base64EncodedPassword), StandardCharsets.UTF_8);
    }

    /**
     * 根据Hash算法名称获取对应的MGF1ParameterSpec
     * 
     * @param hashAlgorithm Hash算法名称
     * @return MGF1ParameterSpec对象
     */
    private MGF1ParameterSpec getMgfParameterSpec(String hashAlgorithm) {
        return switch (hashAlgorithm.toUpperCase()) {
            case "SHA-1", "SHA1" -> MGF1ParameterSpec.SHA1;
            case "SHA-224", "SHA224" -> MGF1ParameterSpec.SHA224;
            case "SHA-256", "SHA256" -> MGF1ParameterSpec.SHA256;
            case "SHA-384", "SHA384" -> MGF1ParameterSpec.SHA384;
            case "SHA-512", "SHA512" -> MGF1ParameterSpec.SHA512;
            default -> {
                log.warn("不支持的MGF Hash算法: {}, 使用默认SHA-256", hashAlgorithm);
                yield MGF1ParameterSpec.SHA256;
            }
        };
    }

    /**
     * 从PEM格式的私钥字符串中获取PrivateKey对象
     * 
     * @return PrivateKey对象
     * @throws Exception 解析失败时抛出异常
     */
    private PrivateKey getPrivateKey() throws Exception {
        String privateKeyPEM = rsaPrivateKeyPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(rsaAlgorithm);
        
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 从解密后的数据中提取原始密码
     * 解密结果格式为: <password><nonceSeparator><nonce>
     * 
     * @param decryptedData 解密后的数据
     * @param expectedNonce 期望的nonce值
     * @return 原始密码
     * @throws RuntimeException 格式错误或nonce不匹配时抛出异常
     */
    private String extractPasswordFromDecryptedData(String decryptedData, String expectedNonce) {
        if (!decryptedData.contains(nonceSeparator)) {
            log.error("解密数据格式错误，缺少nonce分隔符: {}", nonceSeparator);
            throw new RuntimeException("解密数据格式错误");
        }

        int separatorIndex = decryptedData.lastIndexOf(nonceSeparator);
        String password = decryptedData.substring(0, separatorIndex);
        String actualNonce = decryptedData.substring(separatorIndex + nonceSeparator.length());

        if (!expectedNonce.equals(actualNonce)) {
            log.error("nonce不匹配，期望: {}, 实际: {}", expectedNonce, actualNonce);
            throw new RuntimeException("nonce验证失败");
        }

        return password;
    }

    /**
     * 创建nonce记录
     * 
     * @param userId 用户ID
     * @param nonceValue nonce值
     */
    private void createNonceRecord(String userId, String nonceValue) {
        Nonce nonce = Nonce.builder()
            .nonceValue(nonceValue)
            .userId(userId)
            .used(false)
            .expiresAt(LocalDateTime.now().plusMinutes(nonceExpirationMinutes))
            .build();

        nonceRepository.save(nonce);
        log.debug("nonce记录已创建，用户ID: {}, 过期时间: {}", userId, nonce.getExpiresAt());
    }

    /**
     * 标记nonce为已使用
     * 
     * @param userId 用户ID
     * @param nonceValue nonce值
     */
    private void markNonceAsUsed(String userId, String nonceValue) {
        Optional<Nonce> nonceOpt = nonceRepository.findByNonceValueAndUserId(nonceValue, userId);
        
        if (nonceOpt.isPresent()) {
            Nonce nonce = nonceOpt.get();
            nonce.setUsed(true);
            nonce.setUsedAt(LocalDateTime.now());
            nonceRepository.save(nonce);
            log.debug("nonce已标记为已使用，用户ID: {}, nonce: {}", userId, nonceValue);
        }
    }

    /**
     * 清理过期的nonce记录
     * 
     * @return 删除的记录数
     */
    @Transactional
    public int cleanupExpiredNonces() {
        log.info("开始清理过期的nonce记录");
        int deletedCount = nonceRepository.deleteExpiredNonces(LocalDateTime.now());
        log.info("已清理{}条过期的nonce记录", deletedCount);
        return deletedCount;
    }

    /**
     * 清理已使用的nonce记录（超过指定天数）
     * 
     * @param daysToKeep 保留天数
     * @return 删除的记录数
     */
    @Transactional
    public int cleanupUsedNonces(int daysToKeep) {
        log.info("开始清理已使用的nonce记录，保留天数: {}", daysToKeep);
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysToKeep);
        int deletedCount = nonceRepository.deleteUsedNonces(threshold);
        log.info("已清理{}条已使用的nonce记录", deletedCount);
        return deletedCount;
    }

    /**
     * 获取公钥信息（从配置文件读取）
     * 
     * @return 包含modulus和exponent的Map
     * @throws RuntimeException 配置缺失时抛出异常
     */
    public Map<String, String> getPublicKey() {
        log.debug("从配置文件获取公钥信息");
        
        if (rsaPublicModulus == null || rsaPublicModulus.isEmpty()) {
            log.error("RSA公钥模数未配置，请在配置文件中设置 encryption.rsa-public-modulus");
            throw new RuntimeException("RSA公钥模数未配置");
        }
        
        if (rsaPublicExponent == null || rsaPublicExponent.isEmpty()) {
            log.error("RSA公钥指数未配置，请在配置文件中设置 encryption.rsa-public-exponent");
            throw new RuntimeException("RSA公钥指数未配置");
        }
        
        Map<String, String> publicKeyInfo = new HashMap<>();
        publicKeyInfo.put("modulusHex", rsaPublicModulus);
        publicKeyInfo.put("exponentHex", rsaPublicExponent);
        publicKeyInfo.put("modulusBase64", rsaPublicModulus);
        publicKeyInfo.put("exponentBase64", rsaPublicExponent);
        
        log.info("公钥信息获取成功 - Modulus (Hex前32字符): {}", rsaPublicModulus.substring(0, 32));
        log.info("公钥信息获取成功 - Exponent (Hex): {}", rsaPublicExponent);
        return publicKeyInfo;
    }

    /**
     * 从私钥中提取公钥信息（modulus和exponent）
     * 仅用于生成配置文件中的公钥值，不应在运行时频繁调用
     * 
     * @return 包含modulus和exponent的Map
     * @throws RuntimeException 提取失败时抛出异常
     */
    public Map<String, String> extractPublicKeyFromPrivateKey() {
        log.warn("正在从私钥提取公钥信息，此操作应仅在配置更新时使用");
        
        try {
            PrivateKey privateKey = getPrivateKey();
            
            if (!(privateKey instanceof RSAPrivateCrtKey)) {
                throw new RuntimeException("私钥不是RSA CRT格式，无法提取公钥信息");
            }
            
            RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey;
            
            BigInteger modulus = rsaPrivateKey.getModulus();
            BigInteger publicExponent = rsaPrivateKey.getPublicExponent();
            
            int keySize = modulus.bitLength();
            if (keySize < 2048) {
                log.warn("密钥长度 {} 位小于推荐的 2048 位", keySize);
            }
            
            String modulusHex = modulus.toString(16).toUpperCase();
            String exponentHex = publicExponent.toString(16).toUpperCase();
            
            Map<String, String> publicKeyInfo = new HashMap<>();
            publicKeyInfo.put("modulus", modulusHex);
            publicKeyInfo.put("exponent", exponentHex);
            publicKeyInfo.put("modulusBase64", Base64.getEncoder().encodeToString(modulus.toByteArray()));
            publicKeyInfo.put("exponentBase64", Base64.getEncoder().encodeToString(publicExponent.toByteArray()));
            
            log.info("公钥信息提取成功，密钥长度: {} 位", keySize);
            log.debug("请将以下配置添加到配置文件:");
            log.debug("encryption.rsa-public-modulus={}", modulusHex);
            log.debug("encryption.rsa-public-exponent={}", exponentHex);
            
            return publicKeyInfo;
            
        } catch (Exception e) {
            log.error("从私钥提取公钥信息失败", e);
            throw new RuntimeException("提取公钥信息失败: " + e.getMessage(), e);
        }
    }
}
