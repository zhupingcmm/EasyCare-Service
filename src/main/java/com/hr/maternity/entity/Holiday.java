package com.hr.maternity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 节假日实体类
 */
@Entity
@Table(name = "holiday", schema = "hr-maternity-cn-dev",
       uniqueConstraints = @UniqueConstraint(name = "uk_holiday_year_region_date", 
                                           columnNames = {"year", "region", "date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holiday {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "region", nullable = false, length = 10)
    private String region = "CN";

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "name_cn", nullable = false, length = 100)
    private String nameCn;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private HolidayType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 节假日类型枚举
     */
    public enum HolidayType {
        public_holiday,    // 公共假日
        transfer_workday   // 调休工作日
    }
}
