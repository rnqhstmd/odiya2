package com.loopers.application.tag;

import com.loopers.domain.friend.FriendService;
import com.loopers.domain.tag.Tag;
import com.loopers.domain.tag.TagService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class TagFacade {
    private final TagService tagService;
    private final UserService userService;
    private final FriendService friendService;

    public List<TagInfo> getMyTags(Long userId) {
        return tagService.getTagsByOwner(userId).stream()
            .map(tag -> TagInfo.of(tag, friendService.countAcceptedFriendsByTag(userId, tag.getId())))
            .toList();
    }

    public TagInfo createTag(Long userId, String name, String color) {
        User owner = userService.getUser(userId);
        Tag tag = tagService.createTag(owner, name, color);
        return TagInfo.from(tag);
    }

    public TagInfo updateTag(Long userId, Long tagId, String name, String color) {
        Tag tag = tagService.updateTag(tagId, userId, name, color);
        return TagInfo.from(tag);
    }

    @Transactional
    public void deleteTag(Long userId, Long tagId) {
        // 1. 소유권 검증 먼저
        Tag tag = tagService.getTag(tagId);
        if (!tag.getOwner().getId().equals(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 태그입니다.");
        }
        tag.guardDeletable();

        // 2. 기본 태그 조회
        Tag defaultTag = tagService.getDefaultTag(userId);

        // 3. 해당 태그를 사용하는 본인 friendship만 기본 태그로 변경
        friendService.revertFriendshipsToDefaultTag(tagId, defaultTag);

        // 4. 태그 삭제 (이미 소유권 검증했으므로 직접 delete)
        tag.delete();
    }
}
