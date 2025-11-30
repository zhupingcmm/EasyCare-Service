package com.hr.maternity.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 产假规则实体类
 */
@Entity
@Table(name = "t_maternity_rules")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class MaternityRules {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 城市
     */
    @Column(name = "city", nullable = false, length = 50)
    private String city;

    /**
     * 产假类型（如：产假、陪产假）
     */
    @Column(name = "maternity_leave_type", nullable = false, length = 100)
    private String maternityLeaveType;

    /**
     * 流产类型（如：早期流产、晚期流产，可为空）
     */
    @Column(name = "abortion_leave_type", length = 100)
    private String abortionLeaveType;

    /**
     * 假期天数
     */
    @Column(name = "leave_days", nullable = false)
    private Integer leaveDays;

    /**
     * 是否节假日顺延
     */
    @Column(name = "is_extendable", nullable = false)
    private Boolean isExtendable = false;

    /**
     * 是否有津贴
     */
    @Column(name = "has_allowance", nullable = false)
    private Boolean hasAllowance = true;

    /**
     * 是否默认选择
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /**
     * 单选分组标识
     */
    @Column(name = "radio_group", nullable = false)
    private Integer radioGroup = 0;

    /**
     * 是否启用
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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
