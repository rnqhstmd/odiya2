package com.loopers.interfaces.api.device;

import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Device V1 API", description = "디바이스 토큰 관리 API")
public interface DeviceV1ApiSpec {

    @Operation(summary = "디바이스 토큰 등록", description = "푸시 알림을 위한 디바이스 토큰을 등록합니다.")
    ApiResponse<Void> registerDevice(LoginUser loginUser, DeviceV1Dto.RegisterDeviceRequest request);

    @Operation(summary = "디바이스 토큰 해제", description = "디바이스 토큰을 비활성화합니다.")
    ApiResponse<Void> deactivateDevice(LoginUser loginUser, String token);
}
