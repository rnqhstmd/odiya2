package com.loopers.batch.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.mobility")
public record BatchKakaoMobilityProperties(
    String apiUrl,
    String restApiKey,
    int connectTimeoutSeconds,
    int readTimeoutSeconds
) {}
