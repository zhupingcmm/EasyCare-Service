package com.ocbc.ms.easy.care.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * PDF邮件发送请求DTO
 */
@Data
public class PdfEmailRequest {

    @NotBlank(message = "PDF文件路径不能为空")
    private String pdfPath;

    @NotBlank(message = "收件人邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String emailAddress;

    @NotBlank(message = "邮件主题不能为空")
    private String subject;

    @NotBlank(message = "邮件内容不能为空")
    private String content;
}
