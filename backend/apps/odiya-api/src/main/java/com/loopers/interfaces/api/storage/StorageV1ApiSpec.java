package com.loopers.interfaces.api.storage;

import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Storage V1 API", description = "S3 Presigned URL 발급 API")
public interface StorageV1ApiSpec {

    @Operation(
        summary = "Presigned URL 발급",
        description = "S3에 프로필 이미지를 업로드하기 위한 Presigned URL을 발급합니다."
    )
    ApiResponse<StorageV1Dto.PresignedUrlResponse> getPresignedUrl(
        LoginUser loginUser,
        StorageV1Dto.PresignedUrlRequest request
    );
}
