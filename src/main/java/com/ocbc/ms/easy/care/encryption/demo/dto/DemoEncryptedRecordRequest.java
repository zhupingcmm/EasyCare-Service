package com.ocbc.ms.easy.care.encryption.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

/**
 * Demo 入库请求 DTO。
 */
@Data
public class DemoEncryptedRecordRequest {

    @NotBlank(message = "plainKeyword 不能为空")
    private String plainKeyword;

    @NotBlank(message = "nationalId 不能为空")
    private String nationalId;

    @NotNull(message = "monthlySalary 不能为空")
    private BigDecimal monthlySalary;

    @NotNull(message = "hireDate 不能为空")
    private LocalDate hireDate;

    @NotNull(message = "childCount 不能为空")
    private Integer childCount;

    @NotNull(message = "hasAllowance 不能为空")
    private Boolean hasAllowance;

    @NotNull(message = "retirementAccount 不能为空")
    private BigInteger retirementAccount;

    @NotNull(message = "newAccount 不能为空")
    private BigDecimal newAccount;
}
