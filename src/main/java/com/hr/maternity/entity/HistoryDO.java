package com.hr.maternity.entity;

import com.hr.maternity.enums.RecordTypeEnum;
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
 * 历史记录实体类
 */
@Entity
@Table(name = "t_history")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class HistoryDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lan_id", nullable = false, length = 50)
    private String lanId;

    @Column(name = "maternity_leave_request_id")
    private Long maternityLeaveRequestId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maternity_leave_request_id", insertable = false, updatable = false)
    private MaternityLeaveRequestDO maternityLeaveRequest;

    @Column(name = "maternity_leave_result_id")
    private Long maternityLeaveResultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maternity_leave_result_id", insertable = false, updatable = false)
    private MaternityLeaveResultDO maternityLeaveResult;

    @Column(name = "maternity_allowance_request_id")
    private Long maternityAllowanceRequestId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maternity_allowance_request_id", insertable = false, updatable = false)
    private MaternityAllowanceRequestDO maternityAllowanceRequest;

    @Column(name = "maternity_allowance_result_id")
    private Long maternityAllowanceResultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maternity_allowance_result_id", insertable = false, updatable = false)
    private MaternityAllowanceResultDO maternityAllowanceResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 20)
    private RecordTypeEnum recordType = RecordTypeEnum.MATERNITY;

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
