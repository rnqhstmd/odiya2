package com.loopers.interfaces.api.departureplace;

import com.loopers.application.departureplace.DeparturePlaceInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DeparturePlaceV1Dto {

    public record CreateDeparturePlaceRequest(
        @NotBlank String label,
        @NotBlank String address,
        @NotNull Double latitude,
        @NotNull Double longitude
    ) {}

    public record UpdateDeparturePlaceRequest(
        String label,
        String address,
        Double latitude,
        Double longitude
    ) {}

    public record DeparturePlaceResponse(Long id, String label, String address,
                                         Double latitude, Double longitude) {
        public static DeparturePlaceResponse from(DeparturePlaceInfo info) {
            return new DeparturePlaceResponse(
                info.id(),
                info.label(),
                info.address(),
                info.latitude(),
                info.longitude()
            );
        }
    }
}
