package com.loopers.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    public S3Presigner s3Presigner(StorageProperties props) {
        S3Presigner.Builder builder = S3Presigner.builder()
            .region(Region.of(props.region()));

        // accessKey/secretKey가 명시된 경우에만 StaticCredentialsProvider 사용.
        // 비어 있으면 DefaultCredentialsProvider(환경변수, 프로필, IAM Role 체인)로 fallback.
        String accessKey = props.accessKey();
        String secretKey = props.secretKey();
        if (accessKey != null && !accessKey.isBlank()
            && secretKey != null && !secretKey.isBlank()) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            );
        }

        return builder.build();
    }
}
