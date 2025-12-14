package com.ocbc.ms.easy.care.strategy.impl.leave;

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
    public Integer calculate(MaternityLeaveRequest request, MaternityRules rule) {
        int baseDays = rule.getDefaultDays();
        Integer plan = rule.getPlanAllowanceDay();
        return plan == null ? baseDays : baseDays + plan;
    }
}
