package com.loopers.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class DepartureReminderScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final NotificationProcessor notificationProcessor;

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void run() {
        try {
            ZonedDateTime now = ZonedDateTime.now();
            Timestamp from = Timestamp.from(now.toInstant());
            Timestamp to = Timestamp.from(now.plusMinutes(1).toInstant());

            // travel_times where departure_alert_at in [now, now+1min)
            // exclude CANCELLED/COMPLETED appointments, REJECTED participants
            String sql = "SELECT tt.id AS tt_id, " +
                "p.id AS participant_id, " +
                "p.user_id AS receiver_id, " +
                "a.id AS appointment_id, " +
                "a.name AS appointment_name " +
                "FROM travel_times tt " +
                "JOIN appointment_participants p ON tt.participant_id = p.id " +
                "JOIN appointments a ON p.appointment_id = a.id " +
                "WHERE tt.departure_alert_at >= ? AND tt.departure_alert_at < ? " +
                "AND a.status NOT IN ('CANCELLED', 'COMPLETED') " +
                "AND a.deleted_at IS NULL " +
                "AND p.status <> 'REJECTED'";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, from, to);
            log.info("[DepartureReminderScheduler] 대상 {}건 처리 시작", rows.size());

            for (Map<String, Object> row : rows) {
                try {
                    notificationProcessor.processDepartureReminder(row);
                } catch (Exception e) {
                    log.warn("[DepartureReminderScheduler] 참여자 처리 실패: participantId={}, error={}",
                        row.get("participant_id"), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.warn("[DepartureReminderScheduler] 스케줄러 실행 실패: error={}", e.getMessage(), e);
        }
    }
}
