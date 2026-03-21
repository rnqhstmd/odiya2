package com.loopers.infrastructure.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FcmConfig {

    private static final Logger log = LoggerFactory.getLogger(FcmConfig.class);

    @Value("${fcm.credentials-path:}")
    private String credentialsPath;

    @PostConstruct
    public void init() {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.info("FCM credentials-path not configured. Skipping FirebaseApp initialization (test environment).");
            return;
        }
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp already initialized.");
            return;
        }
        try (InputStream inputStream = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(inputStream))
                .build();
            FirebaseApp.initializeApp(options);
            log.info("FirebaseApp initialized successfully.");
        } catch (IOException e) {
            throw new IllegalStateException("FirebaseApp 초기화 실패. credentials-path를 확인하세요: " + credentialsPath, e);
        }
    }
}
