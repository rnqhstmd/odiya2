package com.loopers.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StorageService {

    private final S3Presigner s3Presigner;
    private final StorageConfig config;

    public PresignedUrlResult generatePresignedUrl(String fileName, String contentType) {
        // Path Traversal 방지: basename만 추출하고 위험 문자 제거
        String sanitized = fileName.replaceAll("[/\\\\]", "").replaceAll("\\.\\.", "");
        if (sanitized.isBlank()) {
            sanitized = "profile.jpg";
        }
        String objectKey = "profile-images/" + UUID.randomUUID() + "/" + sanitized;

        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(config.getBucket())
            .key(objectKey)
            .contentType(contentType)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(config.getPresignedUrlExpiration()))
            .putObjectRequest(putRequest)
            .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
            config.getBucket(), config.getRegion(), objectKey);

        return new PresignedUrlResult(presigned.url().toString(), publicUrl);
    }

    public record PresignedUrlResult(String presignedUrl, String imageUrl) {}
}
