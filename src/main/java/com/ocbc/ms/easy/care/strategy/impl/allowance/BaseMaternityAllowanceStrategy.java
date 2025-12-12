package com.ocbc.ms.easy.care.strategy.impl.allowance;

import com.ocbc.ms.easy.care.domain.MonthlyWorkdayInfoDO;
import com.ocbc.ms.easy.care.dto.AllowanceRulesResponse;
import com.ocbc.ms.easy.care.dto.MaternityAllowanceRequest;
import com.ocbc.ms.easy.care.dto.MaternityAllowanceResponse;
import com.ocbc.ms.easy.care.enums.AddDeleteItemEnum;
import com.ocbc.ms.easy.care.enums.CityEnum;
import com.ocbc.ms.easy.care.service.AllowanceRulesService;
import com.ocbc.ms.easy.care.service.CityService;
import com.ocbc.ms.easy.care.service.MaternityWageCalculatorService;
import com.ocbc.ms.easy.care.service.RequestDateCompensationService;
import com.ocbc.ms.easy.care.service.WorkdayCalculatorService;
import com.ocbc.ms.easy.care.strategy.MaternityAllowanceStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ocbc.ms.easy.care.constant.AllowanceRuleConstants.PAYOUT_METHOD_CORPORATE;
import static com.ocbc.ms.easy.care.constant.AllowanceRuleConstants.PAYOUT_METHOD_INDIVIDUAL;

/**
 * 深圳市生育津贴计算策略实现
 */
@Component
@RequiredArgsConstructor
public class BaseMaternityAllowanceStrategy implements MaternityAllowanceStrategy {

    private final MaternityWageCalculatorService maternityWageCalculatorService;
    private final CityService cityService;
    private final AllowanceRulesService allowanceRulesService;
    private final WorkdayCalculatorService workdayCalculatorService;
    private final RequestDateCompensationService requestDateCompensationService;
    @Override
    public MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request) {

        String cityName = cityService.getEnabledCityChineseName(request.getCityCode());
        AllowanceRulesResponse allowanceRules = allowanceRulesService.getEnabledAllowanceRulesByCity(cityName);
        if (allowanceRules == null) {
            throw new IllegalArgumentException("不支持的城市代码: " + request.getCityCode());
        }

        BigDecimal paidWageInMaternity = getMaternityWage(request);
        if(isIndividual (allowanceRules)){
            validateRequest( request);
            paidWageInMaternity = null;
        }

        // a. 单位申报上年度月均工资计算补贴金额
        BigDecimal allowanceBasedCorporateSalary = getAllowanceBasedCorporateSalary(request,allowanceRules.getMonthDays());
        // b. 员工产前12个月月均工资计算补贴金额
        BigDecimal allowanceBasedEmployeeSalary = getAllowanceBasedEmployeeSalary(request,allowanceRules.getMonthDays());
        // c. 政府发放补贴金额
        BigDecimal governmentAllowance = request.getGovernmentAllowance() != null ?
                request.getGovernmentAllowance() : BigDecimal.ZERO;
        // d. 员工应享受补贴：MAX(a,b,c)
        BigDecimal maxAllowance = allowanceBasedCorporateSalary
                .max(allowanceBasedEmployeeSalary)
                .max(governmentAllowance);

        //  e. 补差 = 计算补差
        BigDecimal compensationAmount = BigDecimal.ZERO;
        if(isIndividual(allowanceRules)){
            compensationAmount = maxAllowance.subtract(governmentAllowance);
        }
        else {
            compensationAmount = maxAllowance.subtract(paidWageInMaternity);
        }

        // 构建详细说明列表
        List<String> allowanceCompensationDetails = new ArrayList<>();
        // 添加产假工资信息
        if (isCorporate(allowanceRules) ) {
            allowanceCompensationDetails.add(String.format("产假期间发放工资：%.2f元", paidWageInMaternity));
        }

        allowanceCompensationDetails.add(String.format("单位申报上年度月均工资计算补贴金额：%.2f÷%.2f天×%d=%.2f元",
                request.getUnitMonthlyAverageSalary(),
                allowanceBasedCorporateSalary,
                request.getMaternityLeaveDays(),
                allowanceBasedCorporateSalary));

        allowanceCompensationDetails.add(String.format("员工产前12个月月均工资计算补贴金额：%.2f元÷%.2f天×%d天=%.2f元",
                request.getAverageSalaryPast12Months(),
                allowanceRules.getMonthDays(),
                request.getMaternityLeaveDays(),
                allowanceBasedEmployeeSalary));

        allowanceCompensationDetails.add(String.format("员工应享受补贴：%.2f元", maxAllowance));

        allowanceCompensationDetails.add(String.format("政府发放补贴金额：%.2f元", governmentAllowance));

        if(isIndividual(allowanceRules)) {
            allowanceCompensationDetails.add(String.format("补差金额：员工应享受补贴%.2f元-政府发放补贴金额%.2f元=%.2f元",
                    maxAllowance, governmentAllowance, compensationAmount));
        }
        if(isCorporate(allowanceRules)) {
            allowanceCompensationDetails.add(String.format("补差金额：员工应享受补贴%.2f元-产假期间发放工资%.2f元=%.2f元",
                    maxAllowance, paidWageInMaternity, compensationAmount));
        }
        // 如果补差金额为负数，则返回0
        BigDecimal finalCompensationAmount = compensationAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : compensationAmount;

        MaternityAllowanceResponse response =
                MaternityAllowanceResponse.builder()
                .requestId(null) // 由Service层设置
                .resultId(null) // 由Service层设置
                .lanId(request.getLanId())
                .employeeName(request.getEmployeeName())
                .cityCode(request.getCityCode())
                .cityName(cityName)
                .allowanceDays(request.getMaternityLeaveDays())
                .extraAllowance(BigDecimal.ZERO) // 额外补贴暂设为0
                .maternityAllowance(maxAllowance) // 应享受补贴金额
                .compensationAmount(finalCompensationAmount) // 补差金额
                .paidMaternityWage(paidWageInMaternity) // 产假应付工资
                .employeeRefundAmount(null)
                .allowanceCompensationDetails(allowanceCompensationDetails)
                .refundDetails(null)
                .build();
        if(isCorporate(allowanceRules)){
            return response;
        }

        //返还计算
        return reFund(request,allowanceRules,response);
    }

    private MaternityAllowanceResponse reFund(MaternityAllowanceRequest request,AllowanceRulesResponse allowanceRules,MaternityAllowanceResponse response){

        // 获取第一个月和最后一个月年月信息
        int firstCompleteYear = request.getMaternityLeaveStartDate().getYear();
        int firstCompleteMonth = request.getMaternityLeaveStartDate().getMonthValue();
        int startDay = request.getMaternityLeaveStartDate().getDayOfMonth();
        int startingYear = request.getMaternityLeaveStartDate().getYear();
        int startingMonth = request.getMaternityLeaveStartDate().getMonthValue();

        int lastCompleteYear = request.getMaternityLeaveEndDate().getYear();
        int lastCompleteMonth = request.getMaternityLeaveEndDate().getMonthValue();
        int endingYear = request.getMaternityLeaveEndDate().getYear();
        int endingMonth = request.getMaternityLeaveEndDate().getMonthValue();

        // 获取产假期间月份数
        List<MonthlyWorkdayInfoDO> monthlyWorkdayList = workdayCalculatorService.calculateMonthlyWorkdays(
                request.getMaternityLeaveStartDate(), request.getMaternityLeaveEndDate());

        boolean baseSalaryAdjusted = maternityWageCalculatorService.crossesSalaryAdjustMonth(monthlyWorkdayList,allowanceRules.getSalaryAdjustMonth());
        boolean socialInsuranceBaseAdjusted = maternityWageCalculatorService.crossesSocialAdjustMonth(monthlyWorkdayList,allowanceRules.getSocialAdjustMonth());

        // 计算产假第一个月与最后一个月工资
        BigDecimal startingMonthMaternityWage = BigDecimal.ZERO;
        boolean startingMonthFullMonth = true;
        BigDecimal endingMonthMaternityWage = BigDecimal.ZERO;
        boolean endingMonthFullMonth = true;
        BigDecimal adjustedMonthBaseSalary = baseSalaryAdjusted && request.getAdjustedMonthlyBaseSalary() != null
                ? request.getAdjustedMonthlyBaseSalary() : request.getMonthlyBaseSalary();

        if (request.getMaternityLeaveStartDate() != null && request.getMaternityLeaveEndDate() != null && !monthlyWorkdayList.isEmpty()) {
            if (!monthlyWorkdayList.get(0).getFullMonth()) {
                startingMonthMaternityWage = maternityWageCalculatorService.calculateStartingMonthMaternityWage(
                        request.getMaternityLeaveStartDate(),
                        request.getMaternityLeaveEndDate(),
                        request.getMonthlyBaseSalary()
                );
                startingMonthFullMonth = false;
            }

            if (!monthlyWorkdayList.get(monthlyWorkdayList.size() - 1).getFullMonth()) {
                endingMonthMaternityWage = maternityWageCalculatorService.calculateEndingMonthMaternityWage(
                        request.getMaternityLeaveStartDate(),
                        request.getMaternityLeaveEndDate(),
                        adjustedMonthBaseSalary
                );
                endingMonthFullMonth = false;
            }
        }

        long completeMonths = monthlyWorkdayList.stream()
                .mapToLong(workday -> workday.getFullMonth() ? 1L : 0L)
                .sum();

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

        List<MonthlyWorkdayInfoDO> completeMonthsList = monthlyWorkdayList.stream()
                .filter(MonthlyWorkdayInfoDO::getFullMonth)
                .toList();
        if (!completeMonthsList.isEmpty()) {
            MonthlyWorkdayInfoDO firstCompleteMonthInfo = completeMonthsList.get(0);
            MonthlyWorkdayInfoDO lastCompleteMonthInfo = completeMonthsList.get(completeMonthsList.size() - 1);
            firstCompleteYear = firstCompleteMonthInfo.getYear();
            firstCompleteMonth = firstCompleteMonthInfo.getMonth();
            lastCompleteYear = lastCompleteMonthInfo.getYear();
            lastCompleteMonth = lastCompleteMonthInfo.getMonth();
        }

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
                if (firstMonthWage.compareTo(BigDecimal.ZERO) < 0) {
                    refundAmount = refundAmount.subtract(firstMonthWage);
                }
        }

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
        BigDecimal requestDateCompensation = (BigDecimal) requestDateCompensationResult.getOrDefault("compensation", BigDecimal.ZERO);
        String requestDateCompensationDetail = (String) requestDateCompensationResult.getOrDefault("refundDetail", "");

        refundAmount = refundAmount.add(requestDateCompensation);
        response.setEmployeeRefundAmount(refundAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : refundAmount);

        List<String> refundDetailsList = new ArrayList<>();

        if (!startingMonthFullMonth) {
                StringBuilder wageFormula1stMonth = new StringBuilder();
                wageFormula1stMonth.append(String.format("%.2f-%.2f",
                        request.getMonthlyBaseSalary(), startingMonthMaternityWage));

                if (socialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                    wageFormula1stMonth.append(String.format("-%.2f", socialInsuranceBase));
                }
                if (espp.compareTo(BigDecimal.ZERO) > 0) {
                    wageFormula1stMonth.append(String.format("-%.2f", espp));
                }
                if (unionFee.compareTo(BigDecimal.ZERO) > 0) {
                    wageFormula1stMonth.append(String.format("-%.2f", unionFee));
                }
                refundDetailsList.add(String.format("%d.%d 工资不够扣：%s=%.2f元",
                        startingYear, startingMonth, wageFormula1stMonth, firstMonthWage.abs()));
                refundDetailsList.add(String.format("产假工资折算 %d年%d月，扣除：%.2f元", startingYear, startingMonth, startingMonthMaternityWage));
        }

        if (!endingMonthFullMonth) {
            StringBuilder wageFormula = new StringBuilder();
            wageFormula.append(String.format("%.2f-%.2f",
                    adjustedMonthBaseSalary, endingMonthMaternityWage));

            if (adjustedSocialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                wageFormula.append(String.format("-%.2f", adjustedSocialInsuranceBase));
            }
            if (espp.compareTo(BigDecimal.ZERO) > 0) {
                wageFormula.append(String.format("-%.2f", espp));
            }
            if (unionFee.compareTo(BigDecimal.ZERO) > 0) {
                wageFormula.append(String.format("-%.2f", unionFee));
            }

            if (lastMonthWage.compareTo(BigDecimal.ZERO) >= 0) {
                refundDetailsList.add(String.format("%d.%d 工资剩余：%s=%.2f元",
                        endingYear, endingMonth, wageFormula, lastMonthWage.abs()));
            } else {
                refundDetailsList.add(String.format("%d.%d 工资不够扣：%s=%.2f元",
                        endingYear, endingMonth, wageFormula, lastMonthWage.abs()));
            }

            refundDetailsList.add(String.format("产假工资折算 %d年%d月，扣除：%.2f元", endingYear, endingMonth, endingMonthMaternityWage));
        }

        refundDetailsList.add(String.format("月度个人部分社保公积金合计：%.2f元", socialInsuranceBase));
        if (socialInsuranceBaseAdjusted) {
            refundDetailsList.add(String.format("调整后月度个人部分社保公积金合计：%.2f元", adjustedSocialInsuranceBase));
        }
        if (espp.compareTo(BigDecimal.ZERO) > 0) {
            refundDetailsList.add(String.format("%d.%d-%d.%d月ESPP：%.2f×%d=%.2f元",
                    firstCompleteYear, firstCompleteMonth, lastCompleteYear, lastCompleteMonth, espp, completeMonths, espp.multiply(new BigDecimal(completeMonths))));
        }
        if (unionFee.compareTo(BigDecimal.ZERO) > 0) {
            refundDetailsList.add(String.format("%d.%d-%d.%d月工会费：%.2f×%d=%.2f元",
                    firstCompleteYear, firstCompleteMonth, lastCompleteYear, lastCompleteMonth, unionFee, completeMonths, unionFee.multiply(new BigDecimal(completeMonths))));
        }
        if (requestDateCompensation.compareTo(BigDecimal.ZERO) > 0 && !requestDateCompensationDetail.isEmpty()) {
            refundDetailsList.add(requestDateCompensationDetail);
        }

        StringBuilder formula = new StringBuilder("返还金额：");
        if (request.getCompanyAdvance() != null) {
            if (socialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                formula.append(String.format("%.2f", request.getCompanyAdvance().calculateSocialInsuranceBaseByMonth(socialInsuranceBase, monthlyWorkdayList)));
            }
            if (espp.compareTo(BigDecimal.ZERO) > 0) {
                formula.append("+").append(String.format("%.2f", espp.multiply(new BigDecimal(completeMonths))));
            }
            if (unionFee.compareTo(BigDecimal.ZERO) > 0) {
                formula.append("+").append(String.format("%.2f", unionFee.multiply(new BigDecimal(completeMonths))));
            }

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
                formula.append("+").append(String.format("%.2f", startingMonthMaternityWage.abs()));
            }

            formula.append("=").append(String.format("%.2f元", refundAmount));
            if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                formula.append("（计算结果为负，取0）");
            }
        } else {
            formula.append(String.format("返还金额：%.2f-%.2f=%.2f元",
                    companyAdvanceSum, lastMonthWage, refundAmount));
            if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                formula.append("（计算结果为负，取0）");
            }
        }
        refundDetailsList.add(formula.toString());
        response.setRefundDetails(refundDetailsList);

        return response;
    }

    private BigDecimal getAllowanceBasedCorporateSalary(MaternityAllowanceRequest request,BigDecimal monthDays) {
        // a. 单位申报上年度月均工资计算补贴金额
        BigDecimal unitDeclaredAllowance = BigDecimal.ZERO;
        if (request.getUnitMonthlyAverageSalary() != null) {
            unitDeclaredAllowance = request.getUnitMonthlyAverageSalary()
                    .multiply(new BigDecimal(request.getMaternityLeaveDays()))
                    .divide(monthDays, 2, RoundingMode.HALF_UP);
        }
        return unitDeclaredAllowance;
    }

    private BigDecimal getMaternityWage(MaternityAllowanceRequest request) {
        // 调用 MaternityWageCalculatorService 计算产假应付工资
        BigDecimal paidMaternityWage = BigDecimal.ZERO;
        if (request.getMonthlyBaseSalary() != null) {
            paidMaternityWage = maternityWageCalculatorService.calculateMaternityWage(
                    request.getMaternityLeaveStartDate(),
                    request.getMaternityLeaveEndDate(),
                    request.getMonthlyBaseSalary(),
                    request.getAdjustedMonthlyBaseSalary()
            );
        }
        return paidMaternityWage;
    }

    private BigDecimal getAllowanceBasedEmployeeSalary(MaternityAllowanceRequest request, BigDecimal monthDays) {
        // 计算生育津贴：产前12个月月均工资 / 30 * 产假天数，四舍五入保留2位小数
        BigDecimal maternityAllowance = request.getAverageSalaryPast12Months()
                .multiply(new BigDecimal(request.getMaternityLeaveDays()))
                .divide(monthDays, 2, RoundingMode.HALF_UP);
        return maternityAllowance;
    }
    
    /**
     * 计算补差金额
     */
    private BigDecimal calculateCompensation(BigDecimal allowanceBasedEmployeeSalary, 
                                           BigDecimal governmentAllowance, 
                                           BigDecimal paidMaternityWage) {
        if (governmentAllowance == null) {
            governmentAllowance = BigDecimal.ZERO;
        }
        if (paidMaternityWage == null) {
            paidMaternityWage = BigDecimal.ZERO;
        }
        
        // 如果政府发放金额大于生育津贴，补差 = 政府发放金额 - 已发放产假期间工资
        if (governmentAllowance.compareTo(allowanceBasedEmployeeSalary) > 0) {
            return governmentAllowance.subtract(paidMaternityWage);
        } else {
            // 否则补差 = 生育津贴 - 已发放产假期间工资
            return allowanceBasedEmployeeSalary.subtract(paidMaternityWage);
        }
    }

    @Override
    public String getSupportedCityCode() {
        return  CityEnum.SHENZHEN.getCode();
    }

    private boolean isIndividual(AllowanceRulesResponse allowanceRules) {
        return allowanceRules.getPayoutMethod() == PAYOUT_METHOD_INDIVIDUAL;
    }
    private boolean isCorporate(AllowanceRulesResponse allowanceRules) {
        return allowanceRules.getPayoutMethod() == PAYOUT_METHOD_CORPORATE;
    }
    private void validateRequest(MaternityAllowanceRequest request) {
        // 验证上海特有的计算所需字段
        if (request.getMonthlyBaseSalary() == null) {
            throw new IllegalArgumentException("生育津贴计算需要提供月基本工资");
        }

        if ((request.getCompanyAdvance().getAddItem().get(AddDeleteItemEnum.SOCIAL_INSURANCE_BASE.getCode()) == null)) {
            throw new IllegalArgumentException("生育津贴计算需要提供社保基数总和");
        }
    }

}

