package com.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 难产/流产项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DystociaMiscarriageItemDTO {

    /**
     * 代码
     */
    private String code;

    /**
     * 名称
     */
    private String name;
}
