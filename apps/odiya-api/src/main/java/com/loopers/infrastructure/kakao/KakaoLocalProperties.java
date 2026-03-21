package com.loopers.infrastructure.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.local")
public record KakaoLocalProperties(
    String apiUrl,
    String restApiKey,
    int connectTimeoutSeconds,
    int readTimeoutSeconds
) {}
