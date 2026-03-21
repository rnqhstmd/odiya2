package com.loopers.interfaces.api.tag;

import com.loopers.application.tag.TagInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TagV1Dto {
    public record CreateTagRequest(
        @NotBlank @Size(max = 20) String name,
        @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color
    ) {}
    public record UpdateTagRequest(
        @Size(max = 20) String name,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color
    ) {}
    public record TagResponse(Long id, String name, String color, boolean isDefault, int friendCount) {
        public static TagResponse from(TagInfo info) {
            return new TagResponse(info.id(), info.name(), info.color(), info.isDefault(), info.friendCount());
        }
    }
}
