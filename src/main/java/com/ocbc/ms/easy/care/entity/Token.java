package com.ocbc.ms.easy.care.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = "user")
public class Token {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "exp_time", nullable = false)
    private LocalDateTime expTime;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "op_acc_token", nullable = false, length = 2048)
    private String opAccToken;

    @Column(name = "op_ref_token", nullable = false, length = 2048)
    private String opRefToken;

    @Column(name = "acc_token", nullable = false, columnDefinition = "TEXT")
    private String accToken;

    @Column(name = "ref_token", nullable = false, columnDefinition = "TEXT")
    private String refToken;

    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 255)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
