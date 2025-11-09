package com.hr.maternity.service;

import com.hr.maternity.dto.BatchTestResultDTO;
import com.hr.maternity.dto.TestCaseResultDTO;
import com.hr.maternity.dto.MaternityTestCaseRowDTO;
import com.hr.maternity.dto.MaternityLeaveRequest;
import com.hr.maternity.dto.MaternityLeaveResponse;
import com.hr.maternity.dto.MaternityAllowanceRequest;
import com.hr.maternity.dto.MaternityAllowanceResponse;
import com.hr.maternity.dto.CompanyAdvanceMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 批量测试服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchTestService {

    private final ExcelParseService excelParseService;
    private final MaternityLeaveService maternityLeaveService;
    private final MaternityAllowanceService maternityAllowanceService;
    
    // 线程池：使用可用处理器数量的线程
    private final ExecutorService executorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );

    /**
     * 执行批量测试
     * 
     * @param file Excel 文件
     * @return 批量测试结果
     * @throws IOException 文件读取异常
     */
    public BatchTestResultDTO runBatchTest(MultipartFile file) throws IOException {
        log.info("开始执行批量测试，文件名: {}", file.getOriginalFilename());
        
        long startTime = System.currentTimeMillis();
        
        // 解析 Excel 文件
        List<MaternityTestCaseRowDTO> testCases = excelParseService.parseMaternityTestCaseFile(file);
        
        if (testCases.isEmpty()) {
            log.warn("Excel 文件中没有测试用例数据");
            return buildEmptyResult(System.currentTimeMillis() - startTime);
        }
        
        // 并行执行测试用例
        log.info("使用 {} 个线程并行执行测试用例", Runtime.getRuntime().availableProcessors());
        
        List<CompletableFuture<TestCaseResultDTO>> futures = new ArrayList<>();
        
        for (int i = 0; i < testCases.size(); i++) {
            final int index = i;
            final MaternityTestCaseRowDTO testCase = testCases.get(index);
            final int caseNumber = index + 2; // Excel 行号（从第2行开始，第1行是表头）
            
            CompletableFuture<TestCaseResultDTO> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return executeTestCase(caseNumber, testCase);
                } catch (Exception e) {
                    log.error("执行测试用例 {} 失败", caseNumber, e);
                    return TestCaseResultDTO.builder()
                        .caseNumber(testCase.getCaseNumber())
                        .caseDescription(testCase.getCaseDescription())
                        .cityCode(testCase.getCityCode())
                        .isSuccess(false)
                        .inputData(Map.of("row", testCase))
                        .errorMessage("执行异常: " + e.getMessage())
                        .build();
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        // 收集结果
        List<TestCaseResultDTO> details = allFutures.thenApply(v -> 
            futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList())
        ).join();
        
        // 统计成功和失败数量
        int successCount = (int) details.stream().filter(TestCaseResultDTO::getIsSuccess).count();
        int failureCount = details.size() - successCount;
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // 构建结果
        BatchTestResultDTO result = BatchTestResultDTO.builder()
            .totalCount(testCases.size())
            .successCount(successCount)
            .failureCount(failureCount)
            .successRate(calculateSuccessRate(successCount, testCases.size()))
            .details(details)
            .executionTimeMs(executionTime)
            .build();
        
        log.info("批量测试完成，总数: {}, 成功: {}, 失败: {}, 成功率: {}%, 耗时: {}ms",
            result.getTotalCount(), result.getSuccessCount(), result.getFailureCount(),
            String.format("%.2f", result.getSuccessRate() * 100), result.getExecutionTimeMs());
        
        return result;
    }



    /**
     * 执行单个测试用例
     * 1. 调用 MaternityLeaveService.calculateMaternityLeave 计算产假
     * 2. 调用 MaternityAllowanceService.calculateMaternityAllowance 计算津贴
     * 
     * @param caseNumber 用例编号
     * @param testCase 测试用例数据
     * @return 测试结果
     */
    private TestCaseResultDTO executeTestCase(int caseNumber, MaternityTestCaseRowDTO testCase) {
        log.debug("执行测试用例 {}: {}", caseNumber, testCase.getCaseNumber());
        
        try {
            // 1. 构建产假计算请求
            MaternityLeaveRequest leaveRequest = buildMaternityLeaveRequest(testCase);
            
            // 2. 调用产假计算服务
            MaternityLeaveResponse leaveResponse = maternityLeaveService.calculateMaternityLeave(leaveRequest);
            log.debug("产假计算完成: 总天数={}, 结束日期={}", 
                leaveResponse.getTotalDays(), leaveResponse.getEndDate());
            
            // 3. 构建津贴计算请求（使用产假计算结果）
            MaternityAllowanceRequest allowanceRequest = buildMaternityAllowanceRequest(testCase, leaveResponse);
            
            // 4. 调用津贴计算服务
            MaternityAllowanceResponse allowanceResponse = maternityAllowanceService.calculateMaternityAllowance(allowanceRequest);
            log.debug("津贴计算完成: 应享受津贴={}, 需补差={}", 
                allowanceResponse.getMaternityAllowance(), allowanceResponse.getCompensationAmount());
            
            // 5. 比较实际结果与期望结果
            Map<String, Object> actualResult = Map.of(
                "leaveResponse", leaveResponse,
                "allowanceResponse", allowanceResponse
            );
            
            boolean isSuccess = compareTestResults(testCase.getExpectedResult(), leaveResponse, allowanceResponse);
            
            return TestCaseResultDTO.builder()
                .caseNumber(testCase.getCaseNumber())
                .caseDescription(testCase.getCaseDescription())
                .cityCode(testCase.getCityCode())
                .isSuccess(isSuccess)
                .inputData(Map.of("testCase", testCase))
                .expectedResult(testCase.getExpectedResult())
                .actualResult(actualResult)
                .errorMessage(isSuccess ? null : "实际结果与期望结果不匹配")
                .build();
                
        } catch (Exception e) {
            log.error("执行测试用例 {} 失败: {}", caseNumber, e.getMessage(), e);
            return TestCaseResultDTO.builder()
                .caseNumber(testCase.getCaseNumber())
                .caseDescription(testCase.getCaseDescription())
                .cityCode(testCase.getCityCode())
                .isSuccess(false)
                .inputData(Map.of("testCase", testCase))
                .expectedResult(testCase.getExpectedResult())
                .actualResult(null)
                .errorMessage("执行异常: " + e.getMessage())
                .build();
        }
    }

    /**
     * 构建产假计算请求
     */
    private MaternityLeaveRequest buildMaternityLeaveRequest(MaternityTestCaseRowDTO testCase) {
        MaternityLeaveRequest request = new MaternityLeaveRequest();
        request.setLanId(testCase.getEmployeeId());
        request.setEmployeeName(testCase.getEmployeeName());
        request.setCityCode(testCase.getCityCode());
        
        MaternityTestCaseRowDTO.MaternityCalcInfo calcInfo = testCase.getMaternityCalcInfo();
        if (calcInfo != null) {
            request.setExpectedDeliveryDate(calcInfo.getLeaveStartDate());
            request.setNumberOfBabies(calcInfo.getNumberOfBabies() != null ? calcInfo.getNumberOfBabies() : 1);
            request.setIsMultipleBirth(calcInfo.getNumberOfBabies() != null && calcInfo.getNumberOfBabies() > 1);
            request.setHasExtendedDays(calcInfo.getHasRewardLeave() != null ? calcInfo.getHasRewardLeave() : false);
            
            // 根据生育方式判断是否难产
            boolean isDifficult = calcInfo.getDeliveryMethod() != null && calcInfo.getDeliveryMethod().contains("难产");
            request.setIsDifficultBirth(isDifficult);
            
            // 处理广州难产类型（如果有）
            if (isDifficult) {
                // 广州难产类型1: 30天, 类型2: 15天
                if (calcInfo.getGuangzhouDifficultType1() != null && !calcInfo.getGuangzhouDifficultType1().isEmpty()) {
                    request.setAdditionalDystociaDays(30);
                } else if (calcInfo.getGuangzhouDifficultType2() != null && !calcInfo.getGuangzhouDifficultType2().isEmpty()) {
                    request.setAdditionalDystociaDays(15);
                }
            }
        }
        // todo check
        request.setIsMiscarriage(false);
        request.setIsBreastFeeding(false);
        
        return request;
    }
    
    /**
     * 构建津贴计算请求
     */
    private MaternityAllowanceRequest buildMaternityAllowanceRequest(
            MaternityTestCaseRowDTO testCase, 
            MaternityLeaveResponse leaveResponse) {
        
        MaternityAllowanceRequest request = new MaternityAllowanceRequest();
        request.setLanId(testCase.getEmployeeId());
        request.setEmployeeName(testCase.getEmployeeName());
        request.setCityCode(testCase.getCityCode());
        
        // 使用产假计算结果
        request.setMaternityLeaveDays(leaveResponse.getBaseDays());
        request.setMaternityLeaveStartDate(leaveResponse.getStartDate());
        request.setMaternityLeaveEndDate(leaveResponse.getEndDate());
        
        // 使用津贴计算信息
        MaternityTestCaseRowDTO.AllowanceCalcInfo allowanceInfo = testCase.getAllowanceCalcInfo();
        if (allowanceInfo != null) {
            request.setAverageSalaryPast12Months(allowanceInfo.getAvgSalaryBefore12Months());
            request.setGovernmentAllowance(allowanceInfo.getGovernmentAllowance());
            request.setUnitMonthlyAverageSalary(allowanceInfo.getDeclaredAvgSalaryLastYear());
            request.setMonthlyBaseSalary(allowanceInfo.getBaseSalary());
            
            // 处理调薪后工资
            if (Boolean.TRUE.equals(allowanceInfo.getHasSalaryAdjustmentInApril())) {
                request.setAdjustedMonthlyBaseSalary(allowanceInfo.getSalaryAfterAdjustment());
            }
            
            // 构建公司垫付信息
            CompanyAdvanceMap companyAdvance = buildCompanyAdvanceMap(testCase);
            request.setCompanyAdvance(companyAdvance);
        }
        
        return request;
    }
    
    /**
     * 构建公司垫付信息
     */
    private CompanyAdvanceMap buildCompanyAdvanceMap(MaternityTestCaseRowDTO testCase) {
        CompanyAdvanceMap companyAdvance = new CompanyAdvanceMap();
        
        MaternityTestCaseRowDTO.AllowanceCalcInfo allowanceInfo = testCase.getAllowanceCalcInfo();
        
        // 构建 addItem
        Map<String, BigDecimal> addItem = new java.util.HashMap<>();
        if (allowanceInfo != null) {

            // 弹性福利 -> flexibleBenefit
            if (allowanceInfo.getFlexibleBenefit() != null) {
                addItem.put("flexibleBenefit", allowanceInfo.getFlexibleBenefit());
            }
            // espp → ESPP
            if (allowanceInfo.getEspp() != null) {
                addItem.put("espp", allowanceInfo.getEspp());
            }
            
            // 调整前个人社保公积金合计 → adjustedSocialInsuranceBase
            if (allowanceInfo.getSocialSecurityBeforeAdjustment() != null) {
                addItem.put("adjustedSocialInsuranceBase", allowanceInfo.getSocialSecurityBeforeAdjustment());
            }
            
            // 调整后个人社保公积金合计 → socialInsuranceBase
            if (allowanceInfo.getSocialSecurityAfterAdjustment() != null) {
                addItem.put("socialInsuranceBase", allowanceInfo.getSocialSecurityAfterAdjustment());
            }

            // 个人工会费 -> unionFee
            if (allowanceInfo.getUnionFee() != null) {
                addItem.put("unionFee", allowanceInfo.getUnionFee());
            }
            // 其他奖励项目 -> otherRewards
            if (allowanceInfo.getOtherRewards() != null) {
                addItem.put("otherRewards", allowanceInfo.getOtherRewards());
            }

        }
        companyAdvance.setAddItem(addItem);
        
        // 构建 deleteItem
        Map<String, BigDecimal> deleteItem = new java.util.HashMap<>();
        if (allowanceInfo != null) {
            // spot on → spot on
            if (allowanceInfo.getSpotOn() != null) {
                deleteItem.put("spotOn", allowanceInfo.getSpotOn());
            }

            // 其他扣除项 -> otherDeductions
            if (allowanceInfo.getOtherDeductions() != null) {
                deleteItem.put("otherDeductions", allowanceInfo.getOtherDeductions());
            }

        }
        companyAdvance.setDeleteItem(deleteItem);
        
        return companyAdvance;
    }
    
    /**
     * 比较测试结果
     */
    private boolean compareTestResults(
            MaternityTestCaseRowDTO.ExpectedResult expected,
            MaternityLeaveResponse leaveResponse,
            MaternityAllowanceResponse allowanceResponse) {
        
        if (expected == null) {
            return true; // 如果没有期望结果，认为成功
        }
        
        boolean isSuccess = true;
        
        // 比较产假结束日期
        if (expected.getExpectedLeaveEndDate() != null && leaveResponse.getEndDate() != null) {
            if (!expected.getExpectedLeaveEndDate().equals(leaveResponse.getEndDate())) {
                log.warn("产假结束日期不匹配: 期望={}, 实际={}", 
                    expected.getExpectedLeaveEndDate(), leaveResponse.getEndDate());
                isSuccess = false;
            }
        }
        
        // 比较总产假天数
        if (expected.getExpectedTotalLeaveDays() != null && leaveResponse.getTotalDays() != null) {
            if (!expected.getExpectedTotalLeaveDays().equals(leaveResponse.getTotalDays())) {
                log.warn("总产假天数不匹配: 期望={}, 实际={}", 
                    expected.getExpectedTotalLeaveDays(), leaveResponse.getTotalDays());
                isSuccess = false;
            }
        }
        
        // 比较预计返岗日期
        if (expected.getExpectedReturnDate() != null && leaveResponse.getReturnToWorkDate() != null) {
            if (!expected.getExpectedReturnDate().equals(leaveResponse.getReturnToWorkDate())) {
                log.warn("预计返岗日期不匹配: 期望={}, 实际={}", 
                    expected.getExpectedReturnDate(), leaveResponse.getReturnToWorkDate());
                isSuccess = false;
            }
        }
        
        // 比较应享受津贴
        if (expected.getExpectedTotalAllowance() != null && allowanceResponse.getMaternityAllowance() != null) {
            if (expected.getExpectedTotalAllowance().compareTo(allowanceResponse.getMaternityAllowance()) != 0) {
                log.warn("应享受津贴不匹配: 期望={}, 实际={}", 
                    expected.getExpectedTotalAllowance(), allowanceResponse.getMaternityAllowance());
                isSuccess = false;
            }
        }
        
        // 比较需补差金额
        if (expected.getExpectedSupplementAmount() != null && allowanceResponse.getCompensationAmount() != null) {
            if (expected.getExpectedSupplementAmount().compareTo(allowanceResponse.getCompensationAmount()) != 0) {
                log.warn("需补差金额不匹配: 期望={}, 实际={}", 
                    expected.getExpectedSupplementAmount(), allowanceResponse.getCompensationAmount());
                isSuccess = false;
            }
        }
        
        // 比较返还金额
        if (expected.getExpectedRefundAmount() != null && allowanceResponse.getEmployeeRefundAmount() != null) {
            if (expected.getExpectedRefundAmount().compareTo(allowanceResponse.getEmployeeRefundAmount()) != 0) {
                log.warn("返还金额不匹配: 期望={}, 实际={}", 
                    expected.getExpectedRefundAmount(), allowanceResponse.getEmployeeRefundAmount());
                isSuccess = false;
            }
        }
        
        return isSuccess;
    }

    /**
     * 计算成功率
     */
    private Double calculateSuccessRate(int successCount, int totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount;
    }

    /**
     * 构建空结果
     */
    private BatchTestResultDTO buildEmptyResult(long executionTime) {
        return BatchTestResultDTO.builder()
            .totalCount(0)
            .successCount(0)
            .failureCount(0)
            .successRate(0.0)
            .details(new ArrayList<>())
            .executionTimeMs(executionTime)
            .build();
    }
    
    /**
     * 服务销毁时关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        log.info("关闭批量测试服务线程池");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                log.warn("线程池未能在60秒内正常关闭，强制关闭");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("线程池关闭被中断", e);
        }
    }
}
