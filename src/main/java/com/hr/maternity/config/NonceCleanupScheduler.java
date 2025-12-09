package com.hr.maternity.config;

import com.hr.maternity.util.RSAUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "encryption.rsa-enabled", havingValue = "true")
public class NonceCleanupScheduler {

    private final RSAUtil rsaUtil;

    @Scheduled(cron = "${encryption.nonce-cleanup-cron:0 */10 * * * ?}")
    public void cleanupExpiredNonces() {
        log.debug("开始执行nonce清理任务");
        try {
            int expiredCount = rsaUtil.cleanupExpiredNonces();
            int usedCount = rsaUtil.cleanupUsedNonces(7);
            log.info("nonce清理任务完成，已清理过期记录: {}, 已清理使用记录: {}", expiredCount, usedCount);
        } catch (Exception e) {
            log.error("nonce清理任务执行失败", e);
        }
    }
}
