package com.loopers.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppointmentCompletionScheduler {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void run() {
        try {
            int updated = jdbcTemplate.update(
                "UPDATE appointments SET status = 'COMPLETED', updated_at = NOW() " +
                "WHERE status = 'CONFIRMED' " +
                "AND date_time < NOW() " +
                "AND deleted_at IS NULL");

            if (updated > 0) {
                log.info("[AppointmentCompletionScheduler] {}건 COMPLETED 전이", updated);
            }
        } catch (Exception e) {
            log.warn("[AppointmentCompletionScheduler] 스케줄러 실행 실패: error={}", e.getMessage(), e);
        }
    }
}
