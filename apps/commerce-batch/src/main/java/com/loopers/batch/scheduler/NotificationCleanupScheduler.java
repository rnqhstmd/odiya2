package com.loopers.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationCleanupScheduler {

    private static final int BATCH_LIMIT = 10_000;

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void run() {
        try {
            Timestamp cutoff = Timestamp.from(ZonedDateTime.now().minusDays(90).toInstant());
            int deleted = jdbcTemplate.update(
                "DELETE FROM notifications WHERE id IN " +
                "(SELECT id FROM (SELECT id FROM notifications WHERE created_at < ? " +
                "AND status IN ('SENT', 'FAILED', 'CANCELLED') LIMIT ?) AS sub)",
                cutoff, BATCH_LIMIT);
            log.info("[NotificationCleanupScheduler] 알림 {}건 삭제 완료", deleted);
        } catch (Exception e) {
            log.warn("[NotificationCleanupScheduler] 스케줄러 실행 실패: error={}", e.getMessage(), e);
        }
    }
}
