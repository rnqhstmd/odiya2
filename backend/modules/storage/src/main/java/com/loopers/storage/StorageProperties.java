package com.loopers.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws.s3")
public record StorageProperties(
    String bucket,
    String region,
    String accessKey,
    String secretKey,
    int presignedUrlExpiration
) {}
