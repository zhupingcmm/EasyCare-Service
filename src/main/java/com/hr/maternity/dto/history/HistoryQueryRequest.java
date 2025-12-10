package com.hr.maternity.dto.history;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class HistoryQueryRequest {

    @NotBlank(message = "hrId 不能为空")
    private String hrId;

    /**
     * 可选的 employeeId 过滤条件
     */
    private List<String> employeeIds;
}
