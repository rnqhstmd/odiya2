package com.loopers.infrastructure.odsay;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "odsay")
public record OdsayProperties(
    String apiUrl,
    String apiKey,
    int connectTimeoutSeconds,
    int readTimeoutSeconds
) {}
