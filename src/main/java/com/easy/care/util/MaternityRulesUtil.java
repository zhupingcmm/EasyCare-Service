package com.easy.care.util;

import com.easy.care.entity.MaternityRules;

public final class MaternityRulesUtil {

    private MaternityRulesUtil() {}

    public static boolean isMiscarriageType(MaternityRules rules) {
        if (rules == null || rules.getMaternityLeaveType() == null) {
            return false;
        }
        Integer typeId = rules.getMaternityLeaveType().getId();
        return Integer.valueOf(5).equals(typeId);
    }
}
