package com.ocbc.ms.easy.care.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 津贴规则实体类
 */
@Entity
@Table(name = "t_allowance_rules")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class AllowanceRules {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 城市关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private CityDO city;

    /**
     * 发放方式
     */
    @Column(name = "payout_method", nullable = false)
    private Integer payoutMethod = 1;

    /**
     * 是否启用（用于逻辑删除）
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 是否需要补差
     */
    @Column(name = "need_compensation")
    private Boolean needCompensation = true;

    /**
     * 薪资调整月份
     */
    @Column(name = "salary_adjust_month")
    private Integer salaryAdjustMonth = 4;

    /**
     * 社保调整月份
     */
    @Column(name = "social_adjust_month")
    private Integer socialAdjustMonth = 7;

    /**
     *  一个月的天数，计算日薪资时使用
     */
    @Column(name = "month_days")
    private BigDecimal monthDays = BigDecimal.valueOf(30);

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "create_by", length = 100, updatable = false)
    private String createBy;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_date")
    private LocalDateTime updateDate;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "update_by", length = 100)
    private String updateBy;
}
