package com.loopers.domain.user;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findActiveById(Long id);
    Optional<User> findActiveByKakaoId(Long kakaoId);
    boolean existsByNicknameAndDeletedAtIsNull(String nickname);
    boolean existsByNicknameAndDeletedAtIsNullAndIdNot(String nickname, Long id);
    User save(User user);
    List<User> searchByNickname(String keyword, Long excludeUserId, Pageable pageable);
}
