package com.loopers.interfaces.api.storage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class StorageV1Dto {

    public record PresignedUrlRequest(
        @NotBlank
        @Size(max = 255)
        @Pattern(regexp = "^[\\w\\-. ]+\\.(?:jpg|jpeg|png|webp|heic)$", message = "허용되지 않는 파일명입니다.")
        String fileName,
        @NotBlank @Pattern(regexp = "^image/(jpeg|png|webp|heic)$") String contentType
    ) {}

    public record PresignedUrlResponse(
        String presignedUrl,
        String imageUrl
    ) {}
}
