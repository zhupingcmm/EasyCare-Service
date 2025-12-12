package com.ocbc.ms.easy.care.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 特殊日期实体类（节假日/补班）
 */
@Entity
@Table(name = "t_special_day")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "region", nullable = false, length = 10)
    private String region = "CN";

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "cn_name", nullable = false, length = 100)
    private String cnName;

    @Column(name = "en_name", nullable = false, length = 100)
    private String enName;

    @Column(name = "type", nullable = false)
    private Integer type;

    @Column(name = "is_public_holiday", nullable = false)
    private Boolean isPublicHoliday = true;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @CreatedDate
    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "create_by", length = 100)
    private String createBy;

    @LastModifiedDate
    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "update_by", length = 100)
    private String updateBy;

    @PrePersist
    protected void onCreate() {
        if (region == null) {
            region = "CN";
        }
        if (isPublicHoliday == null) {
            isPublicHoliday = true;
        }
        if (enabled == null) {
            enabled = true;
        }
    }

    /**
     * 特殊日期类型常量
     */
    public static class SpecialDayType {
        public static final Integer HOLIDAY = 1;      // 节假日
        public static final Integer WORKDAY = 2;      // 补班
    }

    /**
     * 节假日类型枚举（保留兼容）
     * @deprecated 使用 SpecialDayType 常量代替
     */
    @Deprecated
    public enum HolidayType {
        public_holiday,    // 公共假日
        transfer_workday   // 调休工作日
    }
}
