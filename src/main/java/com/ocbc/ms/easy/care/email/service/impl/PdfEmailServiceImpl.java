package com.ocbc.ms.easy.care.email.service.impl;

import com.ocbc.ms.easy.care.email.dto.PdfEmailRequest;
import com.ocbc.ms.easy.care.email.dto.PdfEmailResponse;
import com.ocbc.ms.easy.care.email.service.PdfEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

/**
 * PDF邮件发送服务实现
 */
@Slf4j
@RequiredArgsConstructor
public class PdfEmailServiceImpl implements PdfEmailService {

    @Override
    public PdfEmailResponse sendPdfToEmail(PdfEmailRequest request) {
        log.warn("邮件功能已下线，忽略发送请求，收件人: {}", request.getEmailAddress());

        return PdfEmailResponse.builder()
                .success(false)
                .message("邮件功能已下线")
                .sentTime(LocalDateTime.now())
                .emailAddress(request.getEmailAddress())
                .pdfPath(request.getPdfPath())
                .build();
    }
}
