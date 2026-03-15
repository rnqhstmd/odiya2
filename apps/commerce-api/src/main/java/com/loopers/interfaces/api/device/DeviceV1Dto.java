package com.loopers.interfaces.api.device;

import com.loopers.domain.notification.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DeviceV1Dto {

    public record RegisterDeviceRequest(
        @NotBlank @Size(max = 4096) String token,
        @NotNull DeviceType deviceType
    ) {}
}
