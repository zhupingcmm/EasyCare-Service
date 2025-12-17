package com.ocbc.ms.easy.care.config;

import com.ocbc.ms.easy.care.repository.NonceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NonceCleanupScheduler {

    private final NonceRepository nonceRepository;

    @Value("${nonce.delete.threshold:1000}")
    private int threshold;

    @Async("asyncServiceExecutor")
    @Scheduled(cron = "${nonce.delete.cron:0 0 2 * * ?}")
    @SchedulerLock(name = "nonceDelete", lockAtMostFor = "9m", lockAtLeastFor = "1m")
    public void run() {
        log.info("{call deleteYesterdayNonces(?)} .. " + threshold);
        String result = nonceRepository.deleteYesterdayNonces(threshold);
        log.info("Cron job completed to deleteYesterdayNonces .. " + result);
    }
}
