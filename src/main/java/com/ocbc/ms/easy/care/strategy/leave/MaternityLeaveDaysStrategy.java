package com.ocbc.ms.easy.care.strategy.leave;

import com.alibaba.fastjson2.JSONArray;
import com.ocbc.ms.easy.care.dto.MaternityLeaveDaysResult;
import com.ocbc.ms.easy.care.dto.MaternityLeaveExtDTO;
import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public interface MaternityLeaveDaysStrategy {

    MaternityLeaveTypeEnum getType();

    MaternityLeaveDaysResult calculate(MaternityLeaveRequest request, MaternityRules rule);


    default Integer findDaysFromExtOrDefault(MaternityRules rule, String matchCode) {
        Object ext = rule.getMaternityLeaveExt();
        if (ext == null) {
            return rule.getDefaultDays();
        }
        List<MaternityLeaveExtDTO> extList = JSONArray.parseArray((String) ext, MaternityLeaveExtDTO.class);
        if (CollectionUtils.isEmpty(extList)) {
            return rule.getDefaultDays();
        }
        for (MaternityLeaveExtDTO dto : extList) {
            if (dto != null && Objects.equals(dto.getCode(), matchCode)) {
                return dto.getDays();
            }
        }
        return rule.getDefaultDays();
    }


    default MaternityLeaveDaysResult buildResult(Integer leaveDays, Integer allowanceDays) {
        return MaternityLeaveDaysResult.builder()
                .leaveDays(leaveDays)
                .allowanceDays(allowanceDays)
                .build();
    }
}
