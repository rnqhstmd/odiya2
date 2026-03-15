package com.loopers.interfaces.consumer;

import com.loopers.confg.kafka.NotificationKafkaConfig;
import com.loopers.domain.notification.DeviceTokenService;
import com.loopers.domain.notification.NotificationService;
import com.loopers.domain.notification.NotificationStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationResultConsumer.class);

    private final NotificationService notificationService;
    private final DeviceTokenService deviceTokenService;

    public NotificationResultConsumer(NotificationService notificationService, DeviceTokenService deviceTokenService) {
        this.notificationService = notificationService;
        this.deviceTokenService = deviceTokenService;
    }

    @KafkaListener(
        topics = "notification.result",
        containerFactory = NotificationKafkaConfig.NOTIFICATION_LISTENER,
        groupId = "notification-result-consumer"
    )
    public void consume(ConsumerRecord<String, NotificationResultEvent> record, Acknowledgment acknowledgment) {
        NotificationResultEvent payload = record.value();
        if (payload == null) {
            log.warn("Received null notification result payload, skipping.");
            acknowledgment.acknowledge();
            return;
        }

        Long notificationId = payload.notificationId();
        if (notificationId == null) {
            log.warn("notificationId is null in result payload, skipping.");
            acknowledgment.acknowledge();
            return;
        }

        NotificationStatus status = payload.success() ? NotificationStatus.SENT : NotificationStatus.FAILED;
        notificationService.updateStatus(notificationId, status);

        List<String> invalidTokens = payload.invalidTokens();
        if (invalidTokens != null && !invalidTokens.isEmpty()) {
            for (String token : invalidTokens) {
                deviceTokenService.deactivateByToken(token);
            }
        }

        acknowledgment.acknowledge();
    }
}
