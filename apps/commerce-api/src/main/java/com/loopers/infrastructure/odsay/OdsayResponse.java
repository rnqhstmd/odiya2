package com.loopers.infrastructure.odsay;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OdsayResponse(
    Result result
) {
    public record Result(
        @JsonProperty("searchType") int searchType,
        @JsonProperty("path") List<Path> path
    ) {}

    public record Path(
        @JsonProperty("pathType") int pathType,
        Info info
    ) {}

    public record Info(
        @JsonProperty("totalTime") int totalTime,
        @JsonProperty("totalDistance") int totalDistance
    ) {}
}
