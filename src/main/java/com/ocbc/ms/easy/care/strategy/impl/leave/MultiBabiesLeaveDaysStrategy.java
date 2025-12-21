package com.ocbc.ms.easy.care.strategy.impl.leave;

import com.ocbc.ms.easy.care.dto.MaternityLeaveDaysResult;
import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import com.ocbc.ms.easy.care.strategy.leave.MaternityLeaveDaysStrategy;
import org.springframework.stereotype.Component;

@Component
public class MultiBabiesLeaveDaysStrategy implements MaternityLeaveDaysStrategy {

    @Override
    public MaternityLeaveTypeEnum getType() {
        return MaternityLeaveTypeEnum.MULTI_BABIES;
    }

    @Override
    public MaternityLeaveDaysResult calculate(MaternityLeaveRequest request, MaternityRules rule) {
        // When miscarriage, skip non-miscarriage strategies
        if (Boolean.TRUE.equals(request.getIsMiscarriage())) {
            return null;
        }
        Integer num = request.getNumberOfBabies();
        if (num == null || num <= 1) {
            return buildResult(0, 0);
        }
        int perExtra = rule.getDefaultDays();
        int days = (num - 1) * perExtra;
        return buildResult(days, days);
    }
}
