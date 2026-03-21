package com.loopers.infrastructure.tag;

import com.loopers.domain.tag.Tag;
import com.loopers.domain.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class TagRepositoryImpl implements TagRepository {
    private final TagJpaRepository tagJpaRepository;

    @Override
    public Tag save(Tag tag) { return tagJpaRepository.save(tag); }

    @Override
    public Optional<Tag> findActiveById(Long id) { return tagJpaRepository.findActiveById(id); }

    @Override
    public List<Tag> findAllByOwnerIdAndDeletedAtIsNull(Long ownerId) {
        return tagJpaRepository.findAllByOwnerIdAndDeletedAtIsNull(ownerId);
    }

    @Override
    public Optional<Tag> findDefaultTagByOwnerId(Long ownerId) {
        return tagJpaRepository.findFirstDefaultTagByOwnerId(ownerId);
    }
}
