package com.hr.maternity.email.controller;

import com.hr.maternity.common.ApiResponse;
import com.hr.maternity.email.dto.PdfEmailRequest;
import com.hr.maternity.email.dto.PdfEmailResponse;
import com.hr.maternity.email.service.PdfEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * PDF邮件发送控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/pdf-email")
@RequiredArgsConstructor
@Tag(name = "PDF邮件发送", description = "发送PDF文件到指定邮箱")
public class PdfEmailController {

    private final PdfEmailService pdfEmailService;

    @PostMapping("/send")
    @Operation(summary = "发送PDF到邮箱")
    public ResponseEntity<ApiResponse<PdfEmailResponse>> sendPdfToEmail(@Valid @RequestBody PdfEmailRequest request) {
        log.info("收到PDF邮件发送请求，收件人: {}", request.getEmailAddress());
        
        PdfEmailResponse response = pdfEmailService.sendPdfToEmail(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(1, response.getMessage()));
        }
    }
}
