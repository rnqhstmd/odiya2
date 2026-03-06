package com.loopers.infrastructure.tag;

import com.loopers.domain.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagJpaRepository extends JpaRepository<Tag, Long> {
    @Query("SELECT t FROM Tag t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Tag> findActiveById(@Param("id") Long id);

    @Query("SELECT t FROM Tag t WHERE t.owner.id = :ownerId AND t.deletedAt IS NULL ORDER BY t.isDefault DESC, t.createdAt ASC")
    List<Tag> findAllByOwnerIdAndDeletedAtIsNull(@Param("ownerId") Long ownerId);

    @Query("SELECT t FROM Tag t WHERE t.owner.id = :ownerId AND t.isDefault = true AND t.deletedAt IS NULL ORDER BY t.createdAt ASC")
    Optional<Tag> findFirstDefaultTagByOwnerId(@Param("ownerId") Long ownerId);
}
