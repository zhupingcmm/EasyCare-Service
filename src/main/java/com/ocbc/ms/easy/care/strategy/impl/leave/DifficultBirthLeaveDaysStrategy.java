package com.ocbc.ms.easy.care.strategy.impl.leave;

import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import com.ocbc.ms.easy.care.strategy.leave.MaternityLeaveDaysStrategy;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

@Component
public class DifficultBirthLeaveDaysStrategy implements MaternityLeaveDaysStrategy {
    @Override
    public MaternityLeaveTypeEnum getType() {
        return MaternityLeaveTypeEnum.DIFFICULT_BIRTH;
    }

    @Override
    public Integer calculate(MaternityLeaveRequest request, MaternityRules rule) {
        // When miscarriage, skip non-miscarriage strategies
        if (Boolean.TRUE.equals(request.getIsMiscarriage())) {
            return 0;
        }
        if (BooleanUtils.isFalse(request.getIsDifficultBirth())) {
            return 0;
        }
        if (request.getDoctorRecommendDays() != null) {
            return request.getDoctorRecommendDays();
        }
        String matchCode = request.getDifficultBirthLeaveDetail() == null ? null : request.getDifficultBirthLeaveDetail().getCode();
        return findDaysFromExtOrDefault(rule, matchCode);
    }
}
