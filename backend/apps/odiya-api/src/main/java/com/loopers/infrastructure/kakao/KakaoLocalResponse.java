package com.loopers.infrastructure.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoLocalResponse(
    List<Document> documents,
    Meta meta
) {
    public record Document(
        String id,
        @JsonProperty("place_name") String placeName,
        @JsonProperty("address_name") String addressName,
        @JsonProperty("road_address_name") String roadAddressName,
        @JsonProperty("category_group_name") String categoryGroupName,
        String x,
        String y
    ) {}

    public record Meta(
        @JsonProperty("total_count") int totalCount,
        @JsonProperty("pageable_count") int pageableCount,
        @JsonProperty("is_end") boolean isEnd
    ) {}
}
