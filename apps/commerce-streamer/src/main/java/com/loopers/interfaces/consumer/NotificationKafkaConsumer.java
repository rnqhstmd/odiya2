package com.loopers.interfaces.consumer;

import com.loopers.confg.kafka.NotificationKafkaConfig;
import com.loopers.domain.notification.NotificationEvent;
import com.loopers.infrastructure.fcm.FcmClient;
import com.loopers.infrastructure.fcm.FcmSendResult;
import com.loopers.infrastructure.fcm.NotificationResultEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class NotificationKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaConsumer.class);
    private static final String RESULT_TOPIC = "notification.result";
    private static final String DLQ_TOPIC = "notification.send.dlq";
    private static final int MAX_RETRY = 3;
    private static final long[] RETRY_DELAYS_MS = {1000L, 2000L, 4000L};

    private final FcmClient fcmClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationKafkaConsumer(FcmClient fcmClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.fcmClient = fcmClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
        topics = "notification.send",
        containerFactory = NotificationKafkaConfig.NOTIFICATION_LISTENER,
        groupId = "notification-consumer"
    )
    public void consume(ConsumerRecord<String, NotificationEvent> record, Acknowledgment acknowledgment) {
        NotificationEvent event = record.value();

        if (event == null) {
            log.warn("Received null NotificationEvent, skipping.");
            acknowledgment.acknowledge();
            return;
        }

        List<String> tokens = event.tokens();
        if (tokens == null || tokens.isEmpty()) {
            log.info("No tokens for notificationId={}, skipping.", event.notificationId());
            acknowledgment.acknowledge();
            return;
        }

        Map<String, String> data = new java.util.HashMap<>();
        data.put("notificationId", String.valueOf(event.notificationId()));
        if (event.type() != null) data.put("type", event.type());
        if (event.referenceId() != null) data.put("referenceId", String.valueOf(event.referenceId()));
        if (event.referenceType() != null) data.put("referenceType", event.referenceType());

        FcmSendResult result = null;
        Exception lastException = null;

        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            try {
                result = fcmClient.sendToTokens(tokens, event.title(), event.body(), data);
                lastException = null;
                break;
            } catch (Exception e) {
                lastException = e;
                log.warn("FCM send failed (attempt {}/{}): {}", attempt + 1, MAX_RETRY, e.getMessage());
                if (attempt < MAX_RETRY - 1) {
                    try {
                        Thread.sleep(RETRY_DELAYS_MS[attempt]);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Retry sleep interrupted.");
                    }
                }
            }
        }

        if (lastException != null) {
            log.error("All {} retries exhausted for notificationId={}. Sending to DLQ.", MAX_RETRY, event.notificationId(), lastException);
            kafkaTemplate.send(DLQ_TOPIC, record.key(), event);
            kafkaTemplate.send(RESULT_TOPIC, record.key(),
                new NotificationResultEvent(event.notificationId(), false, List.of()));
        } else {
            boolean success = result.failureCount() == 0 || result.successCount() > 0;
            kafkaTemplate.send(RESULT_TOPIC, record.key(),
                new NotificationResultEvent(event.notificationId(), success, result.invalidTokens()));
        }

        acknowledgment.acknowledge();
    }
}
