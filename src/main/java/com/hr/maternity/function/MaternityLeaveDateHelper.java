package com.hr.maternity.function;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hr.maternity.dto.MaternityLeaveRequest;
import com.hr.maternity.entity.MaternityRules;
import com.hr.maternity.enums.MaternityLeaveTypeEnum;
import com.hr.maternity.util.EasyCareDateUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
public class MaternityLeaveDateHelper {

    @Autowired
    private EasyCareDateUtil easyCareDateUtil;

    public LocalDate calMaternityLeaveDay(MaternityLeaveRequest maternityLeaveRequest,
                                          LocalDate startDate,
                                          MaternityRules maternityRule,
                                          MaternityLeaveTypeEnum type) {
        if (type == null) {
            return null;
        }
        int days = switch (type) {
            case BASE -> calcBase(maternityRule);
            case DIFFICULT_BIRTH -> calcDifficult(maternityLeaveRequest, maternityRule);
            case MULTI_BABIES -> calcMultiBabies(maternityRule);
            case AWARD -> calcAward(maternityLeaveRequest, maternityRule);
            default -> 0;
        };
        return computeWithExtension(startDate, days, maternityRule.getHolidayExtend());
    }

    public Integer calcBase(MaternityRules maternityRule) {
        return findDaysFromDefault(maternityRule);
    }

    public Integer calcDifficult(MaternityLeaveRequest maternityLeaveRequest, MaternityRules maternityRule) {
        if (BooleanUtils.isFalse(maternityLeaveRequest.getIsDifficultBirth())) {
            return 0;
        }
        // 1) 医嘱优先
        if (maternityLeaveRequest.getDoctorRecommendDays() != null) {
            return maternityLeaveRequest.getDoctorRecommendDays();
        }
        return findDaysFromExtFirst(maternityRule, MaternityLeaveTypeEnum.DIFFICULT_BIRTH.getCode(), maternityLeaveRequest.getDifficultBirthTypeCode());
    }

    public Integer calcMultiBabies(MaternityRules maternityRule) {
        return findDaysFromDefault(maternityRule);
    }


    public Integer calcAward(MaternityLeaveRequest maternityLeaveRequest, MaternityRules maternityRule) {
        return findDaysFromExtFirst(maternityRule, MaternityLeaveTypeEnum.AWARD.getCode(), maternityLeaveRequest.getNumOfKids() + "");
    }


    public Integer calcMissCarriage(MaternityLeaveRequest maternityLeaveRequest, MaternityRules maternityRule) {
        if (maternityLeaveRequest.getIsMiscarriage() && (maternityLeaveRequest.getMiscarriageLeaveDetail() == null
                || StringUtils.isEmpty(maternityLeaveRequest.getMiscarriageLeaveDetail().getCode())
        )) {
            throw new RuntimeException("misCarriage param invalid");
        }
        return findDaysFromExtFirst(maternityRule, MaternityLeaveTypeEnum.MISCARRIAGE.getCode(), maternityLeaveRequest.getMiscarriageLeaveDetail().getCode());
    }


    private Integer findDaysFromDefault(MaternityRules maternityRule) {
        return maternityRule.getDefaultDays();
    }

    /**
     * 通用：从规则扩展字段中，按组键与匹配code提取天数
     * 期望结构：extMap[groupKey] 为数组，元素包含 {"code":"xxx","days":N}
     */
    private Integer findDaysFromExtFirst(MaternityRules maternityRule, String groupKey, String matchCode) {
        Map<String, Object> extMap = maternityRule.getMaternityLeaveExt();
        if (extMap == null) {
            return maternityRule.getDefaultDays();
        }
        if (!extMap.containsKey(groupKey)) {
            throw new RuntimeException(groupKey + ":config not found");
        }
        Object val = extMap.get(groupKey);
        JSONArray jsonArray;
        if (val instanceof JSONArray) {
            jsonArray = (JSONArray) val;
        } else {
            jsonArray = JSONArray.parseArray(JSON.toJSONString(val));
        }
        if (jsonArray == null || jsonArray.isEmpty()) {
            throw new RuntimeException(matchCode + ":config not found");
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            if (obj == null) continue;
            String code = obj.getString("code");
            if (matchCode.equalsIgnoreCase(code)) {
                Integer d = obj.getInteger("days");
                if (d == null) {
                    throw new RuntimeException(matchCode + ":days not configured");
                }
                return d;
            }
        }
        throw new RuntimeException(matchCode + ":days not configured");
    }


    private LocalDate computeWithExtension(LocalDate startDate, Integer days, Boolean holidayExtend) {
        if (startDate == null || days == null || days <= 0) {
            return null;
        }
        LocalDate endDate = startDate.plusDays(days);
        if (BooleanUtils.isNotTrue(holidayExtend)) {
            return endDate;
        }
        LocalDate loopStart = startDate;
        while (true) {
            int count = easyCareDateUtil.countExtensionDays(loopStart, endDate);
            if (count > 0) {
                endDate = endDate.plusDays(count);
                loopStart = endDate;
            } else {
                break;
            }
        }
        return endDate;
    }
}
