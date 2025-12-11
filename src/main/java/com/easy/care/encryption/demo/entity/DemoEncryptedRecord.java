package com.easy.care.encryption.demo.entity;

import com.easy.care.encryption.annotation.EncryptedField;
import com.easy.care.encryption.converter.Base64AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Demo 实体：展示如何对不同字段类型进行 Base64 字段级加密。
 */
@Entity
@Table(name = "demo_encrypted_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoEncryptedRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** 非加密字段，便于对比查看。 */
    @Column(name = "plain_keyword", nullable = false, length = 120)
    private String plainKeyword;

    /** 字符串类型加密示例。 */
    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "national_id_enc", nullable = false, columnDefinition = "text")
    private String nationalId;

    /** 数值类型加密示例。 */
    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "monthly_salary_enc", nullable = false, columnDefinition = "text")
    private BigDecimal monthlySalary;

    /** 日期类型加密示例。 */
    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "hire_date_enc", nullable = false, columnDefinition = "text")
    private LocalDate hireDate;

    /** 整型加密示例。 */
    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "child_count_enc", nullable = false, columnDefinition = "text")
    private Integer childCount;

    /** 布尔类型加密示例。 */
    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "has_allowance_enc", nullable = false, columnDefinition = "text")
    private Boolean hasAllowance;

    /** BigInteger 类型加密示例。 */
    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "retirement_account_enc", nullable = false, columnDefinition = "text")
    private BigInteger retirementAccount;

    /** BigDecimal 类型加密示例。 */
    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "new_account_enc", nullable = false, columnDefinition = "text")
    private BigDecimal newAccount;
}
