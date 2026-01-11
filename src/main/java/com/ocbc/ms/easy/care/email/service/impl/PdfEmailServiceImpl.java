package com.ocbc.ms.easy.care.email.service.impl;

import com.ocbc.ms.easy.care.email.config.EmailProperties;
import com.ocbc.ms.easy.care.email.dto.PdfEmailRequest;
import com.ocbc.ms.easy.care.email.dto.PdfEmailResponse;
import com.ocbc.ms.easy.care.email.service.PdfEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.time.LocalDateTime;

/**
 * PDF邮件发送服务实现
 */
@Slf4j
@RequiredArgsConstructor
public class PdfEmailServiceImpl implements PdfEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final EmailProperties emailProperties;

    @Override
    public PdfEmailResponse sendPdfToEmail(PdfEmailRequest request) {
        log.info("开始发送PDF邮件，收件人: {}, PDF路径: {}", request.getEmailAddress(), request.getPdfPath());
        
        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                log.warn("未配置邮件发送器(JavaMailSender)，请配置 spring.mail.host 等参数后再使用邮件功能");
                return PdfEmailResponse.builder()
                        .success(false)
                        .message("邮件服务未配置，请先配置 spring.mail.host 等参数")
                        .sentTime(LocalDateTime.now())
                        .emailAddress(request.getEmailAddress())
                        .pdfPath(request.getPdfPath())
                        .build();
            }

            // 验证PDF文件是否存在
            File pdfFile = new File(request.getPdfPath());
            if (!pdfFile.exists() || !pdfFile.isFile()) {
                log.error("PDF文件不存在或不是有效文件: {}", request.getPdfPath());
                return PdfEmailResponse.builder()
                        .success(false)
                        .message("PDF文件不存在或不是有效文件: " + request.getPdfPath())
                        .sentTime(LocalDateTime.now())
                        .emailAddress(request.getEmailAddress())
                        .pdfPath(request.getPdfPath())
                        .build();
            }

            // 创建MIME消息
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // 设置邮件基本信息
            helper.setFrom(emailProperties.getFrom());
            helper.setTo(request.getEmailAddress());
            helper.setSubject(request.getSubject());
            helper.setText(request.getContent());

            // 添加PDF附件
            FileSystemResource resource = new FileSystemResource(pdfFile);
            helper.addAttachment(resource.getFilename(), resource);

            // 发送邮件
            mailSender.send(mimeMessage);
            
            log.info("PDF邮件发送成功，收件人: {}", request.getEmailAddress());
            
            return PdfEmailResponse.builder()
                    .success(true)
                    .message("PDF邮件发送成功")
                    .sentTime(LocalDateTime.now())
                    .emailAddress(request.getEmailAddress())
                    .pdfPath(request.getPdfPath())
                    .build();
                    
        } catch (Exception e) {
            log.error("发送PDF邮件失败", e);
            return PdfEmailResponse.builder()
                    .success(false)
                    .message("发送PDF邮件失败: " + e.getMessage())
                    .sentTime(LocalDateTime.now())
                    .emailAddress(request.getEmailAddress())
                    .pdfPath(request.getPdfPath())
                    .build();
        }
    }
}
