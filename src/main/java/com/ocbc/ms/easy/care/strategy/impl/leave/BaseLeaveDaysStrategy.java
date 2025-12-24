package com.ocbc.ms.easy.care.strategy.impl.leave;

import com.ocbc.ms.easy.care.dto.MaternityLeaveDaysResult;
import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import com.ocbc.ms.easy.care.strategy.leave.MaternityLeaveDaysStrategy;
import org.springframework.stereotype.Component;

@Component
public class BaseLeaveDaysStrategy implements MaternityLeaveDaysStrategy {

    @Override
    public MaternityLeaveTypeEnum getType() {
        return MaternityLeaveTypeEnum.BASE;
    }

    @Override
    public MaternityLeaveDaysResult calculate(MaternityLeaveRequest request, MaternityRules rule) {
        // When miscarriage, skip non-miscarriage strategies
        if (Boolean.TRUE.equals(request.getIsMiscarriage())) {
            return null;
        }
        Integer baseDays = rule.getDefaultDays();
        Integer planAllowanceDay = rule.getPlanAllowanceDay() == null ? baseDays : rule.getPlanAllowanceDay();
        return buildResult(baseDays, planAllowanceDay);
    }
}
