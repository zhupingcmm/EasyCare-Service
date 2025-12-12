package com.ocbc.ms.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeScope {
    private Integer index;
    private String name;
    private String additionalInfo;
    private Integer days;
    private LocalDate startAt;
    private LocalDate endAt;
    // 存顺延信息(only for 上海/苏州)。
    private Map<String, String> details;
}