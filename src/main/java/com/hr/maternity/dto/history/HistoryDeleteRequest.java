package com.hr.maternity.dto.history;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class HistoryDeleteRequest {

    @NotBlank(message = "hrId 不能为空")
    private String hrId;

    @NotEmpty(message = "employeeIds 不能为空")
    private List<String> employeeIds;
}
