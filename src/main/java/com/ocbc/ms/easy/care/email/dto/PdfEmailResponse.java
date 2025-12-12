package com.ocbc.ms.easy.care.email.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PDF邮件发送响应DTO
 */
@Data
@Builder
public class PdfEmailResponse {

    private boolean success;
    private String message;
    private LocalDateTime sentTime;
    private String emailAddress;
    private String pdfPath;
}
