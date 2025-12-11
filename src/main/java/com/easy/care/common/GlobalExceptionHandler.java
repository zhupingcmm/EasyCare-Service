package com.easy.care.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理，统一返回 { code, message, data }
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验异常（@Valid - Bean 参数校验）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        log.error("参数校验失败(MethodArgumentNotValidException): {}", msg, ex);
        return new ResponseEntity<>(ApiResponse.error(400, msg), HttpStatus.BAD_REQUEST);
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + (fe.getDefaultMessage() == null ? "参数不合法" : fe.getDefaultMessage());
    }

    /**
     * 参数校验异常（简单类型 - 如 @RequestParam 校验）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("参数校验失败(ConstraintViolationException): {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * 非法参数
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("非法参数异常: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error(422, ex.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * 业务异常（登录失败、认证失败等）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        if (message != null && (message.contains("LDAP") || message.contains("认证") || message.contains("登录"))) {
            return new ResponseEntity<>(ApiResponse.error(401, message), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(ApiResponse.error(400, message != null ? message : "请求失败"), HttpStatus.BAD_REQUEST);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) throws Exception {
        log.error("未处理异常", ex);
        throw ex;
        // new ResponseEntity<>(ApiResponse.error(500, "服务器内部错误"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
