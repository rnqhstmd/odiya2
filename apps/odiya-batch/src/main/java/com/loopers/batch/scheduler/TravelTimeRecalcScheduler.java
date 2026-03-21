package com.loopers.batch.scheduler;

import com.loopers.batch.client.BatchKakaoMobilityClient;
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
public class TravelTimeRecalcScheduler {

    private static final int DURATION_CHANGE_THRESHOLD_MINUTES = 5;

    private static final String SQL = """
        SELECT
            tt.id              AS tt_id,
            tt.duration_minutes AS old_duration,
            p.id               AS participant_id,
            p.user_id          AS receiver_id,
            p.transport_type   AS transport_type,
            a.id               AS appointment_id,
            a.name             AS appointment_name,
            a.date_time        AS appointment_time,
            a.latitude         AS dest_lat,
            a.longitude        AS dest_lng,
            dp.latitude        AS origin_lat,
            dp.longitude       AS origin_lng
        FROM travel_times tt
        JOIN appointment_participants p ON tt.participant_id = p.id
        JOIN appointments a ON p.appointment_id = a.id
        JOIN departure_places dp ON p.departure_place_id = dp.id
        WHERE a.status = 'CONFIRMED'
          AND a.deleted_at IS NULL
          AND dp.deleted_at IS NULL
          AND p.status <> 'REJECTED'
          AND p.transport_type IN ('CAR_PARKING', 'CAR_PICKUP')
          AND p.departure_place_id IS NOT NULL
          AND DATE(a.date_time) = CURRENT_DATE
          AND a.date_time > NOW()
          AND (
            (a.date_time BETWEEN NOW() + INTERVAL 30 MINUTE AND NOW() + INTERVAL 60 MINUTE
             AND tt.calculated_at < NOW() - INTERVAL 30 MINUTE)
            OR
            (a.date_time BETWEEN NOW() AND NOW() + INTERVAL 30 MINUTE
             AND tt.calculated_at < NOW() - INTERVAL 10 MINUTE)
          )
        """;

    private static final String USER_SETTINGS_SQL =
        "SELECT parking_buffer_minutes, extra_minutes FROM user_settings " +
        "WHERE user_id = ? AND deleted_at IS NULL LIMIT 1";

    private static final String UPDATE_TRAVEL_TIME_SQL =
        "UPDATE travel_times SET duration_minutes = ?, calculated_at = NOW(), " +
        "departure_alert_at = ?, is_fallback = false WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final BatchKakaoMobilityClient batchKakaoMobilityClient;
    private final NotificationProcessor notificationProcessor;

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void run() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL);
            if (!rows.isEmpty()) {
                log.info("[TravelTimeRecalcScheduler] 대상 {}건 처리 시작", rows.size());
            }

            for (Map<String, Object> row : rows) {
                try {
                    processRow(row);
                } catch (Exception e) {
                    log.warn("[TravelTimeRecalcScheduler] 처리 실패: ttId={}, error={}",
                        row.get("tt_id"), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.warn("[TravelTimeRecalcScheduler] 스케줄러 실행 실패: error={}", e.getMessage(), e);
        }
    }

    private void processRow(Map<String, Object> row) {
        Long ttId = toLong(row.get("tt_id"));
        int oldDuration = ((Number) row.get("old_duration")).intValue();
        double originLng = ((Number) row.get("origin_lng")).doubleValue();
        double originLat = ((Number) row.get("origin_lat")).doubleValue();
        double destLng = ((Number) row.get("dest_lng")).doubleValue();
        double destLat = ((Number) row.get("dest_lat")).doubleValue();

        // M3: 캐시 우회, Kakao Mobility API 직접 호출. 실패 시 skip.
        int newDuration;
        try {
            newDuration = batchKakaoMobilityClient.calculateDuration(originLng, originLat, destLng, destLat);
        } catch (Exception e) {
            log.warn("[TravelTimeRecalcScheduler] API 호출 실패, skip: ttId={}, error={}", ttId, e.getMessage());
            return;
        }

        // user_settings 조회하여 departureAlertAt 재계산
        Long receiverId = toLong(row.get("receiver_id"));
        String transportType = (String) row.get("transport_type");
        Map<String, Object> settings = queryUserSettings(receiverId);
        int parkingBuffer = "CAR_PARKING".equals(transportType) && settings != null
            ? ((Number) settings.getOrDefault("parking_buffer_minutes", 0)).intValue() : 0;
        int extraMinutes = settings != null
            ? ((Number) settings.getOrDefault("extra_minutes", 5)).intValue() : 5;

        int totalMinutes = newDuration + parkingBuffer + extraMinutes;
        Timestamp appointmentTime = (Timestamp) row.get("appointment_time");
        ZonedDateTime appointmentZdt = appointmentTime.toInstant().atZone(java.time.ZoneId.systemDefault());
        ZonedDateTime departureAlertAt = appointmentZdt.minusMinutes(totalMinutes);

        // travel_times 갱신 (변동 무관)
        jdbcTemplate.update(UPDATE_TRAVEL_TIME_SQL,
            newDuration, Timestamp.from(departureAlertAt.toInstant()), ttId);

        // 5분+ 변동 시 알림 발송
        int diff = Math.abs(newDuration - oldDuration);
        if (diff >= DURATION_CHANGE_THRESHOLD_MINUTES) {
            Long appointmentId = toLong(row.get("appointment_id"));
            String appointmentName = (String) row.get("appointment_name");
            notificationProcessor.processTravelTimeChanged(receiverId, appointmentId, appointmentName, diff);
        }
    }

    private Map<String, Object> queryUserSettings(Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(USER_SETTINGS_SQL, userId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Long toLong(Object value) {
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        return null;
    }
}
