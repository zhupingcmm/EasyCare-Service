package com.ocbc.ms.easy.care.entity;

import com.ocbc.ms.easy.care.dto.TimeScope;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 产假计算结果实体类
 */
@Entity
@Table(name = "maternity_leave_result")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class MaternityLeaveResultDO {

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

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "total_allowance_days", nullable = false)
    private Integer totalAllowanceDays;

    @Column(name = "base_days", nullable = false)
    private Integer baseDays = 0;

    @Column(name = "dystocia_days")
    private Integer dystociaDays = 0;

    @Column(name = "multi_baby_days")
    private Integer multiBabyDays = 0;

    @Column(name = "extended_days")
    private Integer extendedDays = 0;

    @Column(name = "miscarriage_leave_days")
    private Integer miscarriageLeaveDays = 0;

    @Column(name = "pub_holidays_count")
    private Integer pubHolidaysCount = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "return_to_work_date")
    private LocalDate returnToWorkDate;

    @Column(name = "time_scope_list", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<TimeScope> timeScopeList;

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
