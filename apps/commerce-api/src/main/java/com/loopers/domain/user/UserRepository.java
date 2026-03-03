package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<UserModel> findById(Long id);
    Optional<UserModel> findActiveById(Long id);
    Optional<UserModel> findActiveByKakaoId(Long kakaoId);
    boolean existsByNicknameAndDeletedAtIsNull(String nickname);
    boolean existsByNicknameAndDeletedAtIsNullAndIdNot(String nickname, Long id);
    UserModel save(UserModel userModel);
}
