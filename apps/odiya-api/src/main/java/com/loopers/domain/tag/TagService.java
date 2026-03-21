package com.loopers.domain.tag;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public void initDefaultTags(User owner) {
        tagRepository.save(Tag.createDefault(owner, "친구", "#5AC8FA"));
        tagRepository.save(Tag.createDefault(owner, "연인", "#FF2D55"));
        tagRepository.save(Tag.createDefault(owner, "가족", "#34C759"));
    }

    @Transactional(readOnly = true)
    public List<Tag> getTagsByOwner(Long ownerId) {
        return tagRepository.findAllByOwnerIdAndDeletedAtIsNull(ownerId);
    }

    @Transactional(readOnly = true)
    public Tag getTag(Long tagId) {
        return tagRepository.findActiveById(tagId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 태그입니다."));
    }

    @Transactional(readOnly = true)
    public Tag getDefaultTag(Long ownerId) {
        return tagRepository.findDefaultTagByOwnerId(ownerId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "기본 태그를 찾을 수 없습니다."));
    }

    @Transactional
    public Tag createTag(User owner, String name, String color) {
        return tagRepository.save(Tag.create(owner, name, color));
    }

    @Transactional
    public Tag updateTag(Long tagId, Long ownerId, String name, String color) {
        Tag tag = getTag(tagId);
        validateOwnership(tag, ownerId);
        if (name != null) tag.changeName(name);
        if (color != null) tag.changeColor(color);
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Long tagId, Long ownerId) {
        Tag tag = getTag(tagId);
        validateOwnership(tag, ownerId);
        tag.guardDeletable();
        tag.delete();
        tagRepository.save(tag);
    }

    private void validateOwnership(Tag tag, Long ownerId) {
        if (!tag.getOwner().getId().equals(ownerId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 태그입니다.");
        }
    }
}
