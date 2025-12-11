package com.easy.care.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应包装，将所有返回值统一包装为 { code, message, data }
 */
@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // 对所有响应生效
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 已经是统一结构，直接返回
        if (body instanceof ApiResponse) {
            return body;
        }

        // 文件下载（byte[]）不做包装，直接返回
        if (body instanceof byte[]) {
            return body;
        }

        // 处理 ResponseEntity 包装
        if (body instanceof ResponseEntity<?> re) {
            Object inner = re.getBody();
            if (inner instanceof ApiResponse) {
                return re; // 已包装
            }
            // 文件下载（byte[]）不做包装
            if (inner instanceof byte[]) {
                return re;
            }
            ApiResponse<?> wrapped = ApiResponse.success(inner);
            return new ResponseEntity<>(wrapped, re.getHeaders(), re.getStatusCode());
        }

        // 对字符串不做包装，避免消息转换冲突
        if (body instanceof String) {
            return body;
        }

        // 其他普通对象统一包装
        return ApiResponse.success(body);
    }
}
