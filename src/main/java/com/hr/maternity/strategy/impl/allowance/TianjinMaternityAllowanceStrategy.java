package com.hr.maternity.strategy.impl.allowance;

import com.hr.maternity.domain.MonthlyWorkdayInfoDO;
import com.hr.maternity.dto.MaternityAllowanceRequest;
import com.hr.maternity.dto.MaternityAllowanceResponse;
import com.hr.maternity.enums.AddDeleteItemEnum;
import com.hr.maternity.strategy.MaternityAllowanceStrategy;
import com.hr.maternity.service.MaternityWageCalculatorService;
import com.hr.maternity.service.RequestDateCompensationService;
import com.hr.maternity.service.WorkdayCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TianjinMaternityAllowanceStrategy implements MaternityAllowanceStrategy {

    private final MaternityWageCalculatorService maternityWageCalculatorService;
    private final WorkdayCalculatorService workdayCalculatorService;
    private final RequestDateCompensationService requestDateCompensationService;

    @Override
    public MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request) {
        MaternityAllowanceResponse response = new MaternityAllowanceResponse();

        // 获取第一个月和最后一个月年月信息
        int firstCompleteYear = request.getMaternityLeaveEndDate().getYear();
        int firstCompleteMonth = request.getMaternityLeaveEndDate().getMonthValue();
        int lastCompleteYear = request.getMaternityLeaveEndDate().getYear();
        int lastCompleteMonth = request.getMaternityLeaveEndDate().getMonthValue();

        int startDay = request.getMaternityLeaveStartDate().getDayOfMonth();
        int startingMonth = request.getMaternityLeaveStartDate().getMonthValue();
        int startingYear = request.getMaternityLeaveStartDate().getYear();
        int endingYear = request.getMaternityLeaveEndDate().getYear();
        int endingMonth = request.getMaternityLeaveEndDate().getMonthValue();
        // 判断产假开始日期是否为当月15日之前或之后
        boolean isBeforeOrEqual15th = startDay <= 15;

        // 获取产假期间月份工作日信息
        List<MonthlyWorkdayInfoDO> monthlyWorkdayList = workdayCalculatorService.calculateMonthlyWorkdays(
                request.getMaternityLeaveStartDate(), request.getMaternityLeaveEndDate());

        // 判断是否跨过4月与7月
        boolean baseSalaryAdjusted = maternityWageCalculatorService.crossesApril(monthlyWorkdayList);
        boolean socialInsuranceBaseAdjusted = maternityWageCalculatorService.crossesJuly(monthlyWorkdayList);

        // a. 当前月基本工资计算补贴金额（天津：参照绍兴，仅与政府发放比较）
        BigDecimal currentBaseSalary = request.getMonthlyBaseSalary() != null ? request.getMonthlyBaseSalary() : BigDecimal.ZERO;
        BigDecimal formulaA = currentBaseSalary
                .divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(request.getMaternityLeaveDays()));

        // c. 政府发放补贴金额
        BigDecimal formulaC = request.getGovernmentAllowance() != null ? request.getGovernmentAllowance() : BigDecimal.ZERO;

        // d. 员工应享受补贴（仅比较 a 与 c）
        BigDecimal formulaD = formulaA.max(formulaC);

        // e. 补差
        BigDecimal compensationAmount = formulaD.subtract(formulaC);

        // 计算起止月的产假期间工资（用于返还金额计算）
        BigDecimal startingMonthMaternityWage = BigDecimal.ZERO;
        BigDecimal endingMonthMaternityWage = BigDecimal.ZERO;
        boolean startingMonthFullMonth = true;
        boolean endingMonthFullMonth = true;

        BigDecimal adjustedMonthBaseSalary = baseSalaryAdjusted && request.getAdjustedMonthlyBaseSalary() != null
                ? request.getAdjustedMonthlyBaseSalary() : request.getMonthlyBaseSalary();

        if (!monthlyWorkdayList.isEmpty()) {
            if (!monthlyWorkdayList.get(0).getFullMonth()) {
                startingMonthMaternityWage = maternityWageCalculatorService.calculateStartingMonthMaternityWage(
                        request.getMaternityLeaveStartDate(), request.getMaternityLeaveEndDate(), request.getMonthlyBaseSalary()
                );
                startingMonthFullMonth = false;
            }
            if (!monthlyWorkdayList.get(monthlyWorkdayList.size() - 1).getFullMonth()) {
                endingMonthMaternityWage = maternityWageCalculatorService.calculateEndingMonthMaternityWage(
                        request.getMaternityLeaveStartDate(), request.getMaternityLeaveEndDate(), adjustedMonthBaseSalary
                );
                endingMonthFullMonth = false;
            }
        }

        // 统计完整月份与首末完整月份的年月
        long completeMonths = monthlyWorkdayList.stream().mapToLong(w -> w.getFullMonth() ? 1L : 0L).sum();
        List<MonthlyWorkdayInfoDO> completeMonthsList = monthlyWorkdayList.stream().filter(MonthlyWorkdayInfoDO::getFullMonth).toList();
        if (!completeMonthsList.isEmpty()) {
            MonthlyWorkdayInfoDO firstCompleteMonthInfo = completeMonthsList.get(0);
            MonthlyWorkdayInfoDO lastCompleteMonthInfo = completeMonthsList.get(completeMonthsList.size() - 1);
            firstCompleteYear = firstCompleteMonthInfo.getYear();
            firstCompleteMonth = firstCompleteMonthInfo.getMonth();
            lastCompleteYear = lastCompleteMonthInfo.getYear();
            lastCompleteMonth = lastCompleteMonthInfo.getMonth();
        }

        // 计算公司垫付净额及相关条目
        BigDecimal companyAdvanceSum = BigDecimal.ZERO;
        BigDecimal espp = BigDecimal.ZERO;
        BigDecimal unionFee = BigDecimal.ZERO;
        BigDecimal socialInsuranceBase = BigDecimal.ZERO;
        BigDecimal adjustedSocialInsuranceBase = BigDecimal.ZERO;

        if (request.getCompanyAdvance() != null) {
            espp = request.getCompanyAdvance().getEspp();
            unionFee = request.getCompanyAdvance().getUnionFee();
            socialInsuranceBase = request.getCompanyAdvance().getSocialInsuranceBase();
            adjustedSocialInsuranceBase = socialInsuranceBaseAdjusted ? request.getCompanyAdvance().getAdjustedSocialInsuranceBase() : socialInsuranceBase;
            companyAdvanceSum = request.getCompanyAdvance().calculateNetCompanyAdvanceWithMonthlyLogic(monthlyWorkdayList, socialInsuranceBaseAdjusted);
        }

        // 返还金额与明细
        BigDecimal refundAmount = companyAdvanceSum;
        BigDecimal lastMonthWage = BigDecimal.ZERO;
        if (!endingMonthFullMonth && endingMonthMaternityWage.compareTo(BigDecimal.ZERO) > 0) {
            lastMonthWage = adjustedMonthBaseSalary.subtract(endingMonthMaternityWage);
            if (adjustedSocialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                lastMonthWage = lastMonthWage.subtract(adjustedSocialInsuranceBase);
            }
            if (espp.compareTo(BigDecimal.ZERO) > 0) {
                lastMonthWage = lastMonthWage.subtract(espp);
            }
            if (unionFee.compareTo(BigDecimal.ZERO) > 0) {
                lastMonthWage = lastMonthWage.subtract(unionFee);
            }
            refundAmount = refundAmount.subtract(lastMonthWage);
        }

        BigDecimal firstMonthWage = BigDecimal.ZERO;
        if (!startingMonthFullMonth && startingMonthMaternityWage.compareTo(BigDecimal.ZERO) > 0) {
            if (isBeforeOrEqual15th) {
                // 15日之前：按照原逻辑计算
                firstMonthWage = request.getMonthlyBaseSalary().subtract(startingMonthMaternityWage);
                if (socialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                    firstMonthWage = firstMonthWage.subtract(socialInsuranceBase);
                }
                if (espp.compareTo(BigDecimal.ZERO) > 0) {
                    firstMonthWage = firstMonthWage.subtract(espp);
                }
                if (unionFee.compareTo(BigDecimal.ZERO) > 0) {
                    firstMonthWage = firstMonthWage.subtract(unionFee);
                }
                if (firstMonthWage.compareTo(BigDecimal.ZERO) < 0) { // 产假开始月的应发工资扣除社保，公积金，ESPP后，如果小于0，需计算返还金额
                    refundAmount = refundAmount.subtract(firstMonthWage);
                }
            } else {
                // 15日之后已过HR每月工资结算日期，产假首月工资会照常发放，首月产假期间工资重复发放，需返还startingMonthMaternityWage
                refundAmount = refundAmount.add(startingMonthMaternityWage);
            }
        }

        // 计算基于产假申请日期的补偿金额
        Map<String, Object> requestDateCompensationResult = requestDateCompensationService.calculateRequestDateCompensation(
                request.getMonthlyBaseSalary(),
                adjustedMonthBaseSalary,
                request.getMaternityLeaveStartDate(),
                request.getMaternityLeaveRequestDate(),
                socialInsuranceBase,
                adjustedSocialInsuranceBase,
                espp,
                unionFee
        );
        
        BigDecimal requestDateCompensation = (BigDecimal) requestDateCompensationResult.get("compensation");
        String requestDateCompensationDetail = (String) requestDateCompensationResult.get("refundDetail");
        
        // 将产假申请日期补偿加到返还金额中
        refundAmount = refundAmount.add(requestDateCompensation);
        
        // 修改返还金额计算，小于0按0
        response.setEmployeeRefundAmount(refundAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : refundAmount);

        // 返还明细
        List<String> refundDetailsList = new ArrayList<>();
        if (!startingMonthFullMonth) {
            if(isBeforeOrEqual15th && firstMonthWage.compareTo(BigDecimal.ZERO) < 0) {
                StringBuilder wageFormula1stMonth = new StringBuilder();
                wageFormula1stMonth.append(String.format("%.2f-%.2f", request.getMonthlyBaseSalary(), startingMonthMaternityWage));
                if (socialInsuranceBase.compareTo(BigDecimal.ZERO) > 0)
                    wageFormula1stMonth.append(String.format("-%.2f", socialInsuranceBase));
                if (espp.compareTo(BigDecimal.ZERO) > 0) wageFormula1stMonth.append(String.format("-%.2f", espp));
                if (unionFee.compareTo(BigDecimal.ZERO) > 0)
                    wageFormula1stMonth.append(String.format("-%.2f", unionFee));
                refundDetailsList.add(String.format("%d.%d 工资不够扣：%s=%.2f元", startingYear, startingMonth, wageFormula1stMonth, firstMonthWage.abs()));
            }
            refundDetailsList.add(String.format("产假工资折算 %d年%d月，扣除：%.2f元", startingYear, startingMonth, startingMonthMaternityWage));
        }
        if (!endingMonthFullMonth) {
            StringBuilder wageFormula = new StringBuilder();
            wageFormula.append(String.format("%.2f-%.2f", adjustedMonthBaseSalary, endingMonthMaternityWage));
            if (adjustedSocialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) wageFormula.append(String.format("-%.2f", adjustedSocialInsuranceBase));
            if (espp.compareTo(BigDecimal.ZERO) > 0) wageFormula.append(String.format("-%.2f", espp));
            if (unionFee.compareTo(BigDecimal.ZERO) > 0) wageFormula.append(String.format("-%.2f", unionFee));
            if (lastMonthWage.compareTo(BigDecimal.ZERO) >= 0) {
                refundDetailsList.add(String.format("%d.%d 工资剩余：%s=%.2f元", endingYear, endingMonth, wageFormula, lastMonthWage.abs()));
            } else {
                refundDetailsList.add(String.format("%d.%d 工资不够扣：%s=%.2f元", endingYear, endingMonth, wageFormula, lastMonthWage.abs()));
            }
            refundDetailsList.add(String.format("产假工资折算 %d年%d月，扣除：%.2f元", endingYear, endingMonth, endingMonthMaternityWage));
        }
        refundDetailsList.add(String.format("月度个人部分社保公积金合计：%.2f元", socialInsuranceBase));
        if (socialInsuranceBaseAdjusted) {
            refundDetailsList.add(String.format("调整后月度个人部分社保公积金合计：%.2f元", adjustedSocialInsuranceBase));
        }
        if (espp.compareTo(BigDecimal.ZERO) > 0) {
            refundDetailsList.add(String.format("%d.%d-%d.%d月ESPP：%.2f×%d=%.2f元", firstCompleteYear, firstCompleteMonth, lastCompleteYear, lastCompleteMonth, espp, completeMonths, espp.multiply(new BigDecimal(completeMonths))));
        }
        if (unionFee.compareTo(BigDecimal.ZERO) > 0) {
            refundDetailsList.add(String.format("%d.%d-%d.%d月工会费：%.2f×%d=%.2f元", firstCompleteYear, firstCompleteMonth, lastCompleteYear, lastCompleteMonth, unionFee, completeMonths, unionFee.multiply(new BigDecimal(completeMonths))));
        }
        
        // 添加产假申请日期补偿详情
        if (requestDateCompensation.compareTo(BigDecimal.ZERO) > 0 && !requestDateCompensationDetail.isEmpty()) {
            refundDetailsList.add(requestDateCompensationDetail);
        }
        
        StringBuilder formula = new StringBuilder("返还金额：");
        if (request.getCompanyAdvance() != null) {
            if (socialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                formula.append(String.format("%.2f", request.getCompanyAdvance().calculateSocialInsuranceBaseByMonth(socialInsuranceBase, monthlyWorkdayList)));
            }
            if (espp.compareTo(BigDecimal.ZERO) > 0) formula.append("+").append(String.format("%.2f", espp.multiply(new BigDecimal(completeMonths))));
            if (unionFee.compareTo(BigDecimal.ZERO) > 0) formula.append("+").append(String.format("%.2f", unionFee.multiply(new BigDecimal(completeMonths))));
            if (request.getCompanyAdvance().getAddItem() != null) {
                for (Map.Entry<String, BigDecimal> entry : request.getCompanyAdvance().getAddItem().entrySet()) {
                    if (!entry.getKey().equalsIgnoreCase(AddDeleteItemEnum.ESPP.getCode())
                            && !entry.getKey().equals(AddDeleteItemEnum.UNION_FEE.getCode())
                            && !entry.getKey().equals(AddDeleteItemEnum.SOCIAL_INSURANCE_BASE.getCode())
                            && !entry.getKey().equals(AddDeleteItemEnum.ADJUSTED_SOCIAL_INSURANCE_BASE.getCode())) {
                        formula.append("+").append(String.format("%.2f", entry.getValue()));
                    }
                }
            }
            if (request.getCompanyAdvance().getDeleteItem() != null) {
                for (Map.Entry<String, BigDecimal> entry : request.getCompanyAdvance().getDeleteItem().entrySet()) {
                    formula.append("-").append(String.format("%.2f", entry.getValue()));
                }
            }
            if (lastMonthWage.compareTo(BigDecimal.ZERO) > 0) {
                formula.append("-").append(String.format("%.2f", lastMonthWage));
            } else if (lastMonthWage.compareTo(BigDecimal.ZERO) < 0) {
                formula.append("+").append(String.format("%.2f", lastMonthWage.abs()));
            }
            if (!startingMonthFullMonth) {
                if (!isBeforeOrEqual15th) {
                    formula.append("+").append(String.format("%.2f", startingMonthMaternityWage.abs()));
                } else if (firstMonthWage.compareTo(BigDecimal.ZERO) < 0) {
                    formula.append("+").append(String.format("%.2f", firstMonthWage.abs()));
                }
            }
            formula.append("=").append(String.format("%.2f元", refundAmount));
            if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                formula.append("（计算结果为负，取0）");
            }
        } else {
            // 使用总额时，显示简化计算
            formula.append(String.format("返还金额：%.2f-%.2f=%.2f元", companyAdvanceSum, lastMonthWage, refundAmount));
            if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                formula.append("（计算结果为负，取0）");
            }
        }
        refundDetailsList.add(formula.toString());
        response.setRefundDetails(refundDetailsList);

        // 构建详细计算说明列表（天津版：仅当前base vs 政府发放）
        List<String> calculationDetailsList = new ArrayList<>();
        calculationDetailsList.add(String.format("当前月基本工资计算补贴金额：%.2f÷30×%d=%.2f元", currentBaseSalary, request.getMaternityLeaveDays(), formulaA));
        calculationDetailsList.add(String.format("政府发放补贴金额：%.2f元", formulaC));
        calculationDetailsList.add(String.format("员工应享受补贴：%.2f元", formulaD));
        calculationDetailsList.add(String.format("补差金额：%.2f-%.2f=%.2f元", formulaD, formulaC, compensationAmount));
        response.setAllowanceCompensationDetails(calculationDetailsList);

        // 设置响应数据
        response.setLanId(request.getLanId());
        response.setEmployeeName(request.getEmployeeName());
        response.setCityCode(request.getCityCode());
        response.setAllowanceDays(request.getMaternityLeaveDays());
        response.setExtraAllowance(BigDecimal.ZERO);
        response.setMaternityAllowance(formulaD);
        response.setCompensationAmount(compensationAmount);

        return response;
    }

    @Override
    public String getSupportedCityCode() {
        return "TJ";
    }
}

