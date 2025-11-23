package com.hr.maternity.entity;

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
import java.time.LocalDateTime;
import java.util.List;

/**
 * 津贴计算结果实体类
 */
@Entity
@Table(name = "maternity_allowance_result")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class MaternityAllowanceResultDO {

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

    @Column(name = "city_name", length = 100)
    private String cityName;

    @Column(name = "allowance_days")
    private Integer allowanceDays;

    @Column(name = "extra_allowance", precision = 15, scale = 2)
    private BigDecimal extraAllowance;

    @Column(name = "maternity_allowance", precision = 15, scale = 2)
    private BigDecimal maternityAllowance;

    @Column(name = "compensation_amount", precision = 15, scale = 2)
    private BigDecimal compensationAmount;

    @Column(name = "paid_maternity_wage", precision = 15, scale = 2)
    private BigDecimal paidMaternityWage;

    @Column(name = "employee_refund_amount", precision = 15, scale = 2)
    private BigDecimal employeeRefundAmount;

    @Column(name = "allowance_compensation_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> allowanceCompensationDetails;

    @Column(name = "refund_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> refundDetails;

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
