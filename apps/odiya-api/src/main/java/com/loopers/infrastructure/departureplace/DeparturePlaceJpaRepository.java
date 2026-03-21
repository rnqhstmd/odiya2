package com.loopers.infrastructure.departureplace;

import com.loopers.domain.departureplace.DeparturePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeparturePlaceJpaRepository extends JpaRepository<DeparturePlace, Long> {

    @Query("SELECT d FROM DeparturePlace d WHERE d.id = :id AND d.deletedAt IS NULL")
    Optional<DeparturePlace> findActiveById(@Param("id") Long id);

    @Query("SELECT d FROM DeparturePlace d WHERE d.user.id = :userId AND d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    List<DeparturePlace> findAllActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(d) FROM DeparturePlace d WHERE d.user.id = :userId AND d.deletedAt IS NULL")
    int countActiveByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM departure_places WHERE user_id = :userId AND deleted_at IS NULL FOR UPDATE", nativeQuery = true)
    int countActiveByUserIdWithLock(@Param("userId") Long userId);
}
