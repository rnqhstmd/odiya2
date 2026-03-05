package com.loopers.infrastructure.usersettings;

import com.loopers.domain.usersettings.UserSettings;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserSettingsJpaRepository extends JpaRepository<UserSettings, Long> {

    @Query("SELECT s FROM UserSettings s WHERE s.user.id = :userId AND s.deletedAt IS NULL")
    Optional<UserSettings> findActiveByUserId(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM UserSettings s WHERE s.user.id = :userId AND s.deletedAt IS NULL")
    Optional<UserSettings> findActiveByUserIdWithLock(@Param("userId") Long userId);
}
