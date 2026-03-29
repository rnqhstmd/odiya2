package com.loopers.interfaces.api.storage;

import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.storage.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/storage")
public class StorageV1Controller implements StorageV1ApiSpec {

    private final StorageService storageService;

    @PostMapping("/presigned-url")
    @Override
    public ApiResponse<StorageV1Dto.PresignedUrlResponse> getPresignedUrl(
        @AuthenticationPrincipal LoginUser loginUser,
        @Valid @RequestBody StorageV1Dto.PresignedUrlRequest request
    ) {
        var result = storageService.generatePresignedUrl(request.fileName(), request.contentType());
        return ApiResponse.success(new StorageV1Dto.PresignedUrlResponse(result.presignedUrl(), result.imageUrl()));
    }
}
