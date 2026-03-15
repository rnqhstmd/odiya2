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
public class DepartureLocationMissingScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final NotificationProcessor notificationProcessor;

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void run() {
        try {
            ZonedDateTime now = ZonedDateTime.now();
            // appointments.date_time - 120분 이 [now, now+1분) 범위
            // => appointments.date_time 이 [now+119분, now+121분) 범위
            Timestamp from = Timestamp.from(now.plusMinutes(119).toInstant());
            Timestamp to = Timestamp.from(now.plusMinutes(121).toInstant());

            // departure_place_id NULL인 참여자만 대상
            String sql = "SELECT p.id AS participant_id, " +
                "p.user_id AS receiver_id, " +
                "a.id AS appointment_id, " +
                "a.name AS appointment_name " +
                "FROM appointments a " +
                "JOIN appointment_participants p ON p.appointment_id = a.id " +
                "WHERE a.date_time >= ? AND a.date_time < ? " +
                "AND a.status NOT IN ('CANCELLED', 'COMPLETED') " +
                "AND a.deleted_at IS NULL " +
                "AND p.status <> 'REJECTED' " +
                "AND p.departure_place_id IS NULL";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, from, to);
            log.info("[DepartureLocationMissingScheduler] 대상 {}건 처리 시작", rows.size());

            for (Map<String, Object> row : rows) {
                try {
                    notificationProcessor.processDepartureLocationMissing(row);
                } catch (Exception e) {
                    log.warn("[DepartureLocationMissingScheduler] 참여자 처리 실패: participantId={}, error={}",
                        row.get("participant_id"), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.warn("[DepartureLocationMissingScheduler] 스케줄러 실행 실패: error={}", e.getMessage(), e);
        }
    }
}
