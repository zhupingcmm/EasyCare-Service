package com.hr.maternity.helper;

import com.alibaba.fastjson2.JSONArray;
import com.hr.maternity.dto.MaternityLeaveExtDTO;
import com.hr.maternity.dto.MaternityLeaveRequest;
import com.hr.maternity.entity.MaternityRules;
import com.hr.maternity.enums.AwardLeaveEnum;
import com.hr.maternity.enums.MaternityLeaveTypeEnum;
import com.hr.maternity.util.EasyCareDateUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

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
            case MULTI_BABIES -> calcMultiBabies(maternityLeaveRequest, maternityRule);
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

    public Integer calcMultiBabies(MaternityLeaveRequest maternityLeaveRequest, MaternityRules maternityRule) {
        return (maternityLeaveRequest.getNumberOfBabies() - 1) * findDaysFromDefault(maternityRule);
    }


    public Integer calcAward(MaternityLeaveRequest maternityLeaveRequest, MaternityRules maternityRule) {
        if (BooleanUtils.isFalse(maternityLeaveRequest.getHasExtendedDays())) {
            return 0;
        }
        AwardLeaveEnum awdCode = ConfigDataConvertHelper.convertKidsToAwardLeaveEnum(maternityLeaveRequest.getNumberOfKids());
        return findDaysFromExtFirst(maternityRule, MaternityLeaveTypeEnum.AWARD.getCode(), awdCode.getCode());
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
     * 通用：从规则扩展字段中，按匹配code提取天数
     * maternityLeaveExt 直接是 JSONArray 格式：[{"code":"xxx","days":N}]
     */
    private Integer findDaysFromExtFirst(MaternityRules maternityRule, String groupKey, String matchCode) {
        Object ext = maternityRule.getMaternityLeaveExt();
        if (ext == null) {
            return maternityRule.getDefaultDays();
        }

        String extStr = (String) ext;
        List<MaternityLeaveExtDTO> extList = JSONArray.parseArray(extStr, MaternityLeaveExtDTO.class);

        if (CollectionUtils.isEmpty(extList)) {
            throw new RuntimeException(matchCode + ":config not found");
        }

        MaternityLeaveExtDTO matched = null;
        for (MaternityLeaveExtDTO maternityLeaveExtDTO : extList) {
            if (maternityLeaveExtDTO.getCode().equals(matchCode)) {
                matched = maternityLeaveExtDTO;
                break;
            }
        }
        if (matched == null) {
            throw new RuntimeException(matchCode + ":days not configured");
        }
        return matched.getDays();
    }


    private LocalDate computeWithExtension(LocalDate startDate, Integer days, Boolean holidayExtend) {
        if (startDate == null || days == null || days <= 0) {
            return null;
        }
        LocalDate endDate = startDate.plusDays(days - 1);
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
