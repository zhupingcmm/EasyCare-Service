package com.hr.maternity.email.service;

import com.hr.maternity.email.dto.PdfEmailRequest;
import com.hr.maternity.email.dto.PdfEmailResponse;

/**
 * PDF邮件发送服务接口
 */
public interface PdfEmailService {

    /**
     * 发送PDF文件到指定邮箱
     *
     * @param request PDF邮件发送请求
     * @return 发送结果
     */
    PdfEmailResponse sendPdfToEmail(PdfEmailRequest request);
}
