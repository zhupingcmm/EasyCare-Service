package com.ocbc.ms.easy.care.encryption.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ocbc.ms.easy.care.encryption.config.EncryptionProperties;
import com.ocbc.ms.easy.care.encryption.exception.FieldEncryptionException;
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
 * jsonb 字段专用的加解密转换器
 */
@Slf4j
@Converter(autoApply = false)
@Component
@RequiredArgsConstructor
public class JsonbAttributeConverter implements AttributeConverter<JsonNode, String> {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    private final EncryptionProperties encryptionProperties;

    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String plainJson = OBJECT_MAPPER.writeValueAsString(attribute);
            String cipherText = encrypt(plainJson);
            return buildWrapper(cipherText);
        } catch (Exception ex) {
            log.error("jsonb 字段加密失败", ex);
            throw new FieldEncryptionException("jsonb 字段加密失败", ex);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(dbData);
            if (isEncryptedWrapper(node)) {
                String cipherText = node.get("cipher").asText();
                String plainJson = decrypt(cipherText);
                return OBJECT_MAPPER.readTree(plainJson);
            }
            return node;
        } catch (Exception ex) {
            log.error("jsonb 字段解密失败", ex);
            throw new FieldEncryptionException("jsonb 字段解密失败", ex);
        }
    }

    private String encrypt(String plain) throws Exception {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
        byte[] encryptedBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decrypt(String cipherText) throws Exception {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        byte[] encryptedBytes = Base64.getDecoder().decode(cipherText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private Cipher getCipher(int mode) throws Exception {
        String keyStr = encryptionProperties.getAesSecretKey();
        byte[] keyBytes = keyStr.getBytes(StandardCharsets.UTF_8);
        byte[] normalizedKey;
        if (keyBytes.length >= 32) {
            normalizedKey = new byte[32];
            System.arraycopy(keyBytes, 0, normalizedKey, 0, 32);
        } else if (keyBytes.length >= 16) {
            normalizedKey = new byte[16];
            System.arraycopy(keyBytes, 0, normalizedKey, 0, 16);
        } else {
            normalizedKey = new byte[16];
            System.arraycopy(keyBytes, 0, normalizedKey, 0, keyBytes.length);
            for (int i = keyBytes.length; i < 16; i++) {
                normalizedKey[i] = 0;
            }
        }
        SecretKeySpec secretKey = new SecretKeySpec(normalizedKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, secretKey);
        return cipher;
    }

    private boolean isEncryptedWrapper(JsonNode node) {
        return node.isObject()
                && node.has("encrypted")
                && node.get("encrypted").asBoolean()
                && node.has("cipher");
    }

    private String buildWrapper(String cipherText) {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("encrypted", true);
        node.put("cipher", cipherText);
        return node.toString();
    }
}
