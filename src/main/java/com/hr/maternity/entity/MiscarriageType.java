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
import java.util.List;

/**
 * 流产类型实体类
 */
@Entity
@Table(name = "t_miscarriage_type")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class MiscarriageType {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 类型名称（如：早期流产、晚期流产）
     */
    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

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

    /**
     * 一对多关系：一个流产类型可以对应多个产假规则
     */
    @OneToMany(mappedBy = "miscarriageType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaternityRules> maternityRules;
}
