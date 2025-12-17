package com.ocbc.ms.easy.care.util;

import com.ocbc.ms.easy.care.dto.LoginRequest;
import com.ocbc.ms.easy.care.encryption.config.EncryptionProperties;
import com.ocbc.ms.easy.care.entity.Nonce;
import com.ocbc.ms.easy.care.repository.NonceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RSAUtil {

    private final NonceRepository nonceRepository;
    private final EncryptionProperties encryptionProperties;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String HEX_DIGITS = "0123456789abcdef";
    private static final String PEM_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PEM_FOOTER = "-----END PRIVATE KEY-----";
    
    private PrivateKey cachedPrivateKey;

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
            .expiresAt(LocalDateTime.now().plusMinutes(encryptionProperties.getNonceExpirationMinutes()))
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
        byte[] randomBytes = new byte[encryptionProperties.getNonceByteLength()];
        SECURE_RANDOM.nextBytes(randomBytes);
        String hexNonce = bytesToHex(randomBytes);
        log.debug("生成nonce，字节长度: {}, 十六进制长度: {}", encryptionProperties.getNonceByteLength(), hexNonce.length());
        return hexNonce;
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hexString.append(HEX_DIGITS.charAt((b >> 4) & 0xF));
            hexString.append(HEX_DIGITS.charAt(b & 0xF));
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
        log.info("开始验证nonce，用户ID: {}, nonce: {}", userId, nonce);

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

            log.info("nonce验证通过，用户ID: {}", userId);
        } else {
            log.info("nonce不存在，创建新nonce记录，用户ID: {}", userId);
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
        PrivateKey privateKey = getOrLoadPrivateKey();
        
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        log.debug("RSA解密 - 加密数据长度: {} bytes", encryptedBytes.length);
        
        Cipher cipher = createDecryptCipher(privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        
        log.debug("RSA解密成功，Base64解码中...");
        String base64EncodedPassword = new String(decryptedBytes, StandardCharsets.UTF_8);
        return new String(Base64.getDecoder().decode(base64EncodedPassword), StandardCharsets.UTF_8);
    }
    
    /**
     * 创建解密Cipher对象
     * 
     * @param privateKey 私钥
     * @return 配置好的Cipher对象
     * @throws Exception 创建失败时抛出异常
     */
    private Cipher createDecryptCipher(PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(encryptionProperties.getRsaTransformation());
        
        if ("OAEP".equalsIgnoreCase(encryptionProperties.getRsaPadding())) {
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                encryptionProperties.getRsaOaepHash(),
                encryptionProperties.getRsaOaepMgf(),
                getMgfParameterSpec(encryptionProperties.getRsaOaepMgfHash()),
                PSource.PSpecified.DEFAULT
            );
            cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        }
        
        return cipher;
    }
    
    /**
     * 获取或加载私钥（带缓存）
     * 
     * @return PrivateKey对象
     * @throws Exception 加载失败时抛出异常
     */
    private synchronized PrivateKey getOrLoadPrivateKey() throws Exception {
        if (cachedPrivateKey == null) {
            cachedPrivateKey = loadPrivateKey();
            log.info("RSA私钥已加载并缓存");
        }
        return cachedPrivateKey;
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
     * 从PEM格式的私钥字符串中加载PrivateKey对象
     * 
     * @return PrivateKey对象
     * @throws Exception 解析失败时抛出异常
     */
    private PrivateKey loadPrivateKey() throws Exception {
        String privateKeyPEM = encryptionProperties.getRsaPrivateKeyPem()
            .replace(PEM_HEADER, "")
            .replace(PEM_FOOTER, "")
            .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(encryptionProperties.getRsaAlgorithm());
        
        return keyFactory.generatePrivate(keySpec);
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
            .expiresAt(LocalDateTime.now().plusMinutes(encryptionProperties.getNonceExpirationMinutes()))
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
        nonceRepository.findByNonceValueAndUserId(nonceValue, userId)
            .ifPresent(nonce -> {
                nonce.setUsed(true);
                nonce.setUsedAt(LocalDateTime.now());
                nonceRepository.save(nonce);
                log.info("nonce已标记为已使用，用户ID: {}", userId);
            });
    }

    /**
     * 清理过期的nonce记录（仅清理昨天及之前过期的数据）
     * 
     * @return 删除的记录数
     */
    @Transactional
    public int cleanupExpiredNonces() {
        LocalDateTime yesterdayEnd = LocalDateTime.now().toLocalDate().atStartOfDay();
        log.info("开始清理过期的nonce记录，清理阈值时间: {}", yesterdayEnd);
        int deletedCount = nonceRepository.deleteExpiredNonces(yesterdayEnd);
        log.info("已清理{}条过期的nonce记录（昨天及之前）", deletedCount);
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
        
        validatePublicKeyConfiguration();
        
        Map<String, String> publicKeyInfo = new HashMap<>();
        publicKeyInfo.put("modulusHex", encryptionProperties.getRsaPublicModulus());
        publicKeyInfo.put("exponentHex", encryptionProperties.getRsaPublicExponent());
        publicKeyInfo.put("modulusBase64", encryptionProperties.getRsaPublicModulus());
        publicKeyInfo.put("exponentBase64", encryptionProperties.getRsaPublicExponent());
        
        log.debug("公钥信息获取成功");
        return publicKeyInfo;
    }
    
    /**
     * 验证公钥配置是否完整
     * 
     * @throws RuntimeException 配置缺失时抛出异常
     */
    private void validatePublicKeyConfiguration() {
        if (encryptionProperties.getRsaPublicModulus() == null || encryptionProperties.getRsaPublicModulus().isEmpty()) {
            log.error("RSA公钥模数未配置");
            throw new RuntimeException("RSA公钥模数未配置，请在配置文件中设置 encryption.rsa-public-modulus");
        }
        
        if (encryptionProperties.getRsaPublicExponent() == null || encryptionProperties.getRsaPublicExponent().isEmpty()) {
            log.error("RSA公钥指数未配置");
            throw new RuntimeException("RSA公钥指数未配置，请在配置文件中设置 encryption.rsa-public-exponent");
        }
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
            PrivateKey privateKey = loadPrivateKey();
            
            if (!(privateKey instanceof RSAPrivateCrtKey rsaPrivateKey)) {
                throw new RuntimeException("私钥不是RSA CRT格式，无法提取公钥信息");
            }
            
            BigInteger modulus = rsaPrivateKey.getModulus();
            BigInteger publicExponent = rsaPrivateKey.getPublicExponent();
            
            int keySize = modulus.bitLength();
            if (keySize < encryptionProperties.getRsaMinKeySize()) {
                log.warn("密钥长度 {} 位小于推荐的 {} 位", keySize, encryptionProperties.getRsaMinKeySize());
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

    /**
     * 从公钥PEM中提取公钥信息（modulus和exponent）
     * 用于解析JWT公钥配置并提取公钥参数
     * 
     * @param publicKeyPem 公钥PEM格式字符串
     * @return 包含modulus和exponent的Map
     * @throws RuntimeException 提取失败时抛出异常
     */
    public Map<String, String> extractPublicKeyFromPem(String publicKeyPem) {
        log.info("开始从公钥PEM提取公钥信息");
        
        try {
            String normalized = publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(normalized);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);
            
            if (!(publicKey instanceof RSAPublicKey rsaPublicKey)) {
                throw new RuntimeException("不是有效的RSA公钥");
            }
            
            BigInteger modulus = rsaPublicKey.getModulus();
            BigInteger publicExponent = rsaPublicKey.getPublicExponent();
            
            int keySize = modulus.bitLength();
            
            String modulusHex = modulus.toString(16).toUpperCase();
            String exponentHex = publicExponent.toString(16).toUpperCase();
            
            Map<String, String> publicKeyInfo = new HashMap<>();
            publicKeyInfo.put("modulus", modulusHex);
            publicKeyInfo.put("exponent", exponentHex);
            publicKeyInfo.put("modulusBase64", Base64.getEncoder().encodeToString(modulus.toByteArray()));
            publicKeyInfo.put("exponentBase64", Base64.getEncoder().encodeToString(publicExponent.toByteArray()));
            publicKeyInfo.put("keySize", String.valueOf(keySize));
            
            log.info("公钥信息提取成功，密钥长度: {} 位", keySize);
            log.debug("Modulus (Hex): {}", modulusHex);
            log.debug("Exponent (Hex): {}", exponentHex);
            
            return publicKeyInfo;
            
        } catch (Exception e) {
            log.error("从公钥PEM提取信息失败", e);
            throw new RuntimeException("提取公钥信息失败: " + e.getMessage(), e);
        }
    }
}
