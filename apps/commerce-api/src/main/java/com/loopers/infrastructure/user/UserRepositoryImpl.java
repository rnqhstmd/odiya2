package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<UserModel> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public Optional<UserModel> findActiveById(Long id) {
        return userJpaRepository.findActiveById(id);
    }

    @Override
    public Optional<UserModel> findActiveByKakaoId(Long kakaoId) {
        return userJpaRepository.findActiveByKakaoId(kakaoId);
    }

    @Override
    public boolean existsByNicknameAndDeletedAtIsNull(String nickname) {
        return userJpaRepository.existsByNicknameAndDeletedAtIsNull(nickname);
    }

    @Override
    public boolean existsByNicknameAndDeletedAtIsNullAndIdNot(String nickname, Long id) {
        return userJpaRepository.existsByNicknameAndDeletedAtIsNullAndIdNot(nickname, id);
    }

    @Override
    public UserModel save(UserModel userModel) {
        return userJpaRepository.save(userModel);
    }
}
