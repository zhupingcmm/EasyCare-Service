package com.hr.maternity.entity;

import com.hr.maternity.dto.MiscarriageLeaveDetail;
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
 * 产假申请记录实体类
 */
@Entity
@Table(name = "t_maternity_leave_request")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class MaternityLeaveRequestDO {

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

    @Column(name = "expected_delivery_date", nullable = false)
    private LocalDate expectedDeliveryDate;

    @Column(name = "is_multiple_birth", nullable = false)
    private Boolean isMultipleBirth = false;

    @Column(name = "number_of_babies", nullable = false)
    private Integer numberOfBabies = 1;

    @Column(name = "has_extended_days", nullable = false)
    private Boolean hasExtendedDays = false;

    @Column(name = "is_difficult_birth", nullable = false)
    private Boolean isDifficultBirth = false;

    @Column(name = "additional_dystocia_days")
    private Integer additionalDystociaDays = 0;

    @Column(name = "is_breast_feeding")
    private Boolean isBreastFeeding = false;

    @Column(name = "is_miscarriage", nullable = false)
    private Boolean isMiscarriage = false;

    @Column(name = "is_first_time_birth")
    private Boolean isFirstTimeBirth;

    @Column(name = "miscarriage_leave_detail", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private MiscarriageLeaveDetail miscarriageLeaveDetail;


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
