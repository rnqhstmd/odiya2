package com.loopers.domain.tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {
    Tag save(Tag tag);
    Optional<Tag> findActiveById(Long id);
    List<Tag> findAllByOwnerIdAndDeletedAtIsNull(Long ownerId);
    Optional<Tag> findDefaultTagByOwnerId(Long ownerId);
}
