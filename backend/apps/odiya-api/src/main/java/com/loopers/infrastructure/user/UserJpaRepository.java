package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.kakaoId = :kakaoId AND u.deletedAt IS NULL")
    Optional<User> findActiveByKakaoId(@Param("kakaoId") Long kakaoId);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    boolean existsByNicknameAndDeletedAtIsNullAndIdNot(String nickname, Long id);

    @Query("SELECT u FROM User u WHERE u.nickname LIKE CONCAT('%', :keyword, '%') ESCAPE '!' AND u.deletedAt IS NULL AND u.id != :excludeUserId")
    List<User> searchByNickname(@Param("keyword") String keyword, @Param("excludeUserId") Long excludeUserId, Pageable pageable);
}
