package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserModel, Long> {

    @Query("SELECT u FROM UserModel u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<UserModel> findActiveById(@Param("id") Long id);

    @Query("SELECT u FROM UserModel u WHERE u.kakaoId = :kakaoId AND u.deletedAt IS NULL")
    Optional<UserModel> findActiveByKakaoId(@Param("kakaoId") Long kakaoId);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    boolean existsByNicknameAndDeletedAtIsNullAndIdNot(String nickname, Long id);
}
