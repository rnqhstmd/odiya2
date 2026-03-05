package com.loopers.application.tag;

import com.loopers.domain.tag.Tag;

public record TagInfo(Long id, String name, String color, boolean isDefault, int friendCount) {
    public static TagInfo from(Tag tag) {
        return new TagInfo(tag.getId(), tag.getName(), tag.getColor(), tag.isDefault(), 0);
    }

    public static TagInfo of(Tag tag, int friendCount) {
        return new TagInfo(tag.getId(), tag.getName(), tag.getColor(), tag.isDefault(), friendCount);
    }
}
