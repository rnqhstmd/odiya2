package com.loopers.batch.scheduler;

import com.loopers.domain.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        String type = "APPOINTMENT_REMINDER";

        // 중복 체크 (SENT 또는 PENDING 상태 포함)
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notifications WHERE receiver_id = ? AND type = ? AND reference_id = ? AND status IN ('SENT', 'PENDING')",
            Integer.class, receiverId, type, appointmentId);
        if (count != null && count > 0) {
            return;
        }

        List<String> tokens = jdbcTemplate.queryForList(
            "SELECT token FROM device_tokens WHERE user_id = ? AND active = true",
            String.class, receiverId);

        String title = appointmentName;
        String body = "약속 1시간 전이에요!";
        String eventId = UUID.randomUUID().toString();

        jdbcTemplate.update(
            "INSERT INTO notifications (event_id, receiver_id, sender_id, type, title, body, status, is_read, reference_id, reference_type, created_at, updated_at) " +
            "VALUES (?, ?, NULL, ?, ?, ?, 'PENDING', false, ?, 'APPOINTMENT', NOW(), NOW())",
            eventId, receiverId, type, title, body, appointmentId);

        Long notificationId = jdbcTemplate.queryForObject(
            "SELECT id FROM notifications WHERE event_id = ?", Long.class, eventId);

        NotificationEvent event = new NotificationEvent(
            eventId, notificationId, receiverId, tokens,
            type, title, body, appointmentId, "APPOINTMENT");
        publisher.publish(event);
    }

    @Transactional
    public void processDepartureReminder(Map<String, Object> row) {
        Long receiverId = toLong(row.get("receiver_id"));
        Long appointmentId = toLong(row.get("appointment_id"));
        String appointmentName = (String) row.get("appointment_name");
        // travel_times 기반 폴링이므로 departure_place_id가 null인 참여자는 조회되지 않음 — 항상 DEPARTURE_REMINDER
        String type = "DEPARTURE_REMINDER";

        // 중복 체크 (SENT 또는 PENDING 상태 포함)
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notifications WHERE receiver_id = ? AND type = ? AND reference_id = ? AND status IN ('SENT', 'PENDING')",
            Integer.class, receiverId, type, appointmentId);
        if (count != null && count > 0) {
            return;
        }

        List<String> tokens = jdbcTemplate.queryForList(
            "SELECT token FROM device_tokens WHERE user_id = ? AND active = true",
            String.class, receiverId);

        String title = appointmentName;
        String body = "출발할 시간이에요!";
        String eventId = UUID.randomUUID().toString();

        jdbcTemplate.update(
            "INSERT INTO notifications (event_id, receiver_id, sender_id, type, title, body, status, is_read, reference_id, reference_type, created_at, updated_at) " +
            "VALUES (?, ?, NULL, ?, ?, ?, 'PENDING', false, ?, 'APPOINTMENT', NOW(), NOW())",
            eventId, receiverId, type, title, body, appointmentId);

        Long notificationId = jdbcTemplate.queryForObject(
            "SELECT id FROM notifications WHERE event_id = ?", Long.class, eventId);

        NotificationEvent event = new NotificationEvent(
            eventId, notificationId, receiverId, tokens,
            type, title, body, appointmentId, "APPOINTMENT");
        publisher.publish(event);
    }

    @Transactional
    public void processDepartureLocationMissing(Map<String, Object> row) {
        Long receiverId = toLong(row.get("receiver_id"));
        Long appointmentId = toLong(row.get("appointment_id"));
        String appointmentName = (String) row.get("appointment_name");
        String type = "DEPARTURE_LOCATION_MISSING";

        // 중복 체크 (SENT 또는 PENDING 상태 포함)
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notifications WHERE receiver_id = ? AND type = ? AND reference_id = ? AND status IN ('SENT', 'PENDING')",
            Integer.class, receiverId, type, appointmentId);
        if (count != null && count > 0) {
            return;
        }

        List<String> tokens = jdbcTemplate.queryForList(
            "SELECT token FROM device_tokens WHERE user_id = ? AND active = true",
            String.class, receiverId);

        String title = appointmentName;
        String body = "출발지를 등록해주세요! 약속 2시간 전입니다.";
        String eventId = UUID.randomUUID().toString();

        jdbcTemplate.update(
            "INSERT INTO notifications (event_id, receiver_id, sender_id, type, title, body, status, is_read, reference_id, reference_type, created_at, updated_at) " +
            "VALUES (?, ?, NULL, ?, ?, ?, 'PENDING', false, ?, 'APPOINTMENT', NOW(), NOW())",
            eventId, receiverId, type, title, body, appointmentId);

        Long notificationId = jdbcTemplate.queryForObject(
            "SELECT id FROM notifications WHERE event_id = ?", Long.class, eventId);

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
