package com.loopers.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StorageService {

    private static final String DEFAULT_FILE_NAME = "profile.jpg";

    private final S3Presigner s3Presigner;
    private final StorageProperties properties;

    public PresignedUrlResult generatePresignedUrl(Long userId, String fileName, String contentType) {
        // Path Traversal 방지: basename만 추출 (DTO 단계 정규식 검증과 이중 방어)
        String sanitized = new File(fileName).getName();
        if (sanitized.isBlank()) {
            sanitized = DEFAULT_FILE_NAME;
        }
        String objectKey = "profile-images/" + userId + "/" + UUID.randomUUID() + "/" + sanitized;

        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(properties.bucket())
            .key(objectKey)
            .contentType(contentType)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(properties.presignedUrlExpiration()))
            .putObjectRequest(putRequest)
            .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
            properties.bucket(), properties.region(), objectKey);

        return new PresignedUrlResult(presigned.url().toString(), publicUrl);
    }

    public record PresignedUrlResult(String presignedUrl, String imageUrl) {}
}
