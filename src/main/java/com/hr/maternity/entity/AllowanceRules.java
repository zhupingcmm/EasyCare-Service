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
     * 城市
     */
    @Column(name = "city", nullable = false, length = 50)
    private String city;

    /**
     * 发放方式
     */
    @Column(name = "payout_method", nullable = false, length = 20)
    private String payoutMethod;

    /**
     * 是否启用（用于逻辑删除）
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
