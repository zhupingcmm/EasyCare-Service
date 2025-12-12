package com.ocbc.ms.easy.care.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 安全随机字符串生成工具，用于生成不透明的 Base64 令牌。
 */
public final class RandomGeneratorUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private RandomGeneratorUtil() {
    }

    /**
     * 生成指定长度的安全随机 Base64 字符串。
     * 
     * @param length 期望的字符串长度（字符数）
     * @return Base64 编码的随机字符串
     */
    public static String generateRandomBase64String(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive");
        }
        // Base64 4 字符约对应 3 字节，这里多生成一些再截断
        int byteLen = (int) Math.ceil(length * 0.75) + 4;
        byte[] bytes = new byte[byteLen];
        SECURE_RANDOM.nextBytes(bytes);
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return base64.length() <= length ? base64 : base64.substring(0, length);
    }
}
