package com.ocbc.ms.easy.care.encryption.exception;

/**
 * 自定义运行时异常，用于封装字段加解密过程中出现的异常信息。
 */
public class FieldEncryptionException extends RuntimeException {

    public FieldEncryptionException(String message) {
        super(message);
    }

    public FieldEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
