package com.loopers.interfaces.api.device;

import com.loopers.application.notification.NotificationFacade;
import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/devices")
public class DeviceV1Controller implements DeviceV1ApiSpec {

    private final NotificationFacade notificationFacade;

    @PostMapping
    @Override
    public ApiResponse<Void> registerDevice(
        @AuthenticationPrincipal LoginUser loginUser,
        @Valid @RequestBody DeviceV1Dto.RegisterDeviceRequest request
    ) {
        notificationFacade.registerDevice(loginUser.userId(), request.token(), request.deviceType());
        return ApiResponse.success();
    }

    @DeleteMapping("/{token}")
    @Override
    public ApiResponse<Void> deactivateDevice(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable String token
    ) {
        notificationFacade.deactivateDevice(token, loginUser.userId());
        return ApiResponse.success();
    }
}
