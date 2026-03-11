package com.loopers.infrastructure.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoMobilityResponse(
    @JsonProperty("trans_id") String transId,
    List<Route> routes
) {
    public record Route(
        @JsonProperty("result_code") int resultCode,
        @JsonProperty("result_msg") String resultMsg,
        Summary summary
    ) {}

    public record Summary(
        Distance distance,
        Duration duration
    ) {}

    public record Distance(
        int value,
        String text
    ) {}

    public record Duration(
        int value,
        String text
    ) {}
}
