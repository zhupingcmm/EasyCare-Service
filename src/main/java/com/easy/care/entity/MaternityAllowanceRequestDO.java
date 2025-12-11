package com.easy.care.entity;

import com.easy.care.dto.CompanyAdvanceMap;
import com.easy.care.encryption.annotation.EncryptedField;
import com.easy.care.encryption.converter.Base64AttributeConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 津贴申请记录实体类
 */
@Entity
@Table(name = "maternity_allowance_request")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class MaternityAllowanceRequestDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name = "lan_id", nullable = false, length = 50)
    private String lanId;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    @Column(name = "city_code", nullable = false, length = 50)
    private String cityCode;

    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "unit_monthly_average_salary", precision = 15, scale = 2)
    private BigDecimal unitMonthlyAverageSalary;

    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "monthly_base_salary", precision = 15, scale = 2)
    private BigDecimal monthlyBaseSalary;

    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "adjusted_monthly_base_salary", precision = 15, scale = 2)
    private BigDecimal adjustedMonthlyBaseSalary;

    @EncryptedField
    @Convert(converter = Base64AttributeConverter.class)
    @Column(name = "average_salary_past_12_months", nullable = false, precision = 15, scale = 2)
    private BigDecimal averageSalaryPast12Months;

    @Column(name = "maternity_leave_days", nullable = false)
    private Integer maternityLeaveDays;

    @Column(name = "maternity_leave_start_date", nullable = false)
    private LocalDate maternityLeaveStartDate;

    @Column(name = "maternity_leave_end_date", nullable = false)
    private LocalDate maternityLeaveEndDate;

    @Column(name = "maternity_leave_request_date")
    private LocalDate maternityLeaveRequestDate;

    @Column(name = "company_advance", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private CompanyAdvanceMap companyAdvance;

    @Column(name = "government_allowance", nullable = false, precision = 15, scale = 2)
    private BigDecimal governmentAllowance;

    @CreatedDate
    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @CreatedBy
    @Column(name = "create_by", length = 100, updatable = false)
    private String createBy;

    @LastModifiedDate
    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @LastModifiedBy
    @Column(name = "update_by", length = 100)
    private String updateBy;
}
