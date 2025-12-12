package com.ocbc.ms.easy.care.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }
}
