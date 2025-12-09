package com.hr.maternity.encryption.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hr.maternity.encryption.config.EncryptionProperties;
import com.hr.maternity.encryption.exception.FieldEncryptionException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 通用 AES 字段级加解密转换器，支持多种数据类型。
 * 使用配置的密钥进行AES加密，然后Base64编码存储。
 */
@Slf4j
@Converter(autoApply = false)
@Component
@RequiredArgsConstructor
public class Base64AttributeConverter implements AttributeConverter<Object, String> {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private final EncryptionProperties encryptionProperties;

    private Cipher getCipher(int mode) throws Exception {
        // 获取密钥并确保是16字节(128位)或32字节(256位)
        String keyStr = encryptionProperties.getSecretKey();
        byte[] keyBytes = keyStr.getBytes(StandardCharsets.UTF_8);
        
        // 如果密钥长度不是16或32字节，进行填充或截断
        byte[] normalizedKey;
        if (keyBytes.length >= 32) {
            // 截断到32字节
            normalizedKey = new byte[32];
            System.arraycopy(keyBytes, 0, normalizedKey, 0, 32);
        } else if (keyBytes.length >= 16) {
            // 截断到16字节
            normalizedKey = new byte[16];
            System.arraycopy(keyBytes, 0, normalizedKey, 0, 16);
        } else {
            // 填充到16字节
            normalizedKey = new byte[16];
            System.arraycopy(keyBytes, 0, normalizedKey, 0, keyBytes.length);
            // 用0填充剩余字节
            for (int i = keyBytes.length; i < 16; i++) {
                normalizedKey[i] = 0;
            }
        }
        
        SecretKeySpec secretKey = new SecretKeySpec(normalizedKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, secretKey);
        return cipher;
    }

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            // 先序列化为JSON
            EncryptionPayload payload = new EncryptionPayload(attribute.getClass().getName(),
                    OBJECT_MAPPER.writeValueAsString(attribute));
            String payloadJson = OBJECT_MAPPER.writeValueAsString(payload);
            
            // 使用AES加密
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            byte[] encryptedBytes = cipher.doFinal(payloadJson.getBytes(StandardCharsets.UTF_8));
            
            // Base64编码存储
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception ex) {
            log.error("字段加密失败", ex);
            throw new FieldEncryptionException("字段加密失败", ex);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            // Base64解码
            byte[] encryptedBytes = Base64.getDecoder().decode(dbData);
            
            // 使用AES解密
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String payloadJson = new String(decryptedBytes, StandardCharsets.UTF_8);
            
            // 反序列化
            EncryptionPayload payload = OBJECT_MAPPER.readValue(payloadJson, EncryptionPayload.class);
            Class<?> targetClass = Class.forName(payload.type());
            return OBJECT_MAPPER.readValue(payload.jsonValue(), targetClass);
        } catch (ClassNotFoundException ex) {
            log.error("无法解析加密字段对应的类型", ex);
            throw new FieldEncryptionException("无法解析加密字段对应的类型", ex);
        } catch (Exception ex) {
            log.error("字段解密失败", ex);
            throw new FieldEncryptionException("字段解密失败", ex);
        }
    }
}
