package com.easy.care.encryption.converter;

/**
 * 封装被加密字段的原始类型和 JSON 序列化值，便于在 Base64 字符串中存储。
 */
public record EncryptionPayload(String type, String jsonValue) {
}
