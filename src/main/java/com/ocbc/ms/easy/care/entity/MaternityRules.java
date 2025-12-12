package com.ocbc.ms.easy.care.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
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

import java.time.LocalDateTime;
import java.util.Map;

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
     * 城市ID（外键关联）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false, foreignKey = @ForeignKey(name = "fk_city"))
    private CityDO city;

    /**
     * 产假类型（外键关联）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maternity_leave_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_maternity_leave_type"))
    private MaternityLeaveType maternityLeaveType;

    /**
     * 默认假期天数
     */
    @Column(name = "default_days", nullable = false)
    private Integer defaultDays;

    /**
     * 医嘱天数
     */
    @Column(name = "doctor_recommend_days")
    private Integer doctorRecommendDays;

    /**
     * 产假扩展信息（JSON格式）
     */
    @Column(name = "maternity_leave_ext", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Map<String, Object> maternityLeaveExt;

    /**
     * 产假是否顺延
     */
    @Column(name = "holiday_extend", nullable = false)
    private Boolean holidayExtend = false;

    /**
     * 是否有津贴
     */
    @Column(name = "has_allowance", nullable = false)
    private Boolean hasAllowance = true;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

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
