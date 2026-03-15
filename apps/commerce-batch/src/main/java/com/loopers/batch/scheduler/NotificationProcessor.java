package com.loopers.batch.scheduler;

import com.loopers.domain.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationProcessor {

    private final JdbcTemplate jdbcTemplate;
    private final SchedulerNotificationPublisher publisher;

    @Transactional
    public void processAppointmentReminder(Map<String, Object> row) {
        Long receiverId = toLong(row.get("receiver_id"));
        Long appointmentId = toLong(row.get("appointment_id"));
        String appointmentName = (String) row.get("appointment_name");
        publishNotification(receiverId, appointmentId, "APPOINTMENT_REMINDER", appointmentName, "약속 1시간 전이에요!");
    }

    @Transactional
    public void processDepartureReminder(Map<String, Object> row) {
        Long receiverId = toLong(row.get("receiver_id"));
        Long appointmentId = toLong(row.get("appointment_id"));
        String appointmentName = (String) row.get("appointment_name");
        publishNotification(receiverId, appointmentId, "DEPARTURE_REMINDER", appointmentName, "출발할 시간이에요!");
    }

    @Transactional
    public void processDepartureLocationMissing(Map<String, Object> row) {
        Long receiverId = toLong(row.get("receiver_id"));
        Long appointmentId = toLong(row.get("appointment_id"));
        String appointmentName = (String) row.get("appointment_name");
        publishNotification(receiverId, appointmentId, "DEPARTURE_LOCATION_MISSING", appointmentName, "출발지를 등록해주세요! 약속 2시간 전입니다.");
    }

    private void publishNotification(Long receiverId, Long appointmentId, String type, String title, String body) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notifications WHERE receiver_id = ? AND type = ? AND reference_id = ? AND status IN ('SENT', 'PENDING')",
            Integer.class, receiverId, type, appointmentId);
        if (count != null && count > 0) {
            return;
        }

        List<String> tokens = jdbcTemplate.queryForList(
            "SELECT token FROM device_tokens WHERE user_id = ? AND active = true",
            String.class, receiverId);

        String eventId = UUID.randomUUID().toString();
        String insertSql = "INSERT INTO notifications (event_id, receiver_id, sender_id, type, title, body, status, is_read, reference_id, reference_type, created_at, updated_at) " +
            "VALUES (?, ?, NULL, ?, ?, ?, 'PENDING', false, ?, 'APPOINTMENT', NOW(), NOW())";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql, new String[]{"id"});
            ps.setString(1, eventId);
            ps.setLong(2, receiverId);
            ps.setString(3, type);
            ps.setString(4, title);
            ps.setString(5, body);
            ps.setLong(6, appointmentId);
            return ps;
        }, keyHolder);

        Long notificationId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;

        NotificationEvent event = new NotificationEvent(
            eventId, notificationId, receiverId, tokens,
            type, title, body, appointmentId, "APPOINTMENT");
        publisher.publish(event);
    }

    private Long toLong(Object value) {
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        return null;
    }
}
