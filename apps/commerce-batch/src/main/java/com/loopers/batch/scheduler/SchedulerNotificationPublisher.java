package com.loopers.batch.scheduler;

import com.loopers.domain.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SchedulerNotificationPublisher {

    private static final String TOPIC = "notification.send";

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public void publish(NotificationEvent event) {
        kafkaTemplate.send(TOPIC, event.eventId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.warn("알림 이벤트 발행 실패: eventId={}, error={}", event.eventId(), ex.getMessage(), ex);
                }
            });
    }
}
