package com.ocbc.ms.easy.care.strategy.impl.leave;

import com.alibaba.fastjson2.JSONArray;
import com.ocbc.ms.easy.care.dto.MaternityLeaveExtDTO;
import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import com.ocbc.ms.easy.care.strategy.leave.MaternityLeaveDaysStrategy;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Component
public class DifficultBirthLeaveDaysStrategy implements MaternityLeaveDaysStrategy {
    @Override
    public MaternityLeaveTypeEnum getType() {
        return MaternityLeaveTypeEnum.DIFFICULT_BIRTH;
    }

    @Override
    public Integer calculate(MaternityLeaveRequest request, MaternityRules rule) {
        if (BooleanUtils.isFalse(request.getIsDifficultBirth())) {
            return 0;
        }
        if (request.getDoctorRecommendDays() != null) {
            return request.getDoctorRecommendDays();
        }
        // prefer difficultBirthTypeCode, fallback to difficultType
        String matchCode = request.getDifficultBirthTypeCode();
        if (!StringUtils.isBlank(matchCode)) {
            matchCode = request.getDifficultType();
        }
        return findDaysFromExtOrDefault(rule, matchCode);
    }
}
