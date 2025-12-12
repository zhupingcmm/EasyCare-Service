package com.ocbc.ms.easy.care.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Token 复合主键类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenId implements Serializable {

    private UUID id;
    private LocalDateTime expTime;
}
