package com.ocbc.ms.easy.care.strategy.leave;

import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;

public interface MaternityLeaveDaysStrategy {

    MaternityLeaveTypeEnum getType();

    Integer calculate(MaternityLeaveRequest request, MaternityRules rule);
}
