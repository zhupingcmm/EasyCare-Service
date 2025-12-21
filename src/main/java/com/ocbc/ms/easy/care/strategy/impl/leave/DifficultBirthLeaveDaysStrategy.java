package com.ocbc.ms.easy.care.strategy.impl.leave;

import com.ocbc.ms.easy.care.dto.MaternityLeaveDaysResult;
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
    public MaternityLeaveDaysResult calculate(MaternityLeaveRequest request, MaternityRules rule) {
        // When miscarriage, skip non-miscarriage strategies
        if (Boolean.TRUE.equals(request.getIsMiscarriage())) {
            return null;
        }
        if (BooleanUtils.isFalse(request.getIsDifficultBirth())) {
            return buildResult(0, null);
        }
        if (request.getDoctorRecommendDays() != null) {
            return buildResult(request.getDoctorRecommendDays(), request.getDoctorRecommendDays());
        }
        String matchCode = request.getDifficultBirthLeaveDetail() == null ? null : request.getDifficultBirthLeaveDetail().getCode();
        int days = findDaysFromExtOrDefault(rule, matchCode);
        return buildResult(days, days);
    }
}
