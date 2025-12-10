package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 难产和流产查询响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DystociaMiscarriageResponse {

    /**
     * 流产假列表
     */
    private List<DystociaMiscarriageItemDTO> misCarriage;

    /**
     * 难产假列表
     */
    private List<DystociaMiscarriageItemDTO> dys;
}
