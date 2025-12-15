package com.ocbc.ms.easy.care.helper;

import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.dto.MaternityLeaveTypeEndDate;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import com.ocbc.ms.easy.care.strategy.leave.MaternityLeaveDaysStrategy;
import com.ocbc.ms.easy.care.util.EasyCareDateUtil;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@Service
public class MaternityLeaveDateHelper {

    @Autowired
    private EasyCareDateUtil easyCareDateUtil;

    private final Map<MaternityLeaveTypeEnum, DaysCalculator> calculators = new EnumMap<>(MaternityLeaveTypeEnum.class);

    @Autowired(required = false)
    private List<MaternityLeaveDaysStrategy> strategies;

    @PostConstruct
    private void initCalculators() {
        for (MaternityLeaveDaysStrategy s : strategies) {
            calculators.put(s.getType(), s::calculate);
        }
    }

    @FunctionalInterface
    private interface DaysCalculator {
        Integer apply(MaternityLeaveRequest request, MaternityRules rule);
    }

    public MaternityLeaveTypeEndDate calMaternityLeaveDay(MaternityLeaveRequest maternityLeaveRequest,
                                          LocalDate startDate,
                                          MaternityRules maternityRule,
                                          MaternityLeaveTypeEnum type) {
        if (type == null) {
            return null;
        }
        DaysCalculator calculator = calculators.get(type);
        int days = calculator == null ? 0 : calculator.apply(maternityLeaveRequest, maternityRule);
        return computeWithExtension(startDate, days, maternityRule.getHolidayExtend());
    }

    /**
     * 通用：从规则扩展字段中，按匹配code提取天数
     * maternityLeaveExt 直接是 JSONArray 格式：[{"code":"xxx","days":N}]
     */
    private MaternityLeaveTypeEndDate computeWithExtension(LocalDate startDate, Integer days, Boolean holidayExtend) {
        if (startDate == null || days == null || days <= 0) {
            return null;
        }
        LocalDate originEndDate = startDate.plusDays(days - 1);
        if (BooleanUtils.isNotTrue(holidayExtend)) {
            MaternityLeaveTypeEndDate meta = new MaternityLeaveTypeEndDate();
            meta.setOriginEndDate(originEndDate);
            meta.setExtendDays(0);
            meta.setAdjustEndDate(originEndDate);
            return meta;
        }
        LocalDate endDate = originEndDate;
        LocalDate loopStart = startDate;
        int totalExtend = 0;
        while (true) {
            int count = easyCareDateUtil.countExtensionDays(loopStart, endDate);
            if (count > 0) {
                endDate = endDate.plusDays(count);
                loopStart = endDate;
                totalExtend += count;
            } else {
                break;
            }
        }
        MaternityLeaveTypeEndDate meta = new MaternityLeaveTypeEndDate();
        meta.setOriginEndDate(originEndDate);
        meta.setExtendDays(totalExtend);
        meta.setAdjustEndDate(endDate);
        return meta;
    }
}
