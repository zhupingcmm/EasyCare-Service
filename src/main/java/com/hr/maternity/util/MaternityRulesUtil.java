package com.hr.maternity.util;

import com.hr.maternity.entity.MaternityRules;

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
