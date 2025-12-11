package com.easy.care.rule;

import com.easy.care.dto.MaternityLeaveRequest;
import com.easy.care.dto.MaternityLeaveResponse;
import com.easy.care.dto.TimeScope;
import com.easy.care.entity.MaternityRules;
import com.easy.care.enums.MaternityLeaveTypeEnum;
import com.easy.care.function.MaternityLeaveDateHelper;
import com.easy.care.util.MaternityRulesUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MaternityLeaveRuleService {

    @Autowired
    private MaternityLeaveDateHelper maternityLeaveDateHelper;

    public MaternityLeaveResponse calcMaternityDuration(MaternityLeaveRequest maternityLeaveRequest,
                                                        List<MaternityRules> maternityRuleList) {
        Map<Boolean, List<MaternityRules>> partition = maternityRuleList.stream()
                .collect(Collectors.partitioningBy(MaternityRulesUtil::isMiscarriageType));
        List<MaternityRules> miscarriageRules = partition.get(true);
        List<MaternityRules> normalRules = partition.get(false);
        // 按优先级升序排列（不考虑空值）
        normalRules.sort(Comparator
                .comparingInt(r -> MaternityLeaveTypeEnum.fromId(r.getMaternityLeaveType().getId()).getPriority()));
        LocalDate startDate = maternityLeaveRequest.getExpectedDeliveryDate();
        int totalDays = 0;
        List<TimeScope> timeScopeList = new ArrayList<>();
        int index = 0;
        LocalDate innerStartDate = startDate;
        for (MaternityRules rule : normalRules) {
            LocalDate innerEndDate = maternityLeaveDateHelper.calMaternityLeaveDay(maternityLeaveRequest, innerStartDate, rule,
                    MaternityLeaveTypeEnum.fromId(rule.getMaternityLeaveType().getId()));
            if (innerEndDate == null) {
                continue;
            }
            TimeScope timeScope = new TimeScope();
            timeScope.setIndex(index++);
            timeScope.setName(MaternityLeaveTypeEnum.fromId(rule.getMaternityLeaveType().getId()).getCode());
            timeScope.setStartAt(innerStartDate);
            timeScope.setEndAt(innerEndDate);
            int days = (int) ChronoUnit.DAYS.between(innerStartDate, innerEndDate);
            timeScope.setDays(days);
            timeScopeList.add(timeScope);
            totalDays += days;
            innerStartDate = innerEndDate.plusDays(1);
        }
        return genMaternityLeaveResponse(maternityLeaveRequest, timeScopeList, totalDays, startDate.plusDays(totalDays));
    }

    public MaternityLeaveResponse genMaternityLeaveResponse(MaternityLeaveRequest maternityLeaveRequest,
                                                            List<TimeScope> timeScopeList, int totalDays, LocalDate endDate) {
        MaternityLeaveResponse resp = new MaternityLeaveResponse();
        BeanUtils.copyProperties(maternityLeaveRequest, resp);
        resp.setStartDate(maternityLeaveRequest.getExpectedDeliveryDate());
        resp.setTotalDays(totalDays);
        resp.setEndDate(endDate);
        resp.setTimeScopeList(timeScopeList);
        resp.setTotalAllowanceDays(totalDays);
        resp.setReturnToWorkDate(endDate.plusDays(1));
        // 按 code 聚合各段天数到响应字段
        if (timeScopeList != null) {
            for (TimeScope ts : timeScopeList) {
                if (ts == null || ts.getName() == null || ts.getDays() == null) {
                    continue;
                }
                String code = ts.getName();
                int days = ts.getDays();
                switch (code) {
                    case "base":
                        resp.setBaseDays((resp.getBaseDays() == null ? 0 : resp.getBaseDays()) + days);
                        break;
                    case "dystocia":
                        resp.setDystociaDays((resp.getDystociaDays() == null ? 0 : resp.getDystociaDays()) + days);
                        break;
                    case "multiple":
                        resp.setMultiBabyDays((resp.getMultiBabyDays() == null ? 0 : resp.getMultiBabyDays()) + days);
                        break;
                    case "award":
                        resp.setExtendedDays((resp.getExtendedDays() == null ? 0 : resp.getExtendedDays()) + days);
                        break;
                    case "miscarriage":
                        resp.setMiscarriageLeaveDays((resp.getMiscarriageLeaveDays() == null ? 0 : resp.getMiscarriageLeaveDays()) + days);
                        break;
                    default:
                        // 未知类型不计入细项
                        break;
                }
            }
        }
        return resp;
    }
}
