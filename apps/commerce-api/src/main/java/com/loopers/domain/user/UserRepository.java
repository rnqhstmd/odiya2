package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findActiveById(Long id);
    Optional<User> findActiveByKakaoId(Long kakaoId);
    boolean existsByNicknameAndDeletedAtIsNull(String nickname);
    boolean existsByNicknameAndDeletedAtIsNullAndIdNot(String nickname, Long id);
    User save(User user);
}
