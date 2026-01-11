package com.ocbc.ms.easy.care.entity;

import com.ocbc.ms.easy.care.dto.CityRemark;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

/**
 * 城市实体类
 */
@Entity
@Table(name = "t_city")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class CityDO {

    /**
     * 主键ID 
     */
    @Id
    @Column(name = "id")
    private Integer id;


    /**
     * 城市代码
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 城市中文名称
     */
    @Column(name = "cn_name", length = 100)
    private String chineseName;

    /**
     * 城市英文名称
     */
    @Column(name = "en_name", length = 100)
    private String englishName;

    /**
     * 省份/地区
     */
    @Column(name = "province", length = 100)
    private String province;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 排序序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private CityRemark remark;

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
