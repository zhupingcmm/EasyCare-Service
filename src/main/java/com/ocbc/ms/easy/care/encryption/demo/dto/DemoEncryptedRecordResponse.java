package com.ocbc.ms.easy.care.encryption.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Demo 出库响应 DTO。
 */
@Data
@Builder
public class DemoEncryptedRecordResponse {

    private UUID id;
    private String plainKeyword;
    private String nationalId;
    private BigDecimal monthlySalary;
    private LocalDate hireDate;
    private Integer childCount;
    private Boolean hasAllowance;
    private BigInteger retirementAccount;
    private BigDecimal newAccount;
}
