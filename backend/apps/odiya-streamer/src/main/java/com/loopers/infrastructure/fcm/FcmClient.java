package com.loopers.infrastructure.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FcmClient {

    private static final Logger log = LoggerFactory.getLogger(FcmClient.class);

    public FcmSendResult sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("FirebaseApp not initialized. Skipping FCM send.");
            return new FcmSendResult(0, tokens.size(), new ArrayList<>(tokens), List.of());
        }

        MulticastMessage message = MulticastMessage.builder()
            .addAllTokens(tokens)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .putAllData(data)
            .build();

        try {
            BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            List<SendResponse> responses = batchResponse.getResponses();

            int successCount = batchResponse.getSuccessCount();
            int failureCount = batchResponse.getFailureCount();
            List<String> failedTokens = new ArrayList<>();
            List<String> invalidTokens = new ArrayList<>();

            for (int i = 0; i < responses.size(); i++) {
                SendResponse sendResponse = responses.get(i);
                if (!sendResponse.isSuccessful()) {
                    String token = tokens.get(i);
                    failedTokens.add(token);
                    FirebaseMessagingException exception = sendResponse.getException();
                    if (exception != null && MessagingErrorCode.UNREGISTERED.equals(exception.getMessagingErrorCode())) {
                        invalidTokens.add(token);
                    }
                }
            }

            return new FcmSendResult(successCount, failureCount, failedTokens, invalidTokens);
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast send failed: {}", e.getMessage(), e);
            return new FcmSendResult(0, tokens.size(), new ArrayList<>(tokens), List.of());
        }
    }
}
